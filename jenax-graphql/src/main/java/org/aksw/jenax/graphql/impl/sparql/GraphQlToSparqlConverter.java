package org.aksw.jenax.graphql.impl.sparql;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.util.node.PathUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.impl.NodeQueryImpl;
import org.aksw.jenax.io.json.mapper.RdfToJsonNodeMapper;
import org.aksw.jenax.io.json.mapper.RdfToJsonNodeMapperLiteral;
import org.aksw.jenax.io.json.mapper.RdfToJsonNodeMapperObject;
import org.aksw.jenax.io.json.mapper.RdfToJsonPropertyMapper;
import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.Directive;
import graphql.language.Document;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;

/**
 * Compiles a graphql query to a {@link GraphQlToSparqlMapping} instance.
 * All fields can be qualified with IRIs of properties and classes.
 * Names of unqualified fields are passed to a {@link GraphQlResolver} for resolution to IRIs.
 * All resolver methods are allowed to throw {@link UnsupportedOperationException} to indicate absence
 * of resolution support.
 */
public class GraphQlToSparqlConverter {
    private static final Logger logger = LoggerFactory.getLogger(GraphQlToSparqlConverter.class);

    protected GraphQlResolver resolver;

    public GraphQlToSparqlConverter(GraphQlResolver resolver) {
        super();
        this.resolver = resolver;
    }

    public GraphQlToSparqlMapping convertDocument(Document document) {
        GraphQlToSparqlMapping result = new GraphQlToSparqlMapping();
        OperationDefinition op = document.getFirstDefinitionOfType(OperationDefinition.class).orElse(null);
        if (op != null) {
            SelectionSet selectionSet = op.getSelectionSet();
            new Worker().convertTopLevelFields(selectionSet, result);
        }
        return result;
    }

    public class Worker {
        protected Context context = new Context(null, null);

        public void convertTopLevelFields(SelectionSet selectionSet, GraphQlToSparqlMapping result) {
            // RdfToJsonNodeMapperObject nodeConverter = null;
            for (Field field : selectionSet.getSelectionsOfType(Field.class)) {
                convertTopLevelField(field, result);
            }
        }

        public void convertTopLevelField(Field field, GraphQlToSparqlMapping result) {
            context = setupContext(new Context(context, field), field);

            NodeQuery nodeQuery = NodeQueryImpl.newRoot();

            // The map of aggregators
            // Map<String, PropertyConverter> converters = new LinkedHashMap<>();
            RdfToJsonPropertyMapper propertyConverter = null;
            Multimap<String, Value<?>> args = GraphQlUtils.indexArguments(field);

            String fieldName = field.getName();



            Map<String, List<Directive>> directives = field.getDirectivesByName();

            if (logger.isDebugEnabled()) {
                logger.debug("Seen top level field: " + fieldName);
            }

            Node classNode = resolveClass(context, fieldName);
            NodeQuery fieldQuery = resolveKeyToClasses(nodeQuery, classNode);


            // Handle arguments of the field, such as slice, filter and orderBy
            if (fieldQuery != null) {
                tryApplySlice(nodeQuery, args);
                tryApplyOrderBy(nodeQuery, args);

                for (Argument arg : field.getArguments()) {
                    String argName = arg.getName();

                    FacetPath facetPath = resolveProperty(context, argName);
                    NodeQuery argQuery = facetPath == null ? null : fieldQuery.resolve(facetPath);

                    Value<?> rawV = arg.getValue();
                    if (rawV instanceof StringValue) {
                        StringValue v = (StringValue)rawV;
                        String str = v.getValue();

                        org.apache.jena.graph.Node TO_STRING = NodeFactory.createURI("fn:" + XSD.xstring.getURI());
                        argQuery.constraints().fwd(TO_STRING).enterConstraints()
                            .eq(NodeFactory.createLiteral(str)).activate()
                        .leaveConstraints();
                    }
                }
            }

            SelectionSet subSelection = field.getSelectionSet();
            RdfToJsonNodeMapper nodeMapper = convertInnerSelectionSet(subSelection, nodeQuery);
            result.addEntry(fieldName, nodeQuery, nodeMapper);

            context = context.getParent();
        }

        public RdfToJsonNodeMapper convertInnerSelectionSet(SelectionSet selectionSet, NodeQuery nodeQuery) {
            RdfToJsonNodeMapper result;
            if (selectionSet == null) {
                result = RdfToJsonNodeMapperLiteral.get();
            } else {
                RdfToJsonNodeMapperObject nodeMapperObject = new RdfToJsonNodeMapperObject();
                for (Selection<?> selection : selectionSet.getSelections()) {
                    if (selection instanceof Field) {
                        Field field = (Field)selection;
                        String fieldName = field.getName();
                        RdfToJsonPropertyMapper propertyMapper = convertInnerField(field, nodeQuery);
                        if (propertyMapper != null) {
                            nodeMapperObject.getPropertyMappers().put(fieldName, propertyMapper);
                        }
                    }
                }
                result = nodeMapperObject;
            }
            return result;
        }

