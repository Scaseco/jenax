package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.aksw.jenax.graphql.sparql.v2.context.VocabDirective;
import org.aksw.jenax.graphql.sparql.v2.util.GraphQlUtils;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.ExprUtils;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Field;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Node;
import graphql.language.StringValue;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;

/**
 * Expands:
 * <ul>
 *   <li>&#064;prefix</li>
 *   <li>&#064;graph</li>
 *   <li>&#064;service</li>
 * </ul>
 * <ul>
 *   <li>&#064;vocab</li>
 *   <li>&#064;reverse</li>
 *   <li>&#064;iri</li>
 *   <li>&#064;source</li>
 *   <li>&#064;class</li>
 *   <li>&#064;emitRdfKey</li>
 * </ul>
 * Order of resolution:
 * <ol>
 *   <li>if vocab is given then build tentative field iri/li>
 *   <li>if iri is given then override the field iri</li>
 *   <li>if class is given then use the field iri to build a &#064;source directive</li>
 *   <li>
 * </ol>
 * interplay of source and pattern: source would filter the sources of a given pattern.
 *
 * Expands
 * <pre>@rdf(iri: "rdfs:label") @reverse</pre>
 * into
 * <pre>@pattern(of: "?s rdfs:label ?o", src: 'o', tgt 's'} @emitRdfKey(iri: "rdfs:label", reverse: true)</pre>
 */
