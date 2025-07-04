package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.aksw.jenax.graphql.sparql.v2.api2.Connective;
import org.aksw.jenax.graphql.sparql.v2.context.CardinalityDirective;
import org.aksw.jenax.graphql.sparql.v2.context.Cascadable;
import org.aksw.jenax.graphql.sparql.v2.context.ConditionDirective;
import org.aksw.jenax.graphql.sparql.v2.context.GraphDirective;
import org.aksw.jenax.graphql.sparql.v2.context.IndexDirective;
import org.aksw.jenax.graphql.sparql.v2.context.JoinDirective;
import org.aksw.jenax.graphql.sparql.v2.context.RootFieldMarker;
import org.aksw.jenax.graphql.sparql.v2.context.ViaDirective;
import org.aksw.jenax.graphql.sparql.v2.context.VocabDirective;
import org.aksw.jenax.graphql.util.GraphQlUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;

import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Field;
import graphql.util.TraverserContext;

public class XGraphQlUtils {

    /**
     * Return true if there is no parent context with a root field marker.
     * In that case, the root field marker is added to the given context.
     */
    public static boolean isRootNode(graphql.language.Node<?> node, TraverserContext<?> context) {
        boolean result = context.getVarFromParents(RootFieldMarker.class) == null;
        if (!context.isVisited()) {
            if (result) {
                context.setVar(RootFieldMarker.class, new RootFieldMarker());
            }
        }
        return result;
    }

    public static String fieldToJsonKey(Field field) {
        String result = field.getName();
        return result;
    }

    public static P_Path0 fieldToRonKey(Field field) {
        P_Path0 result = fieldToRdfKey(field);
        // If no Path could be obtained then fall back on the field name
        if (result == null) {
            result = new P_Link(NodeFactory.createLiteralString(field.getName()));
            // result = new P_Link(NodeFactoryExtra.createLiteralNode(field.getName(), null, GraphQlIoBridge.DATATYPE_IRI_JSON));
        }
        return result;
    }

    public static P_Path0 fieldToRdfKey(Field field) {
        P_Path0 result = null;
        Directive directive = GraphQlUtils.expectAtMostOneDirective(field, "emitRdfKey");
        if (directive != null) {
            result = XGraphQlUtils.parseEmitRdfKey(directive);
        }
        return result;
    }


    // @emitRdfKey(iri: "", reverse: true)
    // FIXME In general this should be an expression that can compute the property and direction dynamically
    // With constant folding the expression may turn out to be constant
    public static P_Path0 parseEmitRdfKey(Directive directive) {
        String iriStr = GraphQlUtils.getArgAsString(directive, "iri");
        boolean isForward = !Optional.ofNullable(GraphQlUtils.getArgAsBoolean(directive, "reverse")).orElse(false);

        Node node = NodeFactory.createURI(iriStr);
        P_Path0 result = isForward ? new P_Link(node) : new P_ReverseLink(node);
        return result;
    }

//    public static String tidyElementStr(String str) {
//        String result = str.trim();
//        // Strip the string from an outermost curly braces pair
//        if (result.startsWith("{") && result.endsWith("}")) {
//            result = result.substring(1, result.length() - 1).trim();
//        }
//        return result;
//    }

    public static Element parseElement(String str, PrefixMap prefixMap, String base) {
        // String tidyStr = tidyElementStr(str);

        PrefixMapping pm = new PrefixMappingAdapter(prefixMap);
        Query query = new Query();
        query.setPrefixMapping(pm);

        // Beware: Closing brace must be added after newline to cope with comment on last line
        String finalStr = "SELECT * {\n" + str  + "\n}";
        QueryFactory.parse(query, finalStr, base, Syntax.syntaxARQ);
        Element result = query.getQueryPattern();
        if (result instanceof ElementGroup g && g.size() == 1) {
            result = g.get(0);
        }
        return result;
    }

    public static Connective parsePattern(DirectivesContainer<?> directivesContainer, PrefixMap prefixMap) {
        Directive patternDirective = GraphQlUtils.expectAtMostOneDirective(directivesContainer, "pattern");
        Connective connective = patternDirective == null ? null :
            XGraphQlUtils.parsePattern(patternDirective, prefixMap);
        return connective;
    }

