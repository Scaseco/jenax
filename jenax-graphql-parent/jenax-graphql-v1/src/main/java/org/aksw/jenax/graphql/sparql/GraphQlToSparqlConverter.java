package org.aksw.jenax.graphql.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.util.node.PathUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.impl.NodeQueryImpl;
import org.aksw.jenax.io.json.graph.GraphToJsonMapperNode;
import org.aksw.jenax.io.json.graph.GraphToJsonNodeMapperFragmentBody;
import org.aksw.jenax.io.json.graph.GraphToJsonNodeMapperFragmentHead;
import org.aksw.jenax.io.json.graph.GraphToJsonNodeMapperLiteral;
import org.aksw.jenax.io.json.graph.GraphToJsonNodeMapperObject;
import org.aksw.jenax.io.json.graph.GraphToJsonNodeMapperObjectLike;
import org.aksw.jenax.io.json.graph.GraphToJsonPropertyMapper;
import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.MappedFragment;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.aksw.jenax.stmt.core.SparqlParserConfig;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Document;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.TypeName;
import graphql.language.Value;

/**
 * Compiles a graphql query to a {@link GraphQlToSparqlMapping} instance. All
 * fields can be qualified with IRIs of properties and classes. Names of
 * unqualified fields are passed to a {@link GraphQlResolver} for resolution to
 * IRIs. All resolver methods are allowed to throw
 * {@link UnsupportedOperationException} to indicate absence of resolution
 * support.
 */
public class GraphQlToSparqlConverter {
    private static final Logger logger = LoggerFactory.getLogger(GraphQlToSparqlConverter.class);

    protected GraphQlResolver resolver;

    /** false -&gt; rdf mode, true -&gt; json mode*/
    protected boolean jsonMode;


//    public GraphQlToSparqlConverter(GraphQlResolver resolver) {
//        this(resolver, false);
//    }

    public GraphQlToSparqlConverter(GraphQlResolver resolver, boolean jsonMode) {
        super();
        this.resolver = resolver;
        this.jsonMode = jsonMode;
    }

    public GraphQlToSparqlMapping convertDocument(Document document, Map<String, Value<?>> assignments) {
        GraphQlToSparqlMapping result = new GraphQlToSparqlMapping(document);
        new GraphQlQueryWorker(document, assignments).convertDocument(result);
        return result;
    }

//    public GraphQlToSparqlMapping convertDocument(Document document) {
//        Map<String, FragmentDefinition> fragmentsByName = new LinkedHashMap<>();
//
//        for (graphql.language.Node child : document.getChildren()) {
//            if (child instanceof FragmentDefinition fragmentDefinition) {
//
//            } else if (child instanceof OperationDefinition operationDefinition) {
//
//            }
//        }
//
//        GraphQlToSparqlMapping result = new GraphQlToSparqlMapping(document);
//        OperationDefinition op = document.getFirstDefinitionOfType(OperationDefinition.class).orElse(null);
//        if (op != null) {
//            SelectionSet selectionSet = op.getSelectionSet();
//
//            // System.err.println("Field index: " + fieldIndex);
//
//            new GraphQlQueryWorker(document).convertTopLevelFields(selectionSet, result);
//        }
//
//        return result;
//    }

//    public static TreeDataMap<Path<String>, Field> indexFields(Document document) {
//        TreeDataMap<Path<String>, Field> result = new TreeDataMap<>();
//        Path<String> path = PathStr.newAbsolutePath();
//        OperationDefinition op = document.getFirstDefinitionOfType(OperationDefinition.class).orElse(null);
//        if (op != null) {
//            SelectionSet selectionSet = op.getSelectionSet();
//            indexFields(result, path, selectionSet);
//        }
//        return result;
//    }

    public class GraphQlQueryWorker {

        protected Document document;
        protected Map<String, Value<?>> assignments;

        /** The currently encountered fragments (definitions are processed in order) */
        protected Map<String, FragmentDefinition> fragmentsByName;

        public GraphQlQueryWorker(Document document, Map<String, Value<?>> assignments) {
            this.document = Objects.requireNonNull(document);
            this.fragmentsByName = new LinkedHashMap<>();
            this.assignments = assignments != null ? assignments : Collections.emptyMap();
        }

        public void convertDocument(GraphQlToSparqlMapping result) {
            for (graphql.language.Node child : document.getChildren()) {
                if (child instanceof FragmentDefinition fragmentDefinition) {
                    fragmentsByName.put(fragmentDefinition.getName(), fragmentDefinition);
                } else if (child instanceof OperationDefinition operationDefinition) {
                    SelectionSet selectionSet = operationDefinition.getSelectionSet();
                    convertTopLevelFields(selectionSet, result);
                } else {
                    // logger.warn("Unknown element: );
                    throw new RuntimeException("Unknown element: " + child);
                }
            }
        }