        public RdfToJsonPropertyMapper convertInnerField(Field field, NodeQuery nodeQuery) {
            context = setupContext(new Context(context, field), field);

            RdfToJsonPropertyMapper propertyMapper = null;
            Multimap<String, Value<?>> args = GraphQlUtils.indexArguments(field);

            String fieldName = field.getName();
            Map<String, List<Directive>> directives = field.getDirectivesByName();

            boolean isSingle = directives.containsKey("one");

            String fieldIri = deriveFieldIri(context, fieldName);
            FacetPath keyPath = fieldIri != null
                    ? FacetPath.newRelativePath(FacetStep.fwd(NodeFactory.createURI(fieldIri)))
                    : resolver.resolveKeyToProperty(fieldName);

            if (keyPath != null) {
                boolean isInverse = directives.containsKey(GraphQlSpecialKeys.inverse);
                if (isInverse) {
                    if (keyPath.getNameCount() == 1) {
                        keyPath = FacetPath.newRelativePath(keyPath.getName(0).toSegment().toggleDirection());
                    }
                }

                NodeQuery fieldQuery = nodeQuery.resolve(keyPath);

                if (keyPath.getNameCount() == 1) { // xid resolves to a zero-segment path
                    // Set up an accumulator for the facet path
                    FacetStep step = keyPath.getFileName().toSegment();
                    P_Path0 basicPath = PathUtils.createStep(step.getNode(), step.getDirection().isForward());
                    propertyMapper = RdfToJsonPropertyMapper.of(basicPath);
                    propertyMapper.setSingle(isSingle);

                    Collection<ShPropertyShape> propertyShapes;
                    try {
                        propertyShapes = resolver.getGlobalPropertyShapes(basicPath);
                    } catch (UnsupportedOperationException e) {
                        propertyShapes = Collections.emptySet();
                    }
                    if (!propertyShapes.isEmpty()) {
                        boolean allMaxCountsAreOne = propertyShapes.stream()
                                .map(ShPropertyShape::getMaxCount)
                                .allMatch(v -> v != null && v.intValue() == 1);

                        boolean isUniqueLang = propertyShapes.stream()
                                .map(ShPropertyShape::isUniqueLang)
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

                // Handle arguments of the field, such as slice, filter and orderBy
                tryApplySlice(fieldQuery, args);
                tryApplyOrderBy(fieldQuery, args);
                // tryUpdateContext(args);
                // GraphQlUtils.tryU

                for (Argument arg : field.getArguments()) {
                    String argName = arg.getName();

                    FacetPath facetPath = resolveProperty(context, argName);
                    NodeQuery argQuery = facetPath == null ? null : fieldQuery.resolve(facetPath);

                    Value<?> rawV = arg.getValue();
                    if (rawV instanceof StringValue) {
                        StringValue v = (StringValue)rawV;
                        String str = v.getValue();

                        org.apache.jena.graph.Node TO_STRING = NodeFactory.createURI("fn:" + XSD.xstring.getURI());
                        argQuery.constraints().fwd(TO_STRING).enterConstraints()
                            .eq(NodeFactory.createLiteral(str)).activate()
                        .leaveConstraints();
                    }
                }

                SelectionSet subSelection = field.getSelectionSet();
                RdfToJsonNodeMapper childConverterContrib = convertInnerSelectionSet(subSelection, fieldQuery);

                if (propertyMapper != null) { // can it be null here?
                    propertyMapper.setTargetNodeMapper(childConverterContrib);
                }
            }

            context = context.getParent();

            return propertyMapper;
        }


        public Object tryApplyOrderBy(NodeQuery nodeQuery, Multimap<String, Value<?>> args) {
            Value<?> val = GraphQlUtils.getArgumentValue(args, GraphQlSpecialKeys.orderBy);

            if (val instanceof ArrayValue) {
                ArrayValue array = (ArrayValue)val;
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
                ObjectValue ov = (ObjectValue)v;
                tryApplyOrderBy(nodeQuery, ov);
            }
            return null;
        }

        public FacetPath toFacetPath(Value<?> value) {
            FacetPath result = null;
            if (value instanceof ArrayValue) {
                ArrayValue av = (ArrayValue)value;
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
                key = ((StringValue)value).getValue();
            } else if (value instanceof EnumValue) {
                key = ((EnumValue)value).getName();
            }

            if (key != null) {
                result = resolver.resolveKeyToProperty(key);
            }
            return result;
        }



        public Object tryApplyOrderBy(NodeQuery nodeQuery, ObjectValue ov) {
            // System.out.println("ORDER: " + ov);

            Multimap<String, Value<?>> mm = GraphQlUtils.indexValues(ov);

            Value<?> pathValue = IterableUtils.expectZeroOrOneItems(mm.get("path"));
            FacetPath path = toFacetPath(pathValue);
            // System.out.println("FacetPath: " + path);
            Value<?> dirValue = IterableUtils.expectZeroOrOneItems(mm.get("dir"));

            if (path != null) {
                // We do not want to project the sort conditions so we need to
                // resolve against this node's constraints
                ConstraintNode<NodeQuery> sortNode = nodeQuery.constraints().resolve(path);
                sortNode.sort(Query.ORDER_ASCENDING);
                // nodeQuery.relationQuery().getSortConditions().add(new SortCondition(sortNode.getRoot().asJenaNode(), Query.ORDER_DESCENDING));
                // nodeQuery.resolve(path);
                // sortNode.getRoot().sortDesc();
                // nodeQuery.resolve(path).sortDesc();
            }

            // ov.getObjectFields()
            // Value<?> val = getArgumentValue(args, "order");

            return null;
        }


        public Object tryApplySortCondition(NodeQuery nodeQuery, Value<?> value) {
            if (value instanceof ObjectValue) {
                ObjectValue ov = (ObjectValue)value;
                tryApplyOrderBy(nodeQuery, ov);
            }
            return null;
        }
    }

    public static Context setupContext(Context cxt, Field field) {
        List<Directive> rdfDirectives = field.getDirectives("rdf");
        for (Directive rdf : rdfDirectives) {
            String baseContrib = GraphQlUtils.toString(GraphQlUtils.getValue(rdf.getArgument("base")));
            String iriContrib = GraphQlUtils.toString(GraphQlUtils.getValue(rdf.getArgument("iri")));
            String nsContrib = GraphQlUtils.toString(GraphQlUtils.getValue(rdf.getArgument("ns")));
            // String prefixContrib = GraphQlUtils.toString(GraphQlUtils.getValue(rdf.getArgument("prefix")));
            PrefixMap prefixMapContrib = tryGetPrefixMap(GraphQlUtils.getValue(rdf.getArgument("namespaces")));

            if (baseContrib != null) { cxt.setBase(baseContrib); }
            if (iriContrib != null) { cxt.setIri(iriContrib); }
            if (nsContrib != null) { cxt.setNs(nsContrib); }
            // if (prefixContrib != null) { cxt.setPrefix(prefixContrib); }
            if (prefixMapContrib != null) { cxt.setLocalPrefixMap(prefixMapContrib); }
        }

        if (!rdfDirectives.isEmpty()) {
            // Expand base, iri and ns against the prefixes as needed
            cxt.update();
        }
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

    /** Returns null if neither offset nor limit is found */
    public static Range<Long> tryParseSlice(Multimap<String, Value<?>> args) {
        // System.out.println("Children: " + field.getNamedChildren().getChildren());
        Long offset = GraphQlUtils.toLong(GraphQlUtils.getArgumentValue(args, "offset"));
        Long limit = GraphQlUtils.toLong(GraphQlUtils.getArgumentValue(args, "limit"));
        Range<Long> result = offset == null && limit == null ? null : RangeUtils.createRange(limit, offset);
        return result;
    }

    public FacetPath resolveProperty(Context cxt, String fieldName) {
        String fieldIri = deriveFieldIri(cxt, fieldName);
        FacetPath result = fieldIri != null
                ? FacetPath.newRelativePath(FacetStep.fwd(NodeFactory.createURI(fieldIri)))
                : resolver.resolveKeyToProperty(fieldName);
        return result;
    }

    public org.apache.jena.graph.Node resolveClass(Context cxt, String fieldName) {
        String fieldIri = deriveFieldIri(cxt, fieldName);
        Set<org.apache.jena.graph.Node> classes = fieldIri != null
                ? Set.of(NodeFactory.createURI(fieldIri))
                : resolver.resolveKeyToClasses(fieldName);
        org.apache.jena.graph.Node result = IterableUtils.expectZeroOrOneItems(classes);
        return result;
    }

    public NodeQuery resolveKeyToClasses(NodeQuery nq, org.apache.jena.graph.Node cls) {
        NodeQuery result = null;
        if (cls != null) {
            // FacetStep step = FacetStep.of(RDF.type.asNode(), Direction.FORWARD, "", FacetStep.TARGET);
            result = nq.constraints().fwd(RDF.type.asNode())
                    .enterConstraints().eq(cls).activate().leaveConstraints()
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

    public static void tryApplySlice(NodeQuery nodeQuery, Multimap<String, Value<?>> args) {
        Range<Long> slice = tryParseSlice(args);
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
            ObjectValue obj = (ObjectValue)value;
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
}