    public static Directive newDirectivePattern(Connective connective) {
        Directive result = GraphQlUtils.newDirective("pattern",
            GraphQlUtils.newArgString("of", Objects.toString(connective.getElement())),
            GraphQlUtils.newArgString("from", Var.varNames(connective.getConnectVars())),
            GraphQlUtils.newArgString("to", Var.varNames(connective.getDefaultTargetVars())));
        return result;
    }

    public static Connective parsePattern(Directive directive, PrefixMap prefixMap) {
        String elementStr = GraphQlUtils.getArgAsString(directive, "of");
        List<String> fromVarNames = GraphQlUtils.getArgAsStrings(directive, "from");
        List<String> toVarNames = GraphQlUtils.getArgAsStrings(directive, "to");

        Element elt = parseElement(elementStr, prefixMap, null);
//        if (fromVarNames == null || toVarNames == null) {
//            Set<Var> vars = VarHelper.vars(elt);
//            List<String> varNames = Var.varNames(vars);
//            if (fromVarNames == null) {
//                fromVarNames = varNames;
//            }
//            if (toVarNames == null) {
//                toVarNames = varNames;
//            }
//        }

        Connective result = Connective.newBuilder()
            // .prefixMap(prefixMap) // TODO Implement
            .element(elt)
            .connectVarNames(fromVarNames)
            .targetVarNames(toVarNames)
            .build();
        return result;
    }

    public static Cascadable parseCascadable(Directive directive, boolean cascadesByDefault)  {
        Boolean rawIsSelf = GraphQlUtils.getArgAsBoolean(directive, "self");
        Boolean rawIsCascade = GraphQlUtils.getArgAsBoolean(directive, "cascade");

        boolean dftSelf;
        boolean dftCascade;

        if (!cascadesByDefault) {
            // If self is explicitly false then cascade defaults to true
            // If cascade is explicitly true then self defaults to false
            dftCascade = Boolean.FALSE.equals(rawIsSelf);
            dftSelf = !Boolean.TRUE.equals(rawIsCascade);
        } else {
            // If self is explicitly true then cascade defaults to false
            // self is always true, unless explicitly disabled
            dftCascade = !Boolean.TRUE.equals(rawIsSelf);
            dftSelf = true; // Boolean.FALSE.equals(rawIsCascade);
        }

        boolean isSelf = rawIsSelf != null ? rawIsSelf : dftSelf;
        boolean isCascade = rawIsCascade != null ? rawIsCascade : dftCascade;
        return new Cascadable(isSelf, isCascade);
    }

    public static CardinalityDirective parseCardinality(DirectivesContainer<?> directives) {
        Directive one = GraphQlUtils.expectAtMostOneDirective(directives, "one");
        Directive many = GraphQlUtils.expectAtMostOneDirective(directives, "many");

        if (one != null && many != null) {
            System.err.println("@one and @many provided");
        }

        CardinalityDirective result = null;

        Cascadable cascadable;
        if (one != null) {
             cascadable = parseCascadable(one, false);
             result = new CardinalityDirective(true, cascadable);
        }

        if (many != null) {
            cascadable = parseCascadable(many, false);
            result = new CardinalityDirective(false, cascadable);
        }

        return result;
    }

    public static GraphDirective parseGraph(DirectivesContainer<?> directives) {
        GraphDirective result = null;
        Directive graph = GraphQlUtils.expectAtMostOneDirective(directives, "graph");
        if (graph != null) {
            List<String> graphIris = GraphQlUtils.getArgAsStrings(graph, "iri");
            String varName = GraphQlUtils.getArgAsString(graph, "var");
            Cascadable cascadable = parseCascadable(graph, true);
            result = new GraphDirective(varName, graphIris, cascadable.isSelf(), cascadable.isCascade());
        }
        return result;
    }

    public static IndexDirective parseIndex(DirectivesContainer<?> directives) {
        IndexDirective result = null;
        Directive index = GraphQlUtils.expectAtMostOneDirective(directives, "index");
        if (index != null) {
            String keyExpr = GraphQlUtils.getArgAsString(index, "by");
            // List<String> exprStrs = GraphQlUtils.getArgAsStrings(index, "by");
            String oneIf = GraphQlUtils.getArgAsString(index, "oneIf");
            result = new IndexDirective(keyExpr, null, oneIf);
        }
        return result;
    }