        /**
         * The context keeps track of which information (e.g. namespaces) currently
         * applies
         */
        // protected Context context = new Context(null, null);
        protected Stack<Context> contextStack = new Stack<>();
        // protected TreeDataMap<Path<String>, Field> fieldIndex;

        public void convertTopLevelFields(SelectionSet selectionSet, GraphQlToSparqlMapping result) {
            contextStack.push(new Context(null, null));
            // Index the field tree and create a global map of field names thereby
            // considering
            // aliases declared on fields using such as { foo (as: "bar") }
            // fieldIndex = GraphQlUtils.indexFields(selectionSet);
            // context = new Context(null, selectionSet);

            // RdfToJsonNodeMapperObject nodeConverter = null;
            for (Field field : selectionSet.getSelectionsOfType(Field.class)) {
                convertTopLevelField(field, result);
            }
        }

        public void convertTopLevelField(Field field, GraphQlToSparqlMapping result) {
            Context context = contextStack.peek().newChildContext(field);
            setupContext(context);
            contextStack.push(context);

            NodeQuery nodeQuery = NodeQueryImpl.newRoot();
            context.setNodeQuery(nodeQuery);

            // Relation filterRelation = tryParseSparqlQuery(context, field);

//            if (filterRelation != null) {
//                // TODO Error message if not an unary relation
//                nodeQuery.setFilterRelation(filterRelation.toUnaryRelation());
//            }

            // The map of aggregators
            // Map<String, PropertyConverter> converters = new LinkedHashMap<>();
            Multimap<String, Value<?>> args = GraphQlUtils.indexArguments(field);

            String fieldName = field.getName();

            // String fieldIri = deriveFieldIri(context, fieldName);

            // Map<String, List<Directive>> directives = field.getDirectivesByName();

            if (logger.isDebugEnabled()) {
                logger.debug("Seen top level field: " + fieldName);
            }

            // Node classNode = resolveClass(context, fieldName);
            // NodeQuery fieldQuery = resolveKeyToClasses(nodeQuery, classNode);
            NodeQuery fieldQuery = nodeQuery;

            SelectionSet subSelection = field.getSelectionSet();
            GraphToJsonMapperNode nodeMapper = convertInnerSelectionSet(subSelection, nodeQuery);

            // Handle arguments of the field, such as slice, filter and orderBy
            if (fieldQuery != null) {
                tryApplySlice(nodeQuery, args, assignments);
                tryApplyOrderBy(nodeQuery, args);

                if (false) { // The idea was to allow to filter fields - but this codeblock is broken
                    for (Argument arg : field.getArguments()) {
                        String argName = arg.getName();

                        FacetPath facetPath = resolveProperty(context, argName);
                        NodeQuery argQuery = facetPath == null ? null : fieldQuery.resolve(facetPath);

                        Value<?> rawV = arg.getValue();
                        if (rawV instanceof StringValue) {
                            StringValue v = (StringValue) rawV;
                            String str = v.getValue();

                            org.apache.jena.graph.Node TO_STRING = NodeFactory.createURI("fn:" + XSD.xstring.getURI());
                            argQuery.constraints().fwd(TO_STRING).enterConstraints().eq(NodeFactory.createLiteral(str))
                                    .activate().leaveConstraints();
                        }
                    }
                }
            }

            processSparqlDirectives(context, resolver);

            // Materialize prefixes
            PrefixMap prefixMap = PrefixMapFactory.create(context.getFinalPrefixMap());
            boolean isSingle = Cardinality.ONE.equals(context.getThisCardinality());
            result.addEntry(field, prefixMap, nodeQuery, nodeMapper, isSingle);

            // context = context.getParent();
            contextStack.pop();
        }

