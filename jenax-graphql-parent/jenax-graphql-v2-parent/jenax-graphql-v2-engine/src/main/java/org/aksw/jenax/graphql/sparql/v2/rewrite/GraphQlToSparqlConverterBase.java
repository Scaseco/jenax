package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilder;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderEdge;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderFragmentBody;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderFragmentHead;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderLiteral;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderLiteralProperty;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderMap;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderObject;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderObjectLikeBase;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderProperty;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderTransitionBase;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderTransitionMatch;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.ArrayMode;
import org.aksw.jenax.graphql.sparql.v2.api2.Connective;
import org.aksw.jenax.graphql.sparql.v2.api2.ElementTransform;
import org.aksw.jenax.graphql.sparql.v2.api2.QueryUtils;
import org.aksw.jenax.graphql.sparql.v2.context.BindDirective;
import org.aksw.jenax.graphql.sparql.v2.context.CardinalityDirective;
import org.aksw.jenax.graphql.sparql.v2.context.ConditionDirective;
import org.aksw.jenax.graphql.sparql.v2.context.FragmentCxt;
import org.aksw.jenax.graphql.sparql.v2.context.FragmentCxtHolder;
import org.aksw.jenax.graphql.sparql.v2.context.GraphDirective;
import org.aksw.jenax.graphql.sparql.v2.context.IndexDirective;
import org.aksw.jenax.graphql.sparql.v2.context.JoinDirective;
import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;
import org.aksw.jenax.graphql.sparql.v2.model.XGraphQlConstants;
import org.aksw.jenax.graphql.sparql.v2.rewrite.Bind.BindingMapper;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNavigator;
import org.aksw.jenax.graphql.sparql.v2.util.ElementUtils;
import org.aksw.jenax.graphql.sparql.v2.util.GraphQlUtils;
import org.aksw.jenax.graphql.sparql.v2.util.Vars;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.vocabulary.RDF;

import graphql.language.Argument;
import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.Value;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;


/**
 * Checks for &#064;pattern directives.
 */
