package org.aksw.jenax.graphql;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.util.direction.Direction;
import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.util.expr.NodeValueUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.facete.treequery2.impl.NodeQueryImpl;
import org.aksw.jenax.model.voidx.api.VoidDataset;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VOID;
import org.apache.jena.vocabulary.XSD;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;

import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.Document;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.parser.Parser;


/**
 * Things to be done:
 * - Run the basic void and shacl generation queries
 * - Use the resulting model to configure key and property resolvers; might also need property-per-class resolver
 * -
 *
 */
public class MainPlaygroundGraphql {


    protected VoidDataset voidDataset;

    public MainPlaygroundGraphql(VoidDataset voidDataset) {
        super();
        this.voidDataset = voidDataset;
    }

    public static final String xid = "xid"; // "@id" with '@' replaced by 'x' because '@' is not a valid in graphql identifiers

    public static final String orderBy = "orderBy";

    public static void main(String[] args) {
        // System.out.println(ObjectUtils.tryCastAs(long.class, 1));
        // if (true) return;

        Model dataModel = RDFDataMgr.loadModel("/home/raven/Datasets/pokedex/pokedex-data-rdf.ttl");
        // SparqlStmtMgr.execSparql(model, "void/sportal/compact/qb2.rq");

        // TODO Generate basic void

        Model voidModel = RDFDataMgr.loadModel("/home/raven/Datasets/pokedex/pokedex.void.ttl");
        Resource voidDatasetRaw = IterableUtils.expectOneItem(voidModel.listSubjectsWithProperty(RDF.type, VOID.Dataset).toList());

        VoidDataset voidDataset = voidDatasetRaw.as(VoidDataset.class);

        System.out.println(voidDataset.getClassPartitionMap().keySet());
        System.out.println(voidDataset.getPropertyPartitionMap().keySet());

        // if (true) { return; }

        // items(type: \"Pokemon\")

        Parser parser = new Parser();
        Document document = parser.parseDocument(
                "{ Pokemon(limit: 10, offset: 5, orderBy: [{colour: desc}, {path: [colour, etag], dir: asc}]) {"
                + "_maleRatio, colour(xid: \"red\"), speciesOf { baseHP }, "
                + "} }");
        System.out.println(document.toString());
//        + "Instrument(id: \"1234\", created_datime: { between: { min: 1, max: 5} }) {"
//        + "  Reference { Name },"
//        + "  created_datime"
//        + "} "

        NodeQuery nodeQuery = NodeQueryImpl.newRoot();

        MainPlaygroundGraphql mapper = new MainPlaygroundGraphql(voidDataset);
        mapper.graphQlToNodeQuery(document, nodeQuery, true);
        RelationQuery rq = nodeQuery.relationQuery();
        Query query = ElementGeneratorLateral.toQuery(rq);
        System.out.println(query);
    }

    public Set<org.apache.jena.graph.Node> resolveKeyToClasses(String key) {
        // TODO Index and/or cache
        Set<org.apache.jena.graph.Node> classes = voidDataset.getClassPartitionMap().keySet();
        Set<org.apache.jena.graph.Node> result = classes.stream()
                .filter(org.apache.jena.graph.Node::isURI)
                .filter(node -> node.getLocalName().equals(key))
                .collect(Collectors.toSet());
        return result;
    }

    public NodeQuery resolveKeyToClasses(NodeQuery nq, String key) {
        Set<org.apache.jena.graph.Node> classes = resolveKeyToClasses(key);
        org.apache.jena.graph.Node cls = IterableUtils.expectZeroOrOneItems(classes);
        NodeQuery result = null;
        if (cls != null) {
            // FacetStep step = FacetStep.of(RDF.type.asNode(), Direction.FORWARD, "", FacetStep.TARGET);
            result = nq.constraints().fwd(RDF.type.asNode())
                    .enterConstraints().eq(cls).activate().leaveConstraints()
                    .getRoot();
        }
        return result;
    }