        public GraphToJsonMapperNode convertInnerSelectionSet(SelectionSet selectionSet, NodeQuery nodeQuery) {
            GraphToJsonMapperNode result;
            if (selectionSet == null) {
                result = GraphToJsonNodeMapperLiteral.get();
            } else {
                GraphToJsonNodeMapperObject nodeMapperObject = new GraphToJsonNodeMapperObject();
                convertInnerSelectionSet(nodeMapperObject, selectionSet, nodeQuery);
//                for (Selection<?> selection : selectionSet.getSelections()) {
//                    if (selection instanceof Field) {
//                        Field field = (Field)selection;
//                        String jsonKeyName = ObjectUtils.firstNonNull(field.getAlias(), field.getName());
//                        GraphToJsonPropertyMapper propertyMapper = convertInnerField(field, nodeQuery);
//                        if (propertyMapper != null) {
//                            nodeMapperObject.getPropertyMappers().put(jsonKeyName, propertyMapper);
//                        }
//                    } else if (selection instanceof InlineFragment) {
//                        InlineFragment fragment = (InlineFragment)selection;
//
//                        // If there is a type name then resolve it
//                        TypeName typeName = fragment.getTypeCondition();
//                        String name = typeName.getName();
//                        // contextStack.peek().ge
//
//                        // nodeQuery.
//                        // fragment.getSelectionSet();
//                        // throw new RuntimeException("Inline Fragement is not supported yet");
//                        NodeQuery nodeFragment = nodeQuery.addFragment();
//                        convertInnerSelectionSet(fragment.getSelectionSet(), nodeFragment);
//                    }
//                }
                result = nodeMapperObject;
            }
            return result;
        }

        public GraphToJsonMapperNode convertInnerSelectionSet(GraphToJsonNodeMapperObjectLike nodeMapperObject, SelectionSet selectionSet, NodeQuery nodeQuery) {
            GraphToJsonMapperNode result;
            for (Selection<?> selection : selectionSet.getSelections()) {
                if (selection instanceof Field field) {
                    // String jsonKeyName = ObjectUtils.firstNonNull(field.getAlias(), field.getName());
                    // GraphToJsonPropertyMapper propertyMapper =
                    convertInnerField(field, nodeQuery, nodeMapperObject);
//                    if (propertyMapper != null) {
//                        nodeMapperObject.getPropertyMappers().put(jsonKeyName, propertyMapper);
//                    }
                } else {
                    // Resolve fragments and inline fragements

                    SelectionSet resolvedSelectionSet = null;
                    List<Directive> resolvedbDirectives = null;
                    TypeName resolvedTypeName = null;

                    if (selection instanceof FragmentSpread fragmentSpread) {
                        String name = fragmentSpread.getName();
                        FragmentDefinition def = fragmentsByName.get(name);

                        if (def == null) {
                            throw new RuntimeException("Fragment with name " + name + " not found");
                        }

                        resolvedSelectionSet = def.getSelectionSet();
                        resolvedbDirectives = def.getDirectives();
                        resolvedTypeName = def.getTypeCondition();
                    } else if (selection instanceof InlineFragment inlineFragment) {
                        resolvedSelectionSet = inlineFragment.getSelectionSet();
                        resolvedbDirectives = inlineFragment.getDirectives();
                        resolvedTypeName = inlineFragment.getTypeCondition();
                    }

                    if (resolvedSelectionSet != null) {
                        // If there is a type name then resolve it
                        String name = resolvedTypeName.getName();
                        // contextStack.peek().ge

                        // nodeQuery.
                        // fragment.getSelectionSet();
                        // throw new RuntimeException("Inline Fragement is not supported yet");
                        Field dummyField = Field.newField()
                                .name(name)
                                .directives(resolvedbDirectives)
                                .build()
                                ;
                        Context context = contextStack.peek().newChildContext(dummyField);
                        setupContext(context);
                        contextStack.push(context);

                        NodeQuery nodeFragment = nodeQuery.addFragment();
                        context.setNodeQuery(nodeFragment);
                        processSparqlDirectives(context, resolver);

                        // fieldIri = deriveFieldIri(context, name);
                        // Node node = NodeFactory.createLiteral(nodeQuery.relationQuery().getScopeBaseName());
                        Node node = NodeFactory.createLiteral(nodeFragment.relationQuery().getScopeBaseName());


                        GraphToJsonNodeMapperFragmentHead fragmentMapper = GraphToJsonNodeMapperFragmentHead.of(node, true);

                        GraphToJsonNodeMapperFragmentBody bodyMapper = new GraphToJsonNodeMapperFragmentBody();
                        fragmentMapper.setTargetNodeMapper(bodyMapper);


                        // GraphToJsonM fragmentMapper.getTargetNodeMapper();

                        convertInnerSelectionSet(fragmentMapper.getTargetNodeMapper(), resolvedSelectionSet, nodeFragment);
                        // "fragment" + new Random().nextInt()

                        P_Path0 key = jsonMode
                                ? new P_Link(NodeFactory.createLiteralString(node.getLiteralLexicalForm()))
                                : new P_Link(node);

                        nodeMapperObject.getPropertyMappers().put(key, fragmentMapper);

                        contextStack.pop();
                    }
                }
            }
            result = nodeMapperObject;

            return result;
        }