public abstract class GraphQlToSparqlConverterBase<K>
    extends NodeVisitorStub
{
    public static final CardinalityDirective DFT_CARDINALITY = new CardinalityDirective(false, true, false);

    protected Map<String, FragmentDefinition> nameToFragment;
    protected SchemaNavigator schemaNavigator;

    protected RewriteResult<K> rewriteResult;

    public GraphQlToSparqlConverterBase(Map<String, FragmentDefinition> nameToFragment, SchemaNavigator schemaNavigator) {
        super();
        this.nameToFragment = nameToFragment;
        this.schemaNavigator = schemaNavigator;
    }

    public RewriteResult<K> getRewriteResult() {
        return rewriteResult;
    }

    protected abstract K toKey(Field field);
    protected abstract K toKey(org.apache.jena.graph.Node node);

    protected org.apache.jena.graph.Node globalIdToSparql(String globalId) {
        return NodeFactory.createLiteralString(globalId);
    }

    @Override
    public TraversalControl visitField(Field field, TraverserContext<Node> context) {
        TraversalControl result = !context.isVisited()
            ? enterField(field, context)
            : leaveField(field, context);
        return result;
    }

//    public boolean isRoot(TraverserContext<Node> context) {
//        // AggStateBuilder<Binding, FunctionEnv, K, Node> aggStateBuilderTest = context.getVarFromParents(AggStateBuilder.class);
//        Object aggStateBuilderTest = getAggTransition(context);
//        boolean isRoot = aggStateBuilderTest == null;
//        return isRoot;
//    }

    public String readStateId(DirectivesContainer<?> directives) {
        Directive stateIdDirective = GraphQlUtils.expectAtMostOneDirective(directives, "globalId");
        String stateId = GraphQlUtils.getArgAsString(stateIdDirective, "id");
        return stateId;
    }

    public TraversalControl enterQuery(OperationDefinition node, TraverserContext<Node> context) {
        if (!context.isVisited()) {
            RewriteResult<K> rr = context.getVarFromParents(RewriteResult.class);
            // Directive stateIdDirective = GraphQlUtils.expectAtMostOneDirective(node, "globalId");
            String stateId = readStateId(node);
            // org.apache.jena.graph.Node globalIdNode = globalIdToSparql(stateId);


            // An element node with an empty group graph pattern (single binding that does not bind any variables)
            ElementNode rootElementNode = ElementNode.of("root", Connective.newBuilder().element(new ElementGroup()).connectVars().targetVars().build());
            rootElementNode.setIdentifier(stateId);

            AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> aggStateBuilderRoot = new AggStateBuilderObject<>(false);
            rr.root = new GraphQlFieldRewrite<>(rootElementNode, aggStateBuilderRoot, true, node);
        }

        return TraversalControl.CONTINUE;
    }

    public TraversalControl leaveQuery(OperationDefinition node, TraverserContext<Node> context) {
        // Assemble the overall aggregator
//        RewriteResult<Binding, FunctionEnv, K, org.apache.jena.graph.Node> rr = context
//                .getVarFromParents(RewriteResult.class);
//        AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> objectBuilder = new AggStateBuilderObject<>();
//        for (Entry<String, SingleResult<Binding, FunctionEnv, K, org.apache.jena.graph.Node>> entry : rr.map()
//                .entrySet()) {
//
//            AggStateBuilderProperty<Binding, FunctionEnv, K, org.apache.jena.graph.Node> edgeBuilder = AggStateBuilderProperty
//                    .of(globalIdNode);
//            edgeBuilder.setTargetNodeMapper(targetAggBuilder);
//            edgeBuilder.setSingle(tr);

//              if (parentAggBuilder != null) {
//                  AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> tmp = (AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)parentAggBuilder;
//                  tmp.getPropertyMappers().put(key, edgeBuilder);
//              }
//    }
        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitOperationDefinition(OperationDefinition node, TraverserContext<Node> context) {
        TraversalControl result = "QUERY".equals(node.getOperation().name())
            ? !context.isVisited()
                ? enterQuery(node, context)
                : leaveQuery(node, context)
            : super.visitOperationDefinition(node, context);
        return result;
    }

    @Override
    public TraversalControl visitDocument(Document node, TraverserContext<Node> context) {

        if (!context.isVisited()) {
            RewriteResult<K> rr = new RewriteResult<>();
            rr.map =  new LinkedHashMap<>();
            rewriteResult = rr;
            context.setVar(RewriteResult.class, rr);
            context.setAccumulate(rr);
        }
        return TraversalControl.CONTINUE;
    }

    public TraversalControl enterField(Field field, TraverserContext<Node> context) {
        processFieldOnEnter(field, field.getName(), field.getArguments(), field, field.getSelectionSet(), context);
        return TraversalControl.CONTINUE;
    }

    public void processFieldOnEnter(Node<?> node, String nodeName, List<Argument> arguments, DirectivesContainer<?> directives, SelectionSet selectionSet, TraverserContext<Node> context) {

        boolean isRoot = XGraphQlUtils.isRootNode(node, context);

        /* Handle the case where the parent is a fragment spread or inline fragment */

        FragmentCxtHolder fragmentCxtHolder = context.getVarFromParents(FragmentCxtHolder.class);
        FragmentCxt fragmentCxt = fragmentCxtHolder == null ? null : fragmentCxtHolder.cxt();
        boolean isFragmentStart = fragmentCxt != null;

        // Don't pass a received fragment context to the children
        if (isFragmentStart) {
            context.setVar(FragmentCxtHolder.class, new FragmentCxtHolder(null));
        }

        /* Extract state id */

        // Directive stateIdDirective = GraphQlUtils.expectAtMostOneDirective(directives, "globalId");
        // String stateId = GraphQlUtils.getArgAsString(stateIdDirective, "id");
        String stateId = readStateId(directives);
        org.apache.jena.graph.Node globalIdNode = globalIdToSparql(stateId);

        /* Extract pattern, slice and information about how to join with the parent. */

        Directive patternDir = GraphQlUtils.expectAtMostOneDirective(directives, "pattern");
        Connective connective = null;
        if (patternDir != null) {
            try {
                connective = XGraphQlUtils.parsePattern(patternDir, null);
            } catch (Exception e) {
                throw new RuntimeException("At field " + nodeName + ": Failed to process directive: " + patternDir, e);
            }
            // System.out.println(connective);
        }

        Long offset = GraphQlUtils.getArgAsLong(arguments, "offset", null);
        Long limit = GraphQlUtils.getArgAsLong(arguments, "limit", null);

        List<Var> parentVars = null;


        String typeIri = JenaGraphQlUtils.parseRdfIri(nodeName, directives, XGraphQlConstants.type, "iri", "ns", null);
        org.apache.jena.graph.Node typeNode = typeIri == null ? null : NodeFactory.createURI(typeIri);

        if (connective == null) {
            if (typeNode != null) {
                connective = Connective.newBuilder()
                        .connectVars(Vars.s)
                        .targetVars(Vars.s)
                        .element(ElementUtils.createElementTriple(Vars.s, RDF.type.asNode(), typeNode))
                        .build();

            } else {
                connective = Connective.newBuilder()
                        .connectVarNames()
                        .targetVarNames()
                        .element(new ElementGroup())
                        .build();

                parentVars = List.of();
            }
//        	connective = Connective.newBuilder()
//        			.tar$
            // throw new RuntimeException("No pattern for field " + directives);
        } else {
            if (typeNode != null) {
                List<Var> targetVars = connective.getDefaultTargetVars();
                Var targetVar = targetVars.iterator().next();
                if (targetVars.size() != 1) {
                    throw new RuntimeException("@class can only be used when there is a single target variable. Target variables are: " + targetVars);
                }

                Element a = connective.getElement();
                Element b = ElementUtils.createElementTriple(targetVar, RDF.type.asNode(), typeNode);
                Element merged = ElementUtils.flatGroup(a, b);
                connective = Connective.of(
                        connective.getConnectVars(),
                        connective.getDefaultTargetVars(),
                        merged);
            }
        }

        List<Var> connectVars = connective.getConnectVars();

        /* Process @filter */

        // TODO Allow for multiple conditions.
        // TODO Allow positional references to argument values of the field - probably using ?_1 ... ?_n.

        ConditionDirective condition = XGraphQlUtils.parseCondition(directives);
        if (condition != null) {
            // Pattern of @filter connects to the variables of _this_ node's pattern!
            // ElementNode parentEltNode = context.getVarFromParents(ElementNode.class);

            boolean applyFilter = true;

            String whenExprStr = condition.whenExprStr();
            Map<Var, org.apache.jena.graph.Node> argMap = new HashMap<>();
            if (whenExprStr != null) {
                Expr whenExpr = ExprUtils.parse(whenExprStr);
                // When expressions can only refer to arguments.
                Set<Var> whenExprVars = whenExpr.getVarsMentioned();
                BindingBuilder bb = BindingFactory.builder();
                for (Var v : whenExprVars) {
                    String argName = v.getName();
                    Argument arg = arguments.stream().filter(a -> a.getName().equals(argName)).findFirst().orElseThrow(() -> new RuntimeException("No such argument: " + argName));
                    org.apache.jena.graph.Node n = GraphQlUtils.toNodeValue(arg.getValue()).asNode();
                    bb.add(v, n);
                    argMap.put(v, n);
                }
                Binding b = bb.build();
                NodeValue nv = ExprLib.evalOrNull(whenExpr, b, null);
                applyFilter = XSDFuncOp.booleanEffectiveValue(nv);
            }

            // TODO The @filter expr can originate from the schema in order to filter by the type's
            //   associated pattern. This shouldn't end up here.
            if (condition.byExprStr() == null) {
                applyFilter = false;
            }

            if (applyFilter) {
                Connective filterConnective = filterToConnective(connective.getConnectVars(), condition, argMap);
                List<Var> filterVars = filterConnective.getConnectVars();
                List<Var> patternVars = connective.getDefaultTargetVars();

                // Is there anything to transform for filters??!!
                // So filters affect the 'to' vars - this means that a var mapping thisVars->parentVars needs to be established.
                // xconnective.a
                if (patternVars.size() != filterVars.size()) {
                    throw new RuntimeException("Filter variables do not connect to the pattern: filterVars: " + filterVars + " patternVars: " + patternVars);
                }

                // Map filter variables to the pattern.
                // Prefix filter variables by the current path.
                // VarUtils.createJoinVarMap(filterConnective.getVisibleVars(), connective.getVisibleVars(), filterVars, patternVars, VarGeneratorImpl2.create());
                Map<Var, org.apache.jena.graph.Node> varMap = new HashMap<>();
                for (Var v : filterConnective.getMentionedVars()) {
                    String varName = v.getName();
                    if (varName.matches("^[0-9]+$")) {
                        int argIdx = Integer.parseInt(varName.substring(1));
                        Argument arg = arguments.get(argIdx);
                        Value value = arg.getValue();
                        org.apache.jena.graph.Node n = GraphQlUtils.toNodeValue(value).asNode();
                        varMap.put(v, n);

                    } else {
                        varMap.put(v, Var.alloc(stateId + "_" + v.getVarName()));
                    }
                }

                for (int i = 0; i < filterVars.size(); ++i) {
                    Var filterVar = filterVars.get(i);
                    Var patternVar = patternVars.get(i);
                    varMap.put(filterVar, patternVar);
                }

                Connective fConn = filterConnective.applyNodeTransform(new NodeTransformSubst(varMap));

                Element a = connective.getElement();
                Element b = fConn.getElement();
                Element merged = ElementUtils.flatGroup(a, b);
                connective = Connective.of(
                    connective.getConnectVars(),
                    connective.getDefaultTargetVars(),
                    merged);

                // So one question is: Is a filter always a leaf? Does a filter never introduce new variables that can be filtered again?
                // I think the answer is yes.
                // System.err.println("Got filter: " + connective);
            }
        }

        /* Process @join */

        // Read the parentVars directive (connect(to: ['foo'])
        // List<Var> childVars = null;

        JoinDirective joinDirective = XGraphQlUtils.parseJoin(directives);

        if (joinDirective != null) {
            if (joinDirective.parentVars() != null) {
                parentVars = Var.varList(joinDirective.parentVars());
            }

            if (joinDirective.thisVars() != null) {
                connectVars = Var.varList(joinDirective.thisVars());
            }
        }

        /* Process @class - merges another pattern on the single target variable */
        // May be superseded by a more general approach
//        Directive classDirective = GraphQlUtils.expectAtMostOneDirective(directives, XGraphQlConstants.type);
//        if (classDirective != null) {
//            String typeIri = GraphQlUtils.getArgAsString(classDirective, "iri");
//            org.apache.jena.graph.Node typeNode = NodeFactory.createURI(typeIri);
//
//            List<Var> targetVars = connective.getDefaultTargetVars();
//            Var targetVar = targetVars.iterator().next();
//            if (targetVars.size() != 1) {
//                throw new RuntimeException("@class can only be used when there is a single target variable. Target variables are: " + targetVars);
//            }
//
//            Element a = connective.getElement();
//            Element b = ElementUtils.createElementTriple(targetVar, RDF.type.asNode(), typeNode);
//            Element merged = ElementUtils.flatGroup(a, b);
//            connective = Connective.of(
//                    connective.getConnectVars(),
//                    connective.getDefaultTargetVars(),
//                    merged);
//        }


        /* Process @index */

        AggStateBuilderEdge<Binding, FunctionEnv, K, org.apache.jena.graph.Node> edgeBuilder = null;
        AggStateBuilderTransitionMatch<Binding, FunctionEnv, K, org.apache.jena.graph.Node> transitionBuilder = null;

        IndexDirective index = XGraphQlUtils.parseIndex(directives);
        boolean isIndexField = index != null;
        if (index != null) {
            // TODO If the graph pattern is DISTINCT on the index variable(s) then omit ORDER BY.

            // List<Var> vars = Var.varList(index.indexExprs());
            // List<Expr> exprs = vars.stream().map(ExprLib::nodeToExpr).toList();
            // List<Expr> exprs = index.indexExprs().stream().map(ExprUtils::parse).toList();
            String keyExprStr = index.keyExpr();
            if (keyExprStr == null) {
                throw new RuntimeException("@index(by: \" ... required argument missing ... \")");
            }
            Expr keyExpr = ExprUtils.parse(index.keyExpr());
            List<Expr> sortExprs = keyExpr.getVarsMentioned().stream().map(ExprVar::new).map(x -> (Expr)x).toList();

            Set<Var> visibleVars = connective.getVisibleVars();
            Element before = connective.getElement();
            Query query = QueryUtils.elementToQuery(before);
            Element after = new ElementSubQuery(query);

            // TODO Validate vars
            // Set<Var> exprVars = ...

            for (Expr sortExpr : sortExprs) {
                query.addOrderBy(sortExpr, Query.ORDER_ASCENDING);
            }

            // TODO If there are multiple variables we need an output expression
            // Conversely, if there is only an output expression then use that
//            if (exprs.size() != 1) {
//                throw new RuntimeException("@index(by: ...): Currently only one variable/expression supported.");
//            }

            connective = Connective.newBuilder()
                    .element(after)
                    .connectVars(connectVars)
                    .targetVars(connective.getDefaultTargetVars())
                    .build();

            // Expr indexExpr = exprs.get(0);
            BiFunction<Binding, FunctionEnv, K> indexMapper = (input, env) -> {
                NodeValue nv = keyExpr.eval(input, env);
                org.apache.jena.graph.Node n = nv.asNode();
                K k = toKey(n);
                return k;
            };

            Expr oneIfExpr = index.oneIf() == null ? NodeValue.FALSE : ExprUtils.parse(index.oneIf());
            BiPredicate<Binding, FunctionEnv> oneIfTest = (input, env) -> {
                NodeValue nv = oneIfExpr.eval(input, env);
                // org.apache.jena.graph.Node n = nv.asNode();
                boolean r = NodeValue.TRUE.equals(nv);
                return r;
            };

            edgeBuilder = new AggStateBuilderMap<>(globalIdNode, indexMapper, oneIfTest);
            transitionBuilder = edgeBuilder;
        }
        List<Var> targetVars = connective.getDefaultTargetVars();

        ElementNode elementNode = ElementNode.of(connective);
        ElementNode localRoot = elementNode;
        elementNode.setIdentifier(stateId);

        elementNode.setLimit(limit);
        elementNode.setOffset(offset);


        ElementNode parentNode = context.getVarFromParents(ElementNode.class);


        AggStateBuilderTransitionBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node> parentAggBuilder = context.getVarFromParents(AggStateBuilderTransitionBase.class);

        // Wire up the pattern of this node to the parent
        context.setVar(ElementNode.class, elementNode);

        boolean isPatternFree = connective.isEmpty();

        if (isPatternFree) {
            // System.err.println("Pattern free");
            List<Var> parentTargetVars = parentNode == null ? null : parentNode.getEffectiveTargetVars();
            elementNode.setLocalTargetVars(parentTargetVars);
        }

        if (isFragmentStart) {
            // FIXME Set up the join properly
            // parentNode.addChild(parentVars, elementNode, elementNode.getConnective().getConnectVars());
            parentNode.addChild(parentVars, elementNode, connectVars);
        } else {
            if (parentNode != null) {
                parentNode.addChild(parentVars, elementNode, connectVars);
            }
        }

        /* Process @to and @from */

        boolean isBindValue = false;

        if (directives.hasDirective("to")) {
            List<Var> parentTargetVars = elementNode.getParentLink().parent().getEffectiveTargetVars();
            targetVars = parentTargetVars.stream().map(parentTargetVar -> {
                Var localVar = processBind(node, elementNode, null, new ExprVar(parentTargetVar));
                return localVar;
            }).toList();
            elementNode.setLocalTargetVars(targetVars);
            isBindValue = true;
        }

        /* Process @bind */

        for (Directive directive : directives.getDirectives("bind")) {
            BindDirective bind = BindDirective.PARSER.parser(directive);
            Expr expr = bind.parseExpr();
            Var bindVar = processBind(node, elementNode, bind.varName(), expr);

            // If there is no pattern then use a single bind expression as the target
            if (connective.isEmpty() || Boolean.TRUE.equals(bind.isTarget())) {
                targetVars = List.of(bindVar);
                elementNode.setLocalTargetVars(targetVars);
                // elementNode.setLocalTargetVars(List.of(bindVar));
                isBindValue = true;
            }
        }

        /* Process @array */

        // If the parent is an array then all immediate fields do not produce entries but only values (i.e. the keys are omitted).
        // But still, each sub-property must have its corresponding state!
        AggStateBuilder<Binding, FunctionEnv, K, org.apache.jena.graph.Node> tmpMapper = parentAggBuilder == null ? null : parentAggBuilder.getTargetNodeMapper();
        boolean isParentFieldArray = tmpMapper instanceof AggStateBuilderObject abo && abo.isArray();

        boolean isArray = directives.hasDirective("array");
        ArrayMode arrayMode = isParentFieldArray ? ArrayMode.FLAT : ArrayMode.OFF;
        if (ArrayMode.FLAT.equals(arrayMode) && isArray) {
            arrayMode = ArrayMode.NESTED;
        }

        /* Further processing */

        if (isRoot) {
            RewriteResult<K> rewriteResult = context.getVarFromParents(RewriteResult.class);
            rewriteResult.root.rootElementNode().addChild(List.of(), elementNode, List.of());
        }

        CardinalityDirective cardinality = processCardinality(directives, context);

        GraphDirective graph = processGraph(directives, context);
        if (graph != null) {
            if (!graph.isSelf() && graph.isCascade()) {
                throw new RuntimeException("@graph(self: false, cascade: true) must be handled by a preprocessor that pushes the directive to each child.");
            }

            ElementTransform elementTransform = convert(graph);
            if (graph.isCascade()) {
                localRoot.addTreeTransform(elementTransform);
            } else {
                localRoot.addLocalTransform(elementTransform);
            }
        }

        List<Selection> subSelections = selectionSet == null ? Collections.emptyList() : selectionSet.getSelectionsOfType(Selection.class);

        boolean isLeafField = subSelections.isEmpty();

        // AggStateBuilder<Binding, FunctionEnv, K, org.apache.jena.graph.Node> parentAggBuilder = context.getVarFromParents(AggStateBuilder.class);

        boolean isSingle = cardinality.isOne();

        CardinalityDirective thisCardinality = context.getVar(CardinalityDirective.class);
        if (isPatternFree || isBindValue) {
            isSingle = thisCardinality == null ? true : thisCardinality.isOne();
        }


        boolean skipIfNull = directives.hasDirective("skipIfNull");

        // Parse the key
        K key = null;
        if (node instanceof Field f) {
            // key = fieldToKey.apply(f);
            key = toKey(f);
        }

        AggStateBuilder<Binding, FunctionEnv, K, org.apache.jena.graph.Node> targetAggBuilder = null;
        if (!isLeafField) {
            AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> objectBuilder = new AggStateBuilderObject<>(isArray);
            targetAggBuilder = objectBuilder;
        }


//        if (isLeafField) {
//            BindingMapper<org.apache.jena.graph.Node> mapper = targetVars.size() == 1
//                ? Bind.var(targetVars.get(0))
//                : Bind.vars(targetVars);
//
//            targetAggBuilder = AggStateBuilderLiteral.of(mapper);
//        } else {
//            AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> objectBuilder = new AggStateBuilderObject<>();
//            targetAggBuilder = objectBuilder;
//        }

        // Whether this field produces an edge
        boolean isEdgeField;
        if (isRoot) { // TODO Implement rules
//        	if (isLeafField) {
//
//        	}

            isEdgeField = true;
        } else {
            isEdgeField = true;
        }

        if (isEdgeField) {
            if (isIndexField) {
                if (isLeafField) {
                    BindingMapper<org.apache.jena.graph.Node> mapper = targetVars.size() == 1
                            ? Bind.var(targetVars.get(0))
                            : Bind.vars(targetVars);

                    // AggStateBuilderLiteral<Binding, FunctionEnv, K, org.apache.jena.graph.Node> propertyBuilder;
                    targetAggBuilder = AggStateBuilderLiteral.of(mapper); //, key, isSingle, mapper);

                    setAggResult(context, targetAggBuilder);
                }

                edgeBuilder.setTargetBuilder(targetAggBuilder);
                // Nothing more to do here
            } else {
                if (key == null) {
                    throw new RuntimeException("Failed to obtain a key for field: " + nodeName);
                }

                if (isLeafField) {
                    Objects.requireNonNull(key);

                    BindingMapper<org.apache.jena.graph.Node> mapper = targetVars.size() == 1
                            ? Bind.var(targetVars.get(0))
                            : Bind.vars(targetVars);

                    AggStateBuilderLiteralProperty<Binding, FunctionEnv, K, org.apache.jena.graph.Node> propertyBuilder;
                    propertyBuilder = AggStateBuilderLiteralProperty.of(globalIdNode, key, isSingle, skipIfNull, arrayMode, mapper);
                    transitionBuilder = propertyBuilder;
                    setAggResult(context, propertyBuilder);
                } else {
                    AggStateBuilderProperty<Binding, FunctionEnv, K, org.apache.jena.graph.Node> propertyBuilder;
                    propertyBuilder = AggStateBuilderProperty.of(globalIdNode, key, arrayMode);
                    propertyBuilder.setTargetBuilder(targetAggBuilder);
                    propertyBuilder.setSingle(isSingle);
                    transitionBuilder = propertyBuilder;
                    edgeBuilder = propertyBuilder;
                }
            }

            if (parentAggBuilder != null) {
                AggStateBuilderObjectLikeBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node> tmp = (AggStateBuilderObjectLikeBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)parentAggBuilder.getTargetNodeMapper();
                // AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> tmp = (AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)parentAggBuilder;
                tmp.addMember(transitionBuilder);
            }

            if (edgeBuilder != null) {
                setAggTransition(context, edgeBuilder);
                // context.setVar(AggStateBuilderTransitionBase.class, edgeBuilder);
            }
        }

        // return TraversalControl.CONTINUE;
    }

    private Var processBind(Node<?> node, ElementNode elementNode, String bindVarName, Expr expr) {
        // Expr expr = bind.parseExpr();
        // Check whether the referenced variables exist
        Set<Var> vars = expr.getVarsMentioned();
        for (Var var : vars) {
            ElementNode varSource = elementNode.findVarInAncestors(var);
            if (varSource == null) {
                throw new RuntimeException("Could not resolve variable " + var + " at " + node);
            }
        }

        // String bindVarName = bind.varName();
        if (bindVarName == null) {
            // TODO Properly allocate name to avoid clash
            bindVarName = "bindvar_" + (elementNode.getBinds().size() + 1);
        }
        Var bindVar = Var.alloc(bindVarName);
        elementNode.getBinds().add(bindVar, expr);
        return bindVar;
    }

    public TraversalControl leaveField(Field field, TraverserContext<Node> context) {
        boolean isRoot = XGraphQlUtils.isRootNode(field, context);

        if (isRoot) {
            // Get or create the overall rewrite result
            RewriteResult<K> rewriteResult = context.getVarFromParents(RewriteResult.class);

            // Finalize this node's aggregator
            // AggStateBuilder<Binding, FunctionEnv, K, org.apache.jena.graph.Node> aggStateBuilder = context.getVar(AggStateBuilder.class);
            // AggStateBuilderTransitionBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node> transBuilder = context.getVar(AggStateBuilderTransitionBase.class);
            AggStateBuilderTransitionBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node> transBuilder = getAggTransition(context);

            AggStateBuilderEdge<Binding, FunctionEnv, K, org.apache.jena.graph.Node> edgeBuilder = transBuilder instanceof AggStateBuilderEdge eb
                    ? (AggStateBuilderEdge<Binding, FunctionEnv, K, org.apache.jena.graph.Node>) eb
                    : null;

            // FIXME can be AggStateBulider{Property,Map} - need to handle map case.
            // AggStateBuilderProperty<Binding, FunctionEnv, K, org.apache.jena.graph.Node> edgeBuilder = (AggStateBuilderProperty<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)aggStateBuilder;
            AggStateBuilder<Binding, FunctionEnv, K, org.apache.jena.graph.Node> targetMapper = edgeBuilder == null ? null : edgeBuilder.getTargetNodeMapper();



            ElementNode elementNode = context.getVar(ElementNode.class);

            // FIXME The logic for isSingle seems broken
            boolean isSingle = edgeBuilder != null
                    ? edgeBuilder.isHidden()
                    : true;
                    // edgeBuilder.isHidden();
            // boolean isSingle = processCardinality(field, context).isOne();


            AggStateBuilderTransitionMatch<Binding, FunctionEnv, K, org.apache.jena.graph.Node> matchBuilder = edgeBuilder != null
                    ? edgeBuilder
                    : (AggStateBuilderTransitionMatch<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)getAggResult(context);

            if (targetMapper != null) {
                GraphQlFieldRewrite<K> singleResult = new GraphQlFieldRewrite<>(elementNode, targetMapper, isSingle, field);
                rewriteResult.map.put(field.getName(), singleResult);
            }

            AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> objBuilder = (AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)rewriteResult.root().rootAggBuilder();
            K key = toKey(field);
            Objects.requireNonNull(key);
            Objects.requireNonNull(matchBuilder);
            // if (edgeBuilder != null) {
            objBuilder.addMember(matchBuilder);
        }

        return TraversalControl.CONTINUE;
    }


    /**
     * The main conceptual difference between conventional fields and inline fragments is
     * in the latter case the inline fragment overrides the join for the children.
     * The condition can access all variables declared in the parent.
     *
     * <ul>
     *   <li>Creates a separate ElementNode with the condition.</li>
     *   <li>Inherits all variables of the parent</li>
     * </ul>
     */
    @Override
    public TraversalControl visitInlineFragment(InlineFragment node, TraverserContext<Node> context) {
        if (!context.isVisited()) {
            String stateId = readStateId(node);
            processInlineFragment(node, context, stateId);
            // parentAggStateBuilder.setTargetNodeMapper(parentAggStateBuilder);
            // rr.root = new SingleResult<>(rootElementNode, aggStateBuilderRoot, true);
        }
        return super.visitInlineFragment(node, context);
    }

    private void processInlineFragment(InlineFragment node, TraverserContext<Node> context, String stateId) {
        org.apache.jena.graph.Node globalIdNode = globalIdToSparql(stateId);

        ConditionDirective condition = XGraphQlUtils.parseCondition(node);

        // FIXME Allow no condition.
        Objects.requireNonNull(condition.byExprStr(), "@filter(if: 'required key absent')");

        ElementNode parentEltNode = context.getVarFromParents(ElementNode.class);

        // The filter of the inline fragment affects the connective's target vars.
        Connective connective = filterToConnective(parentEltNode.getConnective().getDefaultTargetVars(), condition, null);
        List<Var> parentVars = connective.getConnectVars();
        List<Var> thisVars = connective.getDefaultTargetVars();

        ElementNode thisElementNode = ElementNode.of(connective);
        thisElementNode.setIdentifier(stateId);
        parentEltNode.addChild(parentVars, thisElementNode, thisVars);

        context.setVar(ElementNode.class, thisElementNode);

        FragmentCxt cxt = new FragmentCxt(node, thisVars);
        context.setVar(FragmentCxtHolder.class, new FragmentCxtHolder(cxt));

        AggStateBuilderTransitionBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node> parentAggBuilder = context.getVarFromParents(AggStateBuilderTransitionBase.class);
        // parentTarget = parentTransition.getTargetNodeMapper();


        AggStateBuilderFragmentHead<Binding, FunctionEnv, K, org.apache.jena.graph.Node> aggStateBuilder = new AggStateBuilderFragmentHead<>(globalIdNode);
        aggStateBuilder.setTargetBuilder(new AggStateBuilderFragmentBody<>());

        setAggTransition(context, aggStateBuilder);
        // context.setVar(AggStateBuilderTransitionBase.class, aggStateBuilder);

        if (parentAggBuilder != null) {
            AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> tmp = (AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)parentAggBuilder.getTargetNodeMapper();
            // AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> tmp = (AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)parentAggBuilder;
            tmp.addMember(aggStateBuilder);
        }
    }

    protected static Connective filterToConnective(List<Var> connectVars, ConditionDirective condition, Map<Var, org.apache.jena.graph.Node> binding) {
        Expr byExpr = null;
        Set<Var> byExprVars = null;
        List<Var> parentVars = null;
        List<Var> thisVars = null;
        Connective connective = null;
        if (condition != null) {
            byExpr = ExprUtils.parse(condition.byExprStr());

            if (binding != null && !binding.isEmpty()) {
                byExpr = byExpr.applyNodeTransform(new NodeTransformSubst(binding));
            }

            byExprVars = byExpr.getVarsMentioned();

            List<String> parentVarNames = condition.parentVars();
            if (parentVarNames != null) {
                parentVars = Var.varList(parentVarNames);
            }

            List<String> thisVarNames = condition.thisVars();
            if (thisVarNames != null) {
                thisVars = Var.varList(thisVarNames);
            }
        }

//            if (expr == null) {
//            	expr = NodeValue.TRUE;
//            }

        if (parentVars == null) {
            parentVars = connectVars; // parentEltNode.getConnective().getDefaultTargetVars();
        }

        if (byExprVars != null) {
            // TODO If there is more than one variable then the order must be given!
            if (thisVars == null) {
                if (byExprVars.size() == 1) {
                    thisVars = List.of(byExprVars.iterator().next());
                } else {
                    throw new RuntimeException("@filter(this: [varlist]) must be given if an expression has more than 1 variable");
                }
            }
        }

        if (byExpr != null) {
            connective = Connective.newBuilder()
                    .element(new ElementFilter(byExpr))
                    .connectVars(parentVars)
                    .targetVars(thisVars)
                    .build();
        } else {
             connective = Connective.newBuilder()
                    .element(new ElementBind(Vars.z, new ExprVar(Vars.x)))
                    .connectVars(Vars.x)
                    .targetVars(Vars.z)
                    .build();
        }

        return connective;
    }

    /* At each field (node?) there is the result aggregator builder and for inner nodes also the aggregator builder for the transition.
     * Often they are the same object, but for example leaf nodes only have an aggregator builder for the result but none for the transition. */

    protected void setAggTransition(TraverserContext<Node> context, AggStateBuilderTransitionBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node> aggStateBuilder) {
        context.setVar(AggStateBuilderTransitionBase.class, aggStateBuilder);
    }

    protected AggStateBuilderTransitionBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node> getAggTransition(TraverserContext<Node> context) {
        AggStateBuilderTransitionBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node> result = context.getVar(AggStateBuilderTransitionBase.class);
        return result;
    }

    protected void setAggResult(TraverserContext<Node> context, AggStateBuilder<Binding, FunctionEnv, K, org.apache.jena.graph.Node> aggStateBuilder) {
        context.setVar(AggStateBuilder.class, aggStateBuilder);
    }

    protected AggStateBuilder<Binding, FunctionEnv, K, org.apache.jena.graph.Node> getAggResult(TraverserContext<Node> context) {
        AggStateBuilder<Binding, FunctionEnv, K, org.apache.jena.graph.Node> result = context.getVar(AggStateBuilder.class);
        return result;
    }


    @Override
    public TraversalControl visitFragmentSpread(FragmentSpread node, TraverserContext<Node> context) {
////        private final String name;
////        private final ImmutableList<Directive> directives;
//
//
//        // Join the fragment on these variables
//        String fragmentName = node.getName();
//
//        FragmentDefinition fragment = nameToFragment.get(fragmentName);
//        if (fragment == null) {
//            throw new RuntimeException("No fragment with " + fragmentName);
//        }
//
//
//
//        // The condition is in a lateral block that joins with the parent
//        // Joining variables of the parent need to be passed through to the child.
//        // parent LATERAL { BIND("sX" AS ?stateId) FILTER(condition) . LATERAL { ... } }
//
//        // Create a dummy bind with the condition? Filter exists?
//        // Inject to parent or child element? - Needs to be parent - e.g. parent resource must be an Android.
//        //
//
//
//        fragment.getTypeCondition();
//        fragment.getSelectionSet();
//        fragment.getDirectives();
//
//        //fragment.get



        return TraversalControl.CONTINUE;
    }

    /** Get the effective cardinality; publish cascading cardinalities to the context. */
    public static CardinalityDirective processCardinality(DirectivesContainer<?> directives, TraverserContext<Node> context) {
        CardinalityDirective cardinality = null;
        // XXX We currently need to reparse the cardinality on the post order walk
        cardinality = XGraphQlUtils.parseCardinality(directives);
        if (!context.isVisited()) {
            if (cardinality != null) {
                if (cardinality.isCascade()) {
                    context.setVar(CardinalityDirective.class, cardinality);
                }
            }
        }
//        } else {
//            cardinality = context.getVar(CardinalityDirective.class);
//        }

        // If the cardinality does not affect self then invalidate it again
        if (cardinality != null && !cardinality.isSelf()) {
            cardinality = null;
        }

        if (cardinality == null) {
            cardinality = context.getVarFromParents(CardinalityDirective.class);
        }

        if (cardinality == null) {
            cardinality = DFT_CARDINALITY;
        }
        return cardinality;
    }

    /** Returns a graph directive present on the current field.
     *  Also registers such present directive at the context for reference.
     *  Note: Only directives present on the current field are relevant for processing
     *  because only from those are ElementTransforms created. */
    public static GraphDirective processGraph(DirectivesContainer<?> field, TraverserContext<Node> context) {
        GraphDirective graphDirective = null;
        if (!context.isVisited()) {
            graphDirective = XGraphQlUtils.parseGraph(field);
            if (graphDirective != null) {
                context.setVar(GraphDirective.class, graphDirective);
            }
        }
        return graphDirective;
    }


    /** Create an ElementTransform according to the specification of a GraphDirective instance. */
    public static ElementTransform convert(GraphDirective graph) {
        ElementTransform result;
        String varName = graph.getVarName();
        List<String> iris = graph.getGraphIris();
        if (varName == null && iris != null && iris.size() == 1) {
            org.apache.jena.graph.Node node = NodeFactory.createURI(iris.get(0));
            result = new ElementTransformGraph(node);
        } else {
            // FIXME Allocate a fresh var!
            result = new ElementTransformGraph(Var.alloc("x"));
        }
        return result;
    }


// Handling nulls with injecting an inline fragment is bad, because we have to repeat the pattern that leads to the potential null value
//    boolean skipIfNull = directives.hasDirective("skipIfNull");
//    if (skipIfNull) {
//        // Inject an inline fragment
//        // ElementNode tmp = ElementNode.of(connective)
//        Expr notNullCondition = targetVars.stream()
//                .map(v -> (Expr)new E_Bound(new ExprVar(v)))
//                .reduce((a, b) -> (Expr)new E_LogicalAnd(a, b))
//                .orElse(NodeValue.TRUE);
//        // targetVars
//        Connective notNull = Connective.newBuilder()
//                .element(new ElementFilter(notNullCondition))
//                .connectVars(parentVars)
//                .targetVars(parentVars)
//                .build();
//        ElementNode conditionNode = ElementNode.of(notNull);
//        String conditionStateId = stateId + "_notNull";
//        org.apache.jena.graph.Node conditionStateIdNode = globalIdToSparql(conditionStateId);
//        conditionNode.setIdentifier(conditionStateId);
//        parentNode.addChild(conditionNode);
//        parentNode = conditionNode;
//
//        AggStateBuilderFragmentHead<Binding, FunctionEnv, K, org.apache.jena.graph.Node> fragmentBuilder = new AggStateBuilderFragmentHead<>(conditionStateIdNode);
//        fragmentBuilder.setTargetBuilder(new AggStateBuilderFragmentBody<>());
//
//        if (parentAggBuilder != null) {
//            AggStateBuilderObjectLikeBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node> tmp = (AggStateBuilderObjectLikeBase<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)parentAggBuilder.getTargetNodeMapper();
//            // AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> tmp = (AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node>)parentAggBuilder;
//            tmp.getPropertyMappers().add(fragmentBuilder);
//        }
//
//        parentAggBuilder = fragmentBuilder;
//        elementNode = conditionNode;
//        localRoot = conditionNode;
//    }

}
