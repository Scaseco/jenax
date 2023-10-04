package org.aksw.jenax.graphql.impl.sparql;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.path.P_Path0;
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
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;

/**
 * Compiles a graphql query to a {@link GraphQlToSparqlMapping} instance.
 * Relies on a {@link GraphQlResolver} to map field names to SPARQL classes and properties.
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
            convertTopLevelFields(selectionSet, result);
        }
        return result;
    }

    public void convertTopLevelFields(SelectionSet selectionSet, GraphQlToSparqlMapping result) {
        // RdfToJsonNodeMapperObject nodeConverter = null;
        for (Selection<?> selection : selectionSet.getSelections()) {
            if (selection instanceof Field) {
                Field field = (Field)selection;
                convertTopLevelField(field, result);
            }
        }
    }

    public void convertTopLevelField(Field field, GraphQlToSparqlMapping result) {
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

        NodeQuery fieldQuery = resolver.resolveKeyToClasses(nodeQuery, fieldName);

        // Handle arguments of the field, such as slice, filter and orderBy
        if (fieldQuery != null) {
            tryApplySlice(nodeQuery, args);
            tryApplyOrderBy(nodeQuery, args);

            for (Argument arg : field.getArguments()) {
                String argName = arg.getName();

                NodeQuery argQuery = resolver.resolveKeyToProperty(fieldQuery, argName);

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
        RdfToJsonPropertyMapper propertyMapper = null;
        Multimap<String, Value<?>> args = GraphQlUtils.indexArguments(field);

        String fieldName = field.getName();
        Map<String, List<Directive>> directives = field.getDirectivesByName();

        System.out.println("Seen field: " + fieldName);
        FacetPath keyPath = resolver.resolveKeyToProperty(fieldName);
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

                Collection<ShPropertyShape> propertyShapes = resolver.getGlobalPropertyShapes(basicPath);
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

            for (Argument arg : field.getArguments()) {
                String argName = arg.getName();

                NodeQuery argQuery = resolver.resolveKeyToProperty(fieldQuery, argName);

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

        return propertyMapper;
    }

    /** Returns null if neither offset nor limit is found */
    public static Range<Long> tryParseSlice(Multimap<String, Value<?>> args) {
        // System.out.println("Children: " + field.getNamedChildren().getChildren());
        Long offset = GraphQlUtils.toLong(GraphQlUtils.getArgumentValue(args, "offset"));
        Long limit = GraphQlUtils.toLong(GraphQlUtils.getArgumentValue(args, "limit"));
        Range<Long> result = offset == null && limit == null ? null : RangeUtils.createRange(limit, offset);
        return result;
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
            result = resolver.resolveKeyToProperty(key);
        }
        return result;
    }



    public Object tryApplyOrderBy(NodeQuery nodeQuery, ObjectValue ov) {
        System.out.println("ORDER: " + ov);

        Multimap<String, Value<?>> mm = GraphQlUtils.indexValues(ov);

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


    //
//  public static void main(String[] args) {
//      // System.out.println(ObjectUtils.tryCastAs(long.class, 1));
//      // if (true) return;
//
//      Model dataModel = RDFDataMgr.loadModel("/home/raven/Datasets/pokedex/pokedex-data-rdf.ttl");
//      // SparqlStmtMgr.execSparql(model, "void/sportal/compact/qb2.rq");
//
//      // TODO Generate basic void
//      RdfDataSource dataSource = RdfDataEngines.of(dataModel);
//      Model shaclModel;
//      try (RDFConnection conn = dataSource.getConnection()) {
//          shaclModel = SparqlStmtMgr.execConstruct(conn, "sh-scalar-properties.rq");
//      }
//
//
//      Model voidModel = RDFDataMgr.loadModel("/home/raven/Datasets/pokedex/pokedex.void.ttl");
//      Resource voidDatasetRaw = IterableUtils.expectOneItem(voidModel.listSubjectsWithProperty(RDF.type, VOID.Dataset).toList());
//
//      VoidDataset voidDataset = voidDatasetRaw.as(VoidDataset.class);
//
//      System.out.println(voidDataset.getClassPartitionMap().keySet());
//      System.out.println(voidDataset.getPropertyPartitionMap().keySet());
//
//      // if (true) { return; }
//
//      // items(type: \"Pokemon\")
////GraphQL.newGraphQL(null).build().ex
//
//      Parser parser = new Parser();
//      Document document = parser.parseDocument(
//              "{ Pokemon(limit: 10, offset: 5, orderBy: [{colour: desc}, {path: [colour], dir: asc}]) {"
////              + "maleRatio, colour(xid: \"red\"), speciesOf @inverse { baseHP }, "
//              + "label, colour, speciesOf @inverse { label }, "
//              + "} }");
//      System.out.println(document.toString());
////      + "Instrument(id: \"1234\", created_datime: { between: { min: 1, max: 5} }) {"
////      + "  Reference { Name },"
////      + "  created_datime"
////      + "} "
//
//      NodeQuery nodeQuery = NodeQueryImpl.newRoot();
//
//
//
//      MainPlaygroundGraphql mapper = new MainPlaygroundGraphql(voidDataset, shaclModel);
//      RdfToJsonNodeMapper jsonConverter = mapper.graphQlToNodeQuery(document, nodeQuery, true);
//      RelationQuery rq = nodeQuery.relationQuery();
//      Query query = ElementGeneratorLateral.toQuery(rq);
//
//      System.out.println(query);
//
//      FlowableOperatorSequentialGroupBy<Quad, org.apache.jena.graph.Node, Graph> grouper = FlowableOperatorSequentialGroupBy.create(SequentialGroupBySpec.create(
//              Quad::getGraph,
//              graphNode -> GraphFactoryEx.createInsertOrderPreservingGraph(), // GraphFactory.createDefaultGraph(),
//              (graph, quad) -> graph.add(quad.asTriple())));
//
//      // RdfDataSource remoteDataSource = () -> RDFConnectionRemote.newBuilder().queryEndpoint("http://localhost:8642/sparql").build();
//      Flowable<RDFNode> graphFlow = SparqlRx
//              .execConstructQuads(() -> dataSource.asQef().createQueryExecution(query))
//              .lift(grouper)
//              .map(e -> ModelUtils.convertGraphNodeToRDFNode(e.getKey(), ModelFactory.createModelForGraph(e.getValue())));
//
//      graphFlow.forEach(rdfNode -> {
//          System.out.println("Graph Flow item: " + rdfNode.asNode());
//          // RDFDataMgr.write(System.out, rdfNode.getModel(), RDFFormat.TURTLE_PRETTY);
//          // System.out.println(RdfJsonUtils.toJson(rdfNode, Integer.MAX_VALUE, false));
//          org.apache.jena.graph.Node node = rdfNode.asNode();
//          Graph graph = rdfNode.getModel().getGraph();
//          JsonArray errors = new JsonArray();
//          JsonElement json = jsonConverter.map(PathJson.newAbsolutePath(), errors, graph, node);
//          System.out.println(json);
//      });
//
//      for (NodeQuery child : nodeQuery.getChildren()) {
//          System.out.println("child: " + child.getScopedFacetPath());
//          System.out.println(child.var());
//      }
//
//      System.out.println("jsonConverter: " + jsonConverter);
//  }
//

//  public RdfToJsonNodeMapper convertInnerFieldOld(Node<?> node, NodeQuery nodeQuery, Field field) {
//  RdfToJsonNodeMapper result = null;
//  boolean isNodeProcessed = false;
//
//  boolean typeFound = false;
//
//  if (node instanceof OperationDefinition) {
//      // OperationDefinition queryNode = (OperationDefinition)node;
//      // SelectionSet selectionSet = queryNode.getSelectionSet();
//      //selectionSet.
//      // System.out.println(selectionSet);
//  } else if (node instanceof SelectionSet) {
//      RdfToJsonNodeMapperObject nodeConverter = null;
//      SelectionSet selectionSet = (SelectionSet)node;
//      for (Selection<?> selection : selectionSet.getSelections()) {
//          if (selection instanceof Field) {
//              // The map of aggregators
//              // Map<String, PropertyConverter> converters = new LinkedHashMap<>();
//              RdfToJsonPropertyMapper propertyConverter = null;
//
//              Field field = (Field)selection;
//              Multimap<String, Value<?>> args = indexArguments(field);
//
//              String fieldName = field.getName();
//              Map<String, List<Directive>> directives = field.getDirectivesByName();
//
//              System.out.println("Seen field: " + fieldName);
//              NodeQuery fieldQuery = null;
//              if (isType) {
//                  fieldQuery = resolver.resolveKeyToClasses(nodeQuery, fieldName);
//                  typeFound = true;
//              } else {
//                  boolean isSpecialField = false;
//                  if (!isSpecialField) {
//                      if (nodeConverter == null) {
//                          nodeConverter = new RdfToJsonNodeMapperObject();
//                      }
//
//                      FacetPath keyPath = resolver.resolveKeyToProperty(fieldName);
//                      boolean isInverse = directives.containsKey(GraphQlSpecialKeys.inverse);
//                      if (isInverse) {
//                          if (keyPath.getNameCount() == 1) {
//                              keyPath = FacetPath.newRelativePath(keyPath.getName(0).toSegment().toggleDirection());
//                          }
//                      }
//
//                      fieldQuery = keyPath == null ? null : nodeQuery.resolve(keyPath);
//
//                      if (keyPath.getNameCount() == 1) {
//                          // Set up an accumulator for the facet path
//                          FacetStep step = keyPath.getFileName().toSegment();
//                          P_Path0 basicPath = PathUtils.createStep(step.getNode(), step.getDirection().isForward());
//                          propertyConverter = RdfToJsonPropertyMapper.of(basicPath);
//
//                          Collection<ShPropertyShape> propertyShapes = resolver.getGlobalPropertyShapes(basicPath);
//                          if (!propertyShapes.isEmpty()) {
//                              boolean allMaxCountsAreOne = propertyShapes.stream()
//                                      .map(ShPropertyShape::getMaxCount)
//                                      .allMatch(v -> v != null && v.intValue() == 1);
//
//                              boolean isUniqueLang = propertyShapes.stream()
//                                      .map(ShPropertyShape::isUniqueLang)
//                                      .allMatch(v -> v != null && v == true);
//
//                              if (allMaxCountsAreOne) {
//                                  propertyConverter.setMaxCount(1);
//                              }
//
//                              if (isUniqueLang) {
//                                  propertyConverter.setUniqueLang(true);
//                              }
//                          }
//
//                          if (directives.containsKey(GraphQlSpecialKeys.hide)) {
//                              propertyConverter.setHidden(true);
//                          }
//
//                      }
//                  }
//              }
//
//              // Handle arguments of the field, such as slice, filter and orderBy
//              if (fieldQuery != null) {
//                  tryApplySlice(nodeQuery, args);
//                  tryApplyOrderBy(nodeQuery, args);
//
//
//                  for (Argument arg : field.getArguments()) {
//                      String argName = arg.getName();
//
//                      NodeQuery argQuery = resolver.resolveKeyToProperty(fieldQuery, argName);
//
//                      Value<?> rawV = arg.getValue();
//                      if (rawV instanceof StringValue) {
//                          StringValue v = (StringValue)rawV;
//                          String str = v.getValue();
//
//                          org.apache.jena.graph.Node TO_STRING = NodeFactory.createURI("fn:" + XSD.xstring.getURI());
//                          argQuery.constraints().fwd(TO_STRING).enterConstraints()
//                              .eq(NodeFactory.createLiteral(str)).activate()
//                          .leaveConstraints();
//                      }
//                  }
//                  System.out.println(field);
//              }
//
//              NodeQuery childQuery = fieldQuery != null ? fieldQuery : nodeQuery;
//              RdfToJsonNodeMapper childConverterContrib = graphQlToNodeQueryChildren(selection, childQuery,  isType && !typeFound);
//
//              if (nodeConverter == null) {
//                  result = childConverterContrib;
//              } else {
//                  if (propertyConverter != null) {
//                      if (childConverterContrib != null) {
//                          propertyConverter.setTargetNodeMapper(childConverterContrib);
//                      }
//                      nodeConverter.getPropertyMappers().put(fieldName, propertyConverter);
//                  }
//                  result = nodeConverter;
//              }
////              NodeConverter childConverter = null;
////              for (Node<?> child : selection.getChildren()) {
////                  NodeConverter childConverterContrib = graphQlToNodeQuery(child, childQuery,  isType && !typeFound);
////                  if (childConverterContrib != null) {
////                      if (childConverter != null) {
////                          System.err.println("WARN: Multiple converters obtained");
////                      } else {
////                          childConverter = childConverterContrib;
////                      }
////                  }
////              }
//
//              // graphQlToNodeQuery(selection, fieldQuery, isType && !typeFound);
//              // field.get
//              // System.out.println(field.);
//          }
//          // System.out.println(selection);
//          // selection.fi
//      }
//      // result = nodeConverter;
//      isNodeProcessed = true;
//  }
//
//  // for (Node<?> child : node.getChildren()) {
//  if (!isNodeProcessed) {
//      result = graphQlToNodeQueryChildren(node, nodeQuery,  isType && !typeFound);
//  }
//  // }
//
//
//  // System.out.println(queryNode.getSelectionSet());
//
//  // System.out.println(queryNode);
//  // return checkDepthLimit(queryNode.get());
//
////  for (Entry<String, List<Node>> entry : doc.getNamedChildren().getChildren().entrySet()) {
////System.out.println(entry);
////  }
//
////  for (Definition def : doc.getDefinitions()) {
////      System.out.println(def);
////
////      for (Node child : def.getChildren()) {
////          // child.get
////          // graphQlToNodeQuery(child);
////      }
////  }
////  for (Node child : doc.getChildren()) {
////
////      System.out.println(child);
////
////  }
//  // System.out.println(doc.getChildren());
//
//  // RelationQue
//  return result;
//}



//public RdfToJsonNodeMapper graphQlToNodeQueryChildren(Node<?> node, NodeQuery nodeQuery, boolean isType) {
//  RdfToJsonNodeMapper childConverter = null;
//  for (Node<?> child : node.getChildren()) {
//      RdfToJsonNodeMapper childConverterContrib = graphQlToNodeQuery(child, nodeQuery,  isType);
//      if (childConverterContrib != null) {
//          if (childConverter != null) {
//              System.err.println("WARN: Multiple converters obtained");
//          } else {
//              childConverter = childConverterContrib;
//          }
//      }
//  }
//  return childConverter;
//
////  for (Node<?> child : node.getChildren()) {
////      graphQlToNodeQuery(child, nodeQuery,  isType);
////  }
////  return nodeQuery;
//}
}