        /**
         *
         * @param field
         * @param nodeQuery
         * @param nodeMapperObject This method will register the mapper for the field to this object
         * @return
         */
        public GraphToJsonPropertyMapper convertInnerField(Field field, NodeQuery nodeQuery, GraphToJsonNodeMapperObjectLike nodeMapperObject) {

            String jsonKeyName = ObjectUtils.firstNonNull(field.getAlias(), field.getName());

            // context = setupContext(new Context(context, field), field);
            Context context = contextStack.peek().newChildContext(field);
            setupContext(context);
            contextStack.push(context);

            // Relation filterRelation = tryParseSparqlQuery(context, field);

            GraphToJsonPropertyMapper propertyMapper = null;
            Multimap<String, Value<?>> args = GraphQlUtils.indexArguments(field);

            String fieldName = field.getName();
            Map<String, List<Directive>> directives = field.getDirectivesByName();

            // Cardinality is set up in setupContext
            boolean isSingle = Cardinality.ONE.equals(context.getThisCardinality());

            FacetPath keyPath = resolveProperty(context, fieldName);

            if (keyPath != null) {
                boolean isInverse = directives.containsKey(GraphQlSpecialKeys.inverse);
                if (isInverse) {
                    if (keyPath.getNameCount() == 1) {
                        keyPath = FacetPath.newRelativePath(keyPath.getName(0).toSegment().toggleDirection());
                    }
                }

                NodeQuery fieldQuery = nodeQuery.resolve(keyPath);
                context.setNodeQuery(fieldQuery);

//                if (filterRelation != null) {
//                    // TODO Error message if not an unary relation
//                    fieldQuery.setFilterRelation(filterRelation.toUnaryRelation());
//                }

                // FIXME We need to handle the xid field to get a resources IRI / blank node
                // label
                if (keyPath.getNameCount() == 1) { // xid resolves to a zero-segment path
                    // Set up an accumulator for the facet path

                    FacetStep step = keyPath.getFileName().toSegment();
                    P_Path0 basicPath = PathUtils.createStep(step.getNode(), step.getDirection().isForward());
                    boolean useRelationId = true;
                    if (useRelationId) {
                        Node node = NodeFactory.createLiteral(fieldQuery.relationQuery().getScopeBaseName());
                        propertyMapper = GraphToJsonPropertyMapper.of(node, step.getDirection().isForward());
                    } else {
                        propertyMapper = GraphToJsonPropertyMapper.of(basicPath);
                    }
                    propertyMapper.setSingle(isSingle);

                    Collection<ShPropertyShape> propertyShapes;
                    try {
                        propertyShapes = resolver.getGlobalPropertyShapes(basicPath);
                    } catch (UnsupportedOperationException e) {
                        propertyShapes = Collections.emptySet();
                    }
                    if (!propertyShapes.isEmpty()) {
                        boolean allMaxCountsAreOne = propertyShapes.stream().map(ShPropertyShape::getMaxCount)
                                .allMatch(v -> v != null && v.intValue() == 1);

                        boolean isUniqueLang = propertyShapes.stream().map(ShPropertyShape::isUniqueLang)
                                .allMatch(v -> v != null && v == true);

                        if (allMaxCountsAreOne) {
                            propertyMapper.setMaxCount(1);
                        }

                        if (isUniqueLang) {
                            propertyMapper.setUniqueLang(true);
                        }
                    }

                    if (directives.containsKey(GraphQlSpecialKeys.hide)) {
                        propertyMapper.setHidden(true);
                    }
                }

                SelectionSet subSelection = field.getSelectionSet();
                GraphToJsonMapperNode childConverterContrib = convertInnerSelectionSet(subSelection, fieldQuery);

                if (propertyMapper != null) { // can it be null here?
                    propertyMapper.setTargetNodeMapper(childConverterContrib);
                }

                // Handle arguments of the field, such as slice, filter and orderBy
                tryApplySlice(fieldQuery, args, assignments);
                tryApplyOrderBy(fieldQuery, args);
                // tryUpdateContext(args);
                // GraphQlUtils.tryU

                if (false) {
                    for (Argument arg : field.getArguments()) {
                        String argName = arg.getName();

                        FacetPath facetPath = resolveProperty(context, argName);
                        NodeQuery argQuery = facetPath == null ? null : fieldQuery.resolve(facetPath);

                        Value<?> rawV = arg.getValue();
                        if (rawV instanceof StringValue) {
                            StringValue v = (StringValue) rawV;
                            String str = v.getValue();

                            org.apache.jena.graph.Node TO_STRING = NodeFactory.createURI("fn:" + XSD.xstring.getURI());
                            argQuery.constraints().fwd(TO_STRING).enterConstraints().eq(NodeFactory.createLiteral(str))
                                    .activate().leaveConstraints();
                        }
                    }
                }
            }

            processSparqlDirectives(context, resolver);

            // context = context.getParent();
            contextStack.pop();

            if (nodeMapperObject != null) {
                P_Path0 key = jsonMode
                        ? new P_Link(NodeFactory.createLiteralString(jsonKeyName))
                        : keyPath.getFileName().toSegment().getStep();

                // Objects.requireNonNull(propertyMapper);
                // TODO Deal with xid
                if (propertyMapper != null) {
                    nodeMapperObject.getPropertyMappers().put(key, propertyMapper);
                }
            }

            return propertyMapper;
        }