// <pre>@emitJsonKey(name: "field with whitespace")</pre>
public class TransformExpandShorthands
    extends NodeVisitorPrefixesBase
{
    @Override
    public TraversalControl visitInlineFragmentActual(InlineFragment node, TraverserContext<Node> context) {
        return process("", node, node, context, (n, newDirectives) -> n.transform(builder -> builder.directives(newDirectives)));
    }

//    public boolean processDirectives(DirectivesContainer<?> node) {
//        LinkedList<Directive> remainingDirectives = new LinkedList<>(node.getDirectives());
//
//        boolean change = false;
//        change = processFilter(node, null, remainingDirectives) || change;
//
//        return change;
//    }

    @Override
    public TraversalControl visitFieldActual(Field field, TraverserContext<Node> context) {
        return process(field.getName(), field, field, context, (node, newDirectives) -> node.transform(builder -> builder.directives(newDirectives)));
    }

    public <T extends Node<T>> TraversalControl process(String nodeName, T node, DirectivesContainer<?> field, TraverserContext<Node> context, BiFunction<T, List<Directive>, T> transform) {
        // boolean isRootField = isRootField(field, context);
        PrefixMap pm = getEffectivePrefixMap(context);
        PrefixMapping pming = new PrefixMappingAdapter(pm);

        // If we don't have a pattern yet but there is a vocab then create one
        String vocabIri = processVocab(field, context);

        // Tentative fieldIri
        String fieldIri = vocabIri == null ? null : vocabIri + nodeName;

        // List<Directive> sparqlDirectives = field.getDirectives("sparql");
        List<Directive> rdfDirectives = field.getDirectives("rdf");
        boolean hasEmitRdfKey = field.hasDirective("emitRdfKey");
        boolean hasClass = field.hasDirective("class");

        LinkedList<Directive> remainingDirectives = new LinkedList<>(field.getDirectives());
        boolean changed = false;
        boolean hasPattern = false;

        boolean isForward = !field.hasDirective("reverse");

        // Replace class directives with @source
        ListIterator<Directive> it = remainingDirectives.listIterator();
        while (it.hasNext()) {
            Directive directive = it.next();
            if ("class".equals(directive.getName())) {
                String tmpClassIri = null;
                String rawNs = GraphQlUtils.getArgAsString(directive, "ns");
                if (rawNs != null) {
                    String resolved = pm.get(rawNs);
                    if (resolved == null) {
                        throw new RuntimeException("Namespace " + rawNs + " not declared");
                    }
                    tmpClassIri = resolved + nodeName;
                }
                String rawIri = GraphQlUtils.getArgAsString(directive, "iri");
                String finalIri = rawIri != null
                        ? pm.expand(rawIri)
                        : tmpClassIri != null
                            ? tmpClassIri
                            : fieldIri;

                if (finalIri == null) {
                    throw new RuntimeException("Could not resolve @class annotation to an IRI. Set @vocab or provide arguments to @class.");
                }

                it.remove();
                Directive replacement = newDirectiveSource(finalIri);
                it.add(replacement);
                changed = true;
            }
        }

        // From an iri directive generate emitRdfKey and pattern
        // If either exists then nothing is done - if neither exists issue a warning that iri is ignored
        if (!rdfDirectives.isEmpty()) {
            // Only last rdf directive takes effect
            Directive rdfDirective = rdfDirectives.get(rdfDirectives.size() - 1);
            String iriStr = GraphQlUtils.getArgAsString(rdfDirective, "iri");
            String nsStr = GraphQlUtils.getArgAsString(rdfDirective, "ns");

            if (iriStr != null && nsStr != null) {
                System.err.println("Warn: iri and ns are mutually exclusive");
            }

            String finalNs = nsStr == null ? null : Optional.ofNullable(pm.get(nsStr)).orElseGet(() -> pm.expand(nsStr));
            String finalIri = iriStr == null ? null : Optional.ofNullable(pm.get(iriStr)).orElseGet(() -> pm.expand(iriStr));

            String finalFieldName = nodeName;

            fieldIri = finalIri != null
                    ? finalIri
                    : finalNs + finalFieldName;

            boolean removeShortcuts = true;

            hasPattern = field.hasDirective("pattern");

            Set<String> handledFields = Set.of("rdf", "reverse");

            remainingDirectives = field.getDirectives().stream()
                    .filter(x -> !removeShortcuts || !handledFields.contains(x.getName()))
                    .collect(Collectors.toCollection(LinkedList::new));
            if (!hasPattern) {
                Directive p = newDirectivePattern(fieldIri, isForward);
                remainingDirectives.addFirst(p);
                changed = true;
                hasPattern = true;
            }

            if (!hasEmitRdfKey) {
                Directive.Builder pb = Directive.newDirective()
                    .name("emitRdfKey")
                    .argument(Argument.newArgument(
                        "iri",
                        StringValue.of(fieldIri))
                        .build());
                if (!isForward) {
                    pb.argument(Argument.newArgument("reverse", BooleanValue.of(true)).build());
                }

                Directive p = pb.build();
                remainingDirectives.addFirst(p);
                changed = true;
            }
        }

        Directive pattern = GraphQlUtils.expectAtMostOneDirective(field, "pattern");
        if (pattern != null) {
            remainingDirectives.removeIf(x -> "pattern".equals(x.getName()));
            String str = GraphQlUtils.getArgAsString(pattern, "of");
            Element elt = XGraphQlUtils.parseElement(str, pm, null);
            Directive newPattern = pattern.transform(builder -> {
                builder.arguments(pattern.getArguments().stream().map(arg -> {
                    return "of".equals(arg.getName())
                            ? Argument.newArgument("of", StringValue.of(XGraphQlUtils.tidyElementStr(elt.toString()))).build()
                            : arg;
                }).toList());
            });
            remainingDirectives.addFirst(newPattern);
            changed = true;
        }


        Directive index = GraphQlUtils.expectAtMostOneDirective(field, "index");
        if (index != null) {
            // List<String> strs = GraphQlUtils.getArgAsStrings(index, "by");
            String str = GraphQlUtils.getArgAsString(index, "by");
            // XGraphQlUtils.parseIndex(field);
            if (str != null) {
                remainingDirectives.removeIf(x -> "index".equals(x.getName()));
                // List<Expr> exprs = strs.stream().map(str -> ExprUtils.parse(str, pming)).toList();
                Expr expr = ExprUtils.parse(str, pming);
                Directive newIndex = index.transform(builder -> {
                    builder.arguments(index.getArguments().stream().map(arg -> {
                        return "by".equals(arg.getName())
                                ? Argument.newArgument("by", StringValue.of(ExprUtils.fmtSPARQL(expr))
                                        // ArrayValue.newArrayValue().values(
                                                // exprs.stream().map(ExprUtils::fmtSPARQL).map(StringValue::of).map(x -> (Value)x).toList()
                                        // ).build()
                                        )
                                  .build()
                                : arg;
                    }).toList());
                });
                remainingDirectives.addFirst(newIndex);
                changed = true;
            }
        }

        Directive via = GraphQlUtils.expectAtMostOneDirective(field, "via");
        if (via != null) {
            // Expansion of via: @pattern(of: "BIND(?x AS ?y)", from: "x", to: "y") @join(parent: "country", this: "x")
            List<String> parentVarNames = GraphQlUtils.getArgAsStrings(via, "of");

            if (field.hasDirective("pattern")) {
                throw new RuntimeException("@via cannot be combined with @pattern");
            }

            if (field.hasDirective("join")) {
                throw new RuntimeException("@via cannot be combined with @join");
            }

            remainingDirectives.removeIf(x -> "via".equals(x.getName()));
            remainingDirectives.addFirst(Directive.newDirective().name("join")
                    .argument(Argument.newArgument("parent", GraphQlUtils.toArrayValue(parentVarNames)).build()).build());

            // TODO This breaks if there are multiple variables in @via(of: ["v1", "v2"])
            //      The ElementNode model must be extended to allow for passing variables from parent to child (and remove the need of BIND blocks)
            remainingDirectives.addFirst(newDirectivePattern("BIND(?from AS ?to)", "from", "to"));
            changed = true;

        }

        changed = processFilter(field, pming, remainingDirectives) || changed;

        if (changed) {
            List<Directive> finalRemainingDirectives = remainingDirectives;
            T replacementNode = transform.apply(node, finalRemainingDirectives);
            //Field newField = field.transform(builder -> builder.directives(finalRemainingDirectives));
            TreeTransformerUtil.changeNode(context, replacementNode);
        }

        return TraversalControl.CONTINUE;
    }

    private boolean processFilter(DirectivesContainer<?> directives, PrefixMapping pming, LinkedList<Directive> remainingDirectives) {
        boolean changed = false;
        Directive filter = GraphQlUtils.expectAtMostOneDirective(directives, "filter");
        if (filter != null) {
            String exprStr = GraphQlUtils.getArgAsString(filter, "if");
            if (exprStr != null) {
                remainingDirectives.removeIf(x -> "filter".equals(x.getName()));
                Expr expr = ExprUtils.parse(exprStr, pming);
                Directive newFilter = filter.transform(builder -> {
                    builder.arguments(filter.getArguments().stream().map(arg -> {
                        return "if".equals(arg.getName())
                                ? Argument.newArgument("if", StringValue.of(ExprUtils.fmtSPARQL(expr))).build()
                                : arg;
                    }).toList());
                });
                remainingDirectives.addFirst(newFilter);
                changed = true;
            }
        }
        return changed;
    }


//    public static expandPrefixes(DirectivesContainer<?> directives, LinkedList<Directive> outDirectives, String name, List<String> singleFieldNames, List<String> arrayFieldNames) {
//        Directive directive = GraphQlUtils.expectAtMostOneDirective(directives, name);
//        if (directive != null) {
//            List<String> strs = GraphQlUtils.getArgAsStrings(pattern, "by");
//            if (strs != null) {
//                remainingDirectives.removeIf(x -> "index".equals(x.getName()));
//                List<Expr> exprs = strs.stream().map(str -> ExprUtils.parse(str, pming)).toList();
//                Directive newIndex = index.transform(builder -> {
//                    builder.arguments(index.getArguments().stream().map(arg -> {
//                        return "by".equals(arg.getName())
//                                ? Argument.newArgument("by",
//                                        ArrayValue.newArrayValue().values(
//                                                exprs.stream().map(ExprUtils::fmtSPARQL).map(StringValue::of).map(x -> (Value)x).toList()
//                                        ).build())
//                                  .build()
//                                : arg;
//                    }).toList());
//                });
//                remainingDirectives.addFirst(newIndex);
//                changed = true;
//            }
//        }
//
//    }

    /** Returns the vocab active for the given field. Registers a present directive to the context. */
    public static String processVocab(DirectivesContainer<?> directives, TraverserContext<Node> context) {
        VocabDirective vocab = null;
        if (!context.isVisited()) {
            vocab = XGraphQlUtils.parseVocab(directives);
            if (vocab != null) {
                context.setVar(VocabDirective.class, vocab);
            }
        } else {
            vocab = context.getVar(VocabDirective.class);
        }
        if (vocab == null) {
            context.getVarFromParents(VocabDirective.class);
        }
        String result = vocab == null ? null : vocab.getIri();
        return result;
    }

    public Directive newDirectiveSource(String effectiveIri) {
        Objects.requireNonNull(effectiveIri);
        Directive p = Directive.newDirective()
                .name("source")
                .argument(Argument.newArgument(
                    "of",
                    StringValue.of("?s a <" + effectiveIri + ">")) // XXX Could use Element rather than string
                    .build())
                .build();
        return p;
    }


    public Directive newDirectivePattern(String effectiveIri, boolean isForward) {
        Objects.requireNonNull(effectiveIri);
        String src = isForward ? "s" : "o";
        String tgt = isForward ? "o" : "s";
        Directive result = newDirectivePattern("?s <" + effectiveIri + "> ?o", src, tgt);
        return result;
    }

    public Directive newDirectivePattern(String pattern, String src, String tgt) {
        Directive p = Directive.newDirective()
                .name("pattern")
                .argument(Argument.newArgument(
                    "of",
                    StringValue.of(pattern))
                    .build())
                .argument(Argument.newArgument(
                    "from",
                    StringValue.of(src))
                    .build())
                .argument(Argument.newArgument(
                    "to",
                    StringValue.of(tgt))
                    .build())
                .build();
        return p;
    }

    @Override
    public TraversalControl visitFragmentSpreadActual(FragmentSpread node, TraverserContext<Node> context) {
        return TraversalControl.CONTINUE;
    }
}