    public static VocabDirective parseVocab(DirectivesContainer<?> directives) {
        VocabDirective result = null;
        Directive graph = GraphQlUtils.expectAtMostOneDirective(directives, "vocab");
        if (graph != null) {
            String iri = GraphQlUtils.getArgAsString(graph, "iri");
            result = new VocabDirective(iri);
        }
        return result;
    }

    public static JoinDirective parseJoin(DirectivesContainer<?> directives) {
        JoinDirective result = null;
        Directive directive = GraphQlUtils.expectAtMostOneDirective(directives, "join");
        if (directive != null) {
            List<String> parentVarNames = GraphQlUtils.getArgAsStrings(directive, "parent");
            List<String> thisVarNames = GraphQlUtils.getArgAsStrings(directive, "this");
            result = new JoinDirective(thisVarNames, parentVarNames);
        }
        return result;
    }

    public static List<ConditionDirective> parseConditions(DirectivesContainer<?> directives) {
        List<Directive> filters = directives.getDirectives("filter");
        List<ConditionDirective> result = filters.stream().map(XGraphQlUtils::parseCondition).toList();
        return result;
    }

    public static ConditionDirective parseCondition(Directive directive) {
        String whenExprStr = GraphQlUtils.getArgAsString(directive, "when");
        String byExprStr = GraphQlUtils.getArgAsString(directive, "by");
        List<String> parentVarNames = GraphQlUtils.getArgAsStrings(directive, "parent");
        List<String> thisVarNames = GraphQlUtils.getArgAsStrings(directive, "this");
        ConditionDirective result = new ConditionDirective(whenExprStr, byExprStr, thisVarNames, parentVarNames);
        return result;
    }
    // TODO: Parse multiple directives
    @Deprecated
    public static ConditionDirective parseCondition(DirectivesContainer<?> directives) {
        ConditionDirective result = null;
        Directive directive = GraphQlUtils.expectAtMostOneDirective(directives, "filter");
        if (directive != null) {
            String whenExprStr = GraphQlUtils.getArgAsString(directive, "when");
            String byExprStr = GraphQlUtils.getArgAsString(directive, "by");
            List<String> parentVarNames = GraphQlUtils.getArgAsStrings(directive, "parent");
            List<String> thisVarNames = GraphQlUtils.getArgAsStrings(directive, "this");
            result = new ConditionDirective(whenExprStr, byExprStr, thisVarNames, parentVarNames);
        }
        return result;
    }

    public static ViaDirective parseVia(DirectivesContainer<?> directives) {
        ViaDirective result = null;
        Directive directive = GraphQlUtils.expectAtMostOneDirective(directives, "via");
        if (directive != null) {
            List<String> varNames = GraphQlUtils.getArgAsStrings(directive, "of");
            result = new ViaDirective(varNames);
        }
        return result;
    }

    public static ViaDirective parseType(DirectivesContainer<?> directives, PrefixMap prefixMap) {
        ViaDirective result = null;
        Directive directive = GraphQlUtils.expectAtMostOneDirective(directives, "type");
        if (directive != null) {
            List<String> varNames = GraphQlUtils.getArgAsStrings(directive, "iri");
            result = new ViaDirective(varNames);
        }
        return result;
    }


    /** null: Empty string:*/
//    public static String parseVocab(Field directive) {
//        VocabDirective result = null;
//        Directive graph = GraphQlUtils.expectAtMostOneDirective(field, "vocab");
//        if (graph != null) {
//            String iri = GraphQlUtils.getArgAsString(graph, "iri");
//            result = new VocabDirective(iri);
//        }
//        return result;
//    }


//    public static String deriveFieldIri(Context context, String fieldName) {
//        // If base, iri or ns is specified then resolve the field to that IRI
//        String result = null;
//        String base = context.getFinalBase();
//        String ns = context.getFinalNs();
//        String iri = context.getFinalIri();
//
//        if (base != null && !base.isBlank()) {
//            result = base + fieldName;
//        }
//
//        String namespace = null;
////        if (prefix != null) {
////            namespace = context.getPrefix(prefix);
////        }
//        if (ns != null) {
//            namespace = ns;
//        }
//
//        if (namespace != null) {
//            result = namespace + fieldName;
//        }
//
//        if (iri != null) {
//            result = iri;
//        }
//
//        return result;
//    }
}