        public Object tryApplyOrderBy(NodeQuery nodeQuery, Multimap<String, Value<?>> args) {
            Value<?> val = GraphQlUtils.getArgumentValue(args, GraphQlSpecialKeys.orderBy);

            if (val instanceof ArrayValue) {
                ArrayValue array = (ArrayValue) val;
                for (Value<?> item : array.getValues()) {
                    tryApplySortCondition(nodeQuery, item);
                }
            } else {
                tryApplyOrderBy(nodeQuery, val);
            }

            // System.out.println("ORDER: " + val);
            return null;
        }

        public Object tryApplyOrderBy(NodeQuery nodeQuery, Value<?> v) {
            if (v instanceof ObjectValue) {
                ObjectValue ov = (ObjectValue) v;
                tryApplyOrderBy(nodeQuery, ov);
            }
            return null;
        }

        public FacetPath toFacetPath(Value<?> value) {
            FacetPath result = null;
            if (value instanceof ArrayValue) {
                ArrayValue av = (ArrayValue) value;
                result = FacetPath.newRelativePath();
                for (Value<?> v : av.getValues()) {
                    FacetPath contrib = toFacetPathSingle(v);
                    if (contrib == null) {
                        result = null;
                        break;
                    } else {
                        result = result.resolve(contrib);
                    }
                }
            } else {
                result = toFacetPathSingle(value);
            }
            return result;
        }

        public FacetPath toFacetPathSingle(Value<?> value) {
            FacetPath result = null;
            String key = null;
            if (value instanceof StringValue) {
                key = ((StringValue) value).getValue();
            } else if (value instanceof EnumValue) {
                key = ((EnumValue) value).getName();
            }

            if (key != null) {
                result = resolver.resolveKeyToProperty(key);
            }
            return result;
        }

        public Object tryApplyOrderBy(NodeQuery nodeQuery, ObjectValue ov) {
            // System.out.println("ORDER: " + ov);
            Multimap<String, Value<?>> mm = GraphQlUtils.indexValues(ov);
            // System.err.println(mm);

            // for (String fieldName : mm.keySet()) {
            for (Entry<String, Collection<Value<?>>> e : mm.asMap().entrySet()) {
                String fieldName = e.getKey();
                Value<?> val = Iterables.getOnlyElement(e.getValue());
                String orderStr = GraphQlUtils.toString(val);

                int order = "ASC".equalsIgnoreCase(orderStr) ? Query.ORDER_ASCENDING
                        : "DESC".equalsIgnoreCase(orderStr) ? Query.ORDER_DESCENDING : Query.ORDER_DEFAULT;

                Context match = contextStack.peek().findOnlyField(fieldName);
//                Set<Context> matches = context.findField(fieldName);
//                List<Path<String>> matchingPaths = matches.stream().map(Context::getPath).collect(Collectors.toList());
//
//                Context match;
//                if (matches.isEmpty()) {
//                    throw new NoSuchElementException("Could not resolve field name " + fieldName + " at path " + context.getPath());
//                } else if (matches.size() > 1) {
//                    throw new IllegalArgumentException("Ambiguous resolution. Field name + " + fieldName + " expected to resolve to 1 field. Got " + matchingPaths.size() + " fields: " + matchingPaths);
//                } else {
//                    match = Iterables.getOnlyElement(matches);
//                }

                NodeQuery nq = match.getNodeQuery();
                FacetPath facetPath = nq.getFacetPath();

                FacetPath base = nodeQuery.getFacetPath();
                FacetPath rel = base.relativize(facetPath);

                // FacetPath facetPath = match.getFacetPath();
                // NodeQuery nodeQuery = match.getNodeQuery();
                ConstraintNode<NodeQuery> sortNode = nodeQuery.constraints().resolve(rel);
                sortNode.sort(order);
            }

//            Value<?> pathValue = Iterables.getOnlyElement(mm.get("path"), null);
//            FacetPath path = toFacetPath(pathValue);
//            // System.out.println("FacetPath: " + path);
//            Value<?> dirValue = Iterables.getOnlyElement(mm.get("dir"), null);
//
//            if (path != null) {
//                // We do not want to project the sort conditions so we need to
//                // resolve against this node's constraints
//                ConstraintNode<NodeQuery> sortNode = nodeQuery.constraints().resolve(path);
//                sortNode.sort(Query.ORDER_ASCENDING);
//                // nodeQuery.relationQuery().getSortConditions().add(new SortCondition(sortNode.getRoot().asJenaNode(), Query.ORDER_DESCENDING));
//                // nodeQuery.resolve(path);
//                // sortNode.getRoot().sortDesc();
//                // nodeQuery.resolve(path).sortDesc();
//            }
//
//            // ov.getObjectFields()
//            // Value<?> val = getArgumentValue(args, "order");

            return null;
        }