    public FacetPath resolveKeyToProperty(String rawKey) {
        boolean isFwd = !rawKey.startsWith("_");
        String key = isFwd ? rawKey : rawKey.substring(1);

        FacetPath result;
        if (Objects.equals(key, xid)) {
            result = FacetPath.newRelativePath();
        } else {
            // TODO Index and/or cache
            Set<org.apache.jena.graph.Node> allProperties = voidDataset.getPropertyPartitionMap().keySet();
            List<org.apache.jena.graph.Node> matchingProperties = allProperties.stream()
                    .filter(org.apache.jena.graph.Node::isURI)
                    .filter(node -> node.getLocalName().equals(key))
                    .collect(Collectors.toList());

            org.apache.jena.graph.Node p = IterableUtils.expectZeroOrOneItems(matchingProperties);
            if (p != null) {
                FacetStep step = FacetStep.of(p, Direction.ofFwd(isFwd), "", FacetStep.TARGET);
                result = FacetPath.newRelativePath(step);
                // nq.resolve(FacetPath.newAbsolutePath().resolve(Fac))
            } else {
                result = null;
            }
        }
        return result;
    }

    public NodeQuery resolveKeyToProperty(NodeQuery nq, String key) {
        FacetPath keyPath = resolveKeyToProperty(key);
        NodeQuery result = keyPath == null ? null : nq.resolve(keyPath);
        return result;
    }

    public Object graphQlToNodeQueryChildren(Node<?> node, NodeQuery nodeQuery, boolean isType) {
        for (Node<?> child : node.getChildren()) {
            graphQlToNodeQuery(child, nodeQuery,  isType);
        }
        return nodeQuery;
    }

    public static Optional<Node<?>> tryGetNode(Node<?> node, String ... path) {
        Node<?> result = node;
        for (String segment : path) {
            if (result == null) {
                break;
            }
            result = result.getNamedChildren().getChildOrNull(segment);
        }
        return Optional.ofNullable(result);
    }

    public static Number getNumber(Node<?> node, String ... path) {
        return tryGetNode(node, path).map(MainPlaygroundGraphql::toNumber).orElse(null);
    }

    public static Long getLong(Node<?> node, String ... path) {
        Number number = getNumber(node, path);
        return number == null ? null : number.longValue();
    }

    /** Bridge graphql nodes to jena NodeValues (the latter has a nicer API) */
    public static NodeValue toNodeValue(Node<?> node) {
        NodeValue result = null;
        if (node instanceof IntValue) {
            result = NodeValue.makeInteger(((IntValue)node).getValue());
        } else if (node instanceof FloatValue) {
            result = NodeValue.makeDecimal(((FloatValue)node).getValue());
        } else if (node instanceof BooleanValue) {
            result = NodeValue.makeBoolean(((BooleanValue)node).isValue());
        } else if (node instanceof StringValue) {
            result = NodeValue.makeString(((StringValue)node).getValue());
        }
        return result;
    }

    public static Number toNumber(Node<?> node) {
        NodeValue nv = toNodeValue(node);
        Number result = nv == null ? null : NodeValueUtils.getNumber(nv);
        return result;
    }

    public static Long toLong(Node<?> node) {
        Number number = toNumber(node);
        Long result = number == null ? null : number.longValue();
        return result;
    }

    public static Multimap<String, Value<?>> indexArguments(Field field) {
        Multimap<String, Value<?>> result = Multimaps.transformValues(
                Multimaps.index(field.getArguments(), Argument::getName), Argument::getValue);
        return result;
        // field.getArguments().stream().collect(null)
    }

    public static Multimap<String, Value<?>> indexValues(ObjectValue field) {
        Multimap<String, Value<?>> result = Multimaps.transformValues(
                Multimaps.index(field.getObjectFields(), ObjectField::getName), ObjectField::getValue);
        return result;
    }

    public static Value<?> getArgumentValue(Multimap<String, Value<?>> args, String argName) {
        Collection<Value<?>> a = args.get(argName);
        Value<?> result = IterableUtils.expectZeroOrOneItems(a);
        //Value<?> result = arg == null ? null : arg.getValue();
        return result;
    }