        public Object tryApplySortCondition(NodeQuery nodeQuery, Value<?> value) {
            if (value instanceof ObjectValue) {
                ObjectValue ov = (ObjectValue) value;
                tryApplyOrderBy(nodeQuery, ov);
            }
            return null;
        }
    }

    /** Parses a GraphQL node's rdf annotations. */
    public static Context setupContext(Context cxt) {
        Field field = cxt.getField();
        List<Directive> rdfDirectives = field.getDirectives("rdf");
        for (Directive rdf : rdfDirectives) {
            String baseContrib = GraphQlUtils.toString(GraphQlUtils.getValue(rdf.getArgument("base")));
            String iriContrib = GraphQlUtils.toString(GraphQlUtils.getValue(rdf.getArgument("iri")));
            String nsContrib = GraphQlUtils.toString(GraphQlUtils.getValue(rdf.getArgument("ns")));
            // String prefixContrib =
            // GraphQlUtils.toString(GraphQlUtils.getValue(rdf.getArgument("prefix")));
            PrefixMap prefixMapContrib = tryGetPrefixMap(GraphQlUtils.getValue(rdf.getArgument("prefixes")));

            if (baseContrib != null) {
                cxt.setBase(baseContrib);
            }
            if (iriContrib != null) {
                cxt.setIri(iriContrib);
            }
            if (nsContrib != null) {
                cxt.setNs(nsContrib);
            }
            // if (prefixContrib != null) { cxt.setPrefix(prefixContrib); }
            if (prefixMapContrib != null) {
                cxt.setLocalPrefixMap(prefixMapContrib);
            }
        }

        ScopedCardinality scopedCardinality = getCardinality(field);
        if (scopedCardinality != null) {
            Cardinality cardinality = scopedCardinality.getCardinality();
            if (scopedCardinality.isSelf()) {
                cxt.setThisCardinality(cardinality);
            }

            if (scopedCardinality.isCascade()) {
                cxt.setInheritedCardinality(cardinality);
            }
        }

        // Invoke update to compute the effective prefix information
        cxt.update();
        return cxt;
    }

    public static String deriveFieldIri(Context context, String fieldName) {
        // If base, iri or ns is specified then resolve the field to that IRI
        String result = null;
        String base = context.getFinalBase();
        String ns = context.getFinalNs();
        String iri = context.getFinalIri();

        if (base != null && !base.isBlank()) {
            result = base + fieldName;
        }

        String namespace = null;
//        if (prefix != null) {
//            namespace = context.getPrefix(prefix);
//        }
        if (ns != null) {
            namespace = ns;
        }

        if (namespace != null) {
            result = namespace + fieldName;
        }

        if (iri != null) {
            result = iri;
        }

        return result;
    }

//    public static Relation tryParseSparqlQuery(Context cxt, Field field) {
//        List<Directive> sparqlDirectives = field.getDirectives("sparql");
//        Directive dir = ListUtils.lastOrNull(sparqlDirectives);
//
//        return tryParseSparqlQuery(cxt, dir);
//    }

    public static void processSparqlDirectives(Context cxt, GraphQlResolver resolver) {
        Field field = cxt.getField();
        String fieldName = field.getName();
        // String fieldIri = deriveFieldIri(cxt, fieldName);
        NodeQuery nodeQuery = cxt.getNodeQuery();
        FacetPath facetPath = nodeQuery.getFacetPath();

        // Process sparql directives
        List<Fragment1> filterRelations = new ArrayList<>();
        for (Directive dir : field.getDirectives()) {
            Fragment contrib = null;
            boolean isInject = false;
            if (GraphQlSpecialKeys.sparql.equals(dir.getName())) {
                contrib = tryParseSparqlQuery(cxt, dir, GraphQlSpecialKeys.fragment);

                // FIXME handle the case where fragment and inject are given
                if (contrib == null) {
                    contrib = tryParseSparqlQuery(cxt, dir, GraphQlSpecialKeys.inject);
                    if (contrib != null) {
                        isInject = true;
                    }
                }
            } else if ("class".equals(dir.getName())) {
                // If the field does not have an IRI we need to resort to resolution
                Node classNode = resolveClass(resolver, cxt, fieldName);
                String fieldIri = classNode.getURI();
                // NodeQuery fieldQuery = resolveKeyToClasses(nodeQuery, classNode);

                // Produce an IRI from the field name
                contrib = ConceptUtils.createForRdfType(fieldIri);
            }

            if (contrib != null) {

                if (isInject) {
                    // Resolve visible variables against known fields
                    Map<Var, Node> varMap = new HashMap<>();
                    for (Var v : contrib.getVars()) {
                        String name = v.getName();
                        Context match;
                        try {
                            match = cxt.findOnlyField(name);
                        } catch (NoSuchElementException e) { // Expensive - get rid of exception handling
                            // Ignore
                            continue;
                        }

                        NodeQuery tgtNodeQuery = match.getNodeQuery();
                        FacetPath tgtFacetPath = tgtNodeQuery.getFacetPath();

                        FacetPath delta = facetPath.relativize(tgtFacetPath);

                        ConstraintNode<NodeQuery> cn = nodeQuery.constraints().resolve(delta);
                        Node jenaNode = cn.asJenaNode();

                        Node substVar = jenaNode; // cn.var();
                        varMap.put(v, substVar);
                    }

                    // FIXME Only substitute in-scope variables - so apply scope rename first
                    // Rename.reverseVarRename(null)
                    // Relation resolvedContrib = contrib.applyNodeTransform(new
                    // NodeTransformSubst(varMap));
                    MappedFragment<Node> mr = MappedFragment.of(contrib, varMap);
                    // System.err.println("Resolved contrib: " + resolvedContrib);
                    nodeQuery.addInjectFragment(mr);
                    // TODO Register the resolved relation
                } else {
                    filterRelations.add(contrib.toFragment1());
                }
            }
        }

        Fragment1 filterRelation = filterRelations.stream().reduce((a, b) -> {
            return a.joinOn(a.getVar()).with(b).toFragment1();
        }).orElse(null);
        nodeQuery.setFilterFragment(filterRelation);
    }

    public static Fragment tryParseSparqlQuery(Context cxt, Directive dir, String argName) {
        String queryStr = Optional.ofNullable(dir).map(d -> d.getArgument(argName)).map(Argument::getValue)
                .map(GraphQlUtils::toString).orElse(null);

        Fragment result = null;
        if (queryStr != null) {
            String base = cxt.getFinalBase();
            PrefixMapping pm = new PrefixMappingAdapter(cxt.getFinalPrefixMap());
            SparqlQueryParser parser = SparqlQueryParserImpl
                    .create(SparqlParserConfig.newInstance().setBaseURI(base).setPrefixMapping(pm));
            Query query = parser.apply(queryStr);
            result = FragmentUtils.fromQuery(query);
        }
        return result;
    }

    /** Returns null if neither offset nor limit is found */
    public static Range<Long> tryParseSlice(Multimap<String, Value<?>> args, Map<String, Value<?>> assignments) {
        // System.out.println("Children: " + field.getNamedChildren().getChildren());
        Long offset = GraphQlUtils.toLong(GraphQlUtils.getArgumentValue(args, "offset", assignments));
        Long limit = GraphQlUtils.toLong(GraphQlUtils.getArgumentValue(args, "limit", assignments));
        Range<Long> result = offset == null && limit == null ? null : RangeUtils.createRange(limit, offset);
        return result;
    }

    /** This method uses the context to resolve a field name to a relative facet path */
    public FacetPath resolveProperty(Context cxt, String fieldName) {
        FacetPath result;
        // For now it seems easier to handle the xid field here rather then in the
        // property resolver
        if ("xid".equals(fieldName)) {
            result = FacetPath.newRelativePath();
        } else {
            String fieldIri = deriveFieldIri(cxt, fieldName);
            result = fieldIri != null ? FacetPath.newRelativePath(FacetStep.fwd(NodeFactory.createURI(fieldIri)))
                    : resolver.resolveKeyToProperty(fieldName);
        }
        return result;
    }