    /** Returns null if neither offset nor limit is found */
    public static Range<Long> tryParseSlice(Multimap<String, Value<?>> args) {
        // System.out.println("Children: " + field.getNamedChildren().getChildren());
        Long offset = toLong(getArgumentValue(args, "offset"));
        Long limit = toLong(getArgumentValue(args, "limit"));
        Range<Long> result = offset == null && limit == null ? null : RangeUtils.createRange(limit, offset);
        return result;
    }

    public Object tryApplyOrderBy(NodeQuery nodeQuery, Multimap<String, Value<?>> args) {
        Value<?> val = getArgumentValue(args, orderBy);

        if (val instanceof ArrayValue) {
            ArrayValue array = (ArrayValue)val;
            for (Value<?> item : array.getValues()) {
                tryApplySortCondition(nodeQuery, item);
            }
        } else {
            tryApplyOrderBy(nodeQuery, val);
        }

        System.out.println("ORDER: " + val);
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
            result = resolveKeyToProperty(key);
        }
        return result;
    }



    public Object tryApplyOrderBy(NodeQuery nodeQuery, ObjectValue ov) {
        System.out.println("ORDER: " + ov);

        Multimap<String, Value<?>> mm = indexValues(ov);

        Value<?> pathValue = IterableUtils.expectZeroOrOneItems(mm.get("path"));
        FacetPath path = toFacetPath(pathValue);
        System.out.println("FacetPath: " + path);
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


    public Object graphQlToNodeQuery(Node<?> node, NodeQuery nodeQuery, boolean isType) {
        boolean typeFound = false;

        if (node instanceof OperationDefinition) {
            // OperationDefinition queryNode = (OperationDefinition)node;
            // SelectionSet selectionSet = queryNode.getSelectionSet();
            //selectionSet.
            // System.out.println(selectionSet);
        } else if (node instanceof SelectionSet) {
            SelectionSet selectionSet = (SelectionSet)node;
            for (Selection<?> selection : selectionSet.getSelections()) {
                if (selection instanceof Field) {
                    Field field = (Field)selection;
                    Multimap<String, Value<?>> args = indexArguments(field);

                    String fieldName = field.getName();
                    System.out.println("Seen field: " + fieldName);
                    NodeQuery fieldQuery = null;
                    if (isType) {
                        fieldQuery = resolveKeyToClasses(nodeQuery, fieldName);
                        typeFound = true;
                    } else {
                        boolean isSpecialField = false;
                        if (!isSpecialField) {

                            fieldQuery = resolveKeyToProperty(nodeQuery, fieldName);
                        }
                    }

                    if (fieldQuery != null) {
                        tryApplySlice(nodeQuery, args);
                        tryApplyOrderBy(nodeQuery, args);


                        for (Argument arg : field.getArguments()) {
                            String argName = arg.getName();

                            NodeQuery argQuery = resolveKeyToProperty(fieldQuery, argName);

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
                        System.out.println(field);
                    }

                    for (Node<?> child : selection.getChildren()) {
                        graphQlToNodeQuery(child, nodeQuery,  isType && !typeFound);
                    }

                    // graphQlToNodeQuery(selection, fieldQuery, isType && !typeFound);
                    // field.get
                    // System.out.println(field.);
                }
                // System.out.println(selection);
                // selection.fi
            }
        }

        // for (Node<?> child : node.getChildren()) {
            graphQlToNodeQueryChildren(node, nodeQuery,  isType && !typeFound);
        // }


        // System.out.println(queryNode.getSelectionSet());

        // System.out.println(queryNode);
        // return checkDepthLimit(queryNode.get());

//        for (Entry<String, List<Node>> entry : doc.getNamedChildren().getChildren().entrySet()) {
//System.out.println(entry);
//        }

//        for (Definition def : doc.getDefinitions()) {
//            System.out.println(def);
//
//            for (Node child : def.getChildren()) {
//                // child.get
//                // graphQlToNodeQuery(child);
//            }
//        }
//        for (Node child : doc.getChildren()) {
//
//            System.out.println(child);
//
//        }
        // System.out.println(doc.getChildren());

        // RelationQue
        return null;
    }
}