    public static org.apache.jena.graph.Node resolveClass(GraphQlResolver resolver, Context cxt, String fieldName) {
        String fieldIri = deriveFieldIri(cxt, fieldName);
        Set<org.apache.jena.graph.Node> classes = fieldIri != null ? Set.of(NodeFactory.createURI(fieldIri))
                : resolver.resolveKeyToClasses(fieldName);
        org.apache.jena.graph.Node result = IterableUtils.expectZeroOrOneItems(classes);
        return result;
    }

    public NodeQuery resolveKeyToClasses(NodeQuery nq, org.apache.jena.graph.Node cls) {
        NodeQuery result = null;
        if (cls != null) {
            // FacetStep step = FacetStep.of(RDF.type.asNode(), Direction.FORWARD, "",
            // FacetStep.TARGET);
            result = nq.constraints().fwd(RDF.type.asNode()).enterConstraints().eq(cls).activate().leaveConstraints()
                    .getRoot();
        }
        return result;
    }

//    public NodeQuery resolveKeyToProperty(NodeQuery nq, String key) {
//        FacetPath keyPath = resolver.resolveKeyToProperty(key);
//        NodeQuery result = keyPath == null ? null : nq.resolve(keyPath);
//        return result;
//    }

//    public NodeQuery resolveKeyToProperty(NodeQuery nq, FacetPath facetPath) {
//        // FacetPath keyPath = resolver.resolveKeyToProperty(key);
//        NodeQuery result = keyPath == null ? null : nq.resolve(keyPath);
//        return result;
//    }

    public static void tryApplySlice(NodeQuery nodeQuery, Multimap<String, Value<?>> args, Map<String, Value<?>> assignments) {
        Range<Long> slice = tryParseSlice(args, assignments);
        if (slice != null) {
            long offset = QueryUtils.rangeToOffset(slice);
            if (offset != 0) {
                nodeQuery.offset(offset);
            }
            long limit = QueryUtils.rangeToLimit(slice);
            nodeQuery.limit(limit);
        }
    }

//    public static void tryUpdateContext(Multimap<String, Value<?>> args) {
//        String baseIri = GraphQlUtils.tryGetArgumentValue(args, "base")
//                .map(GraphQlUtils::toString)
//                .orElse(null);
//
//        Value<?> val = GraphQlUtils.tryGetArgumentValue(args, "namespaces").orElse(null);
//        PrefixMap pm = tryGetPrefixMap(val);
//
//    }

    public static PrefixMap tryGetPrefixMap(Value<?> value) {
        PrefixMap result = null;
        if (value instanceof ObjectValue) {
            ObjectValue obj = (ObjectValue) value;
            result = PrefixMapFactory.create();
            for (ObjectField field : obj.getObjectFields()) {
                String prefix = field.getName();
                String namespace = GraphQlUtils.toString(field.getValue());
                result.add(prefix, namespace);
            }
        }
        // System.out.println(result);
        return result;
    }

    /**
     * Returns the last {@literal @one} or {@literal @many} directive - null if
     * there is none.
     */
    public static ScopedCardinality getCardinality(DirectivesContainer<?> container) {
        List<Directive> directives = container.getDirectives();
        ScopedCardinality result = directives.stream().map(GraphQlToSparqlConverter::getCardinality)
                .filter(Objects::nonNull).reduce((a, b) -> b).orElse(null);
        return result;
    }

    /**
     * Return a cardinality object if the directive is {@literal @one} or
     * {@literal @many} - null if neither.
     */
    public static ScopedCardinality getCardinality(Directive d) {
        ScopedCardinality result = null;
        String name = d.getName();
        Cardinality cardinality = GraphQlSpecialKeys.one.equalsIgnoreCase(name) ? Cardinality.ONE
                : GraphQlSpecialKeys.many.equalsIgnoreCase(name) ? Cardinality.MANY : null;

        if (cardinality != null) {
            boolean cascade = Optional.ofNullable(GraphQlUtils.getArgAsBoolean(d, GraphQlSpecialKeys.cascade, null))
                    .orElse(false);
            boolean self = Optional.ofNullable(GraphQlUtils.getArgAsBoolean(d, GraphQlSpecialKeys.self, null))
                    .orElse(true);
            result = new ScopedCardinality(cardinality, cascade, self);
        }
        return result;
    }
}
