package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.datatype.RDFDatatypeNodeList;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.node.NodeCollection;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.UnitsConversionException;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

/**
 * DBscan. By default relies on metres and jena's default geometry.greatCircleDistance distance function.
 * For nearest neighbor lookups, this implementation builds a temporary in-memory geosparql model from the input such that the spatial
 * index of jena-geosparql is reused.
 *
 * (?tuple ?geometryComponentIdx ?eps ?minPts) geo:dbscan (?clusterId ?tuple)
 */
public class DbscanPf
    extends PropertyFunctionBase
{
    private static final Logger logger = LoggerFactory.getLogger(DbscanPf.class);

    public static final Query NEARBY_QUERY = QueryFactory.create(String.join("\n",
            "PREFIX spatial: <http://jena.apache.org/spatial#>",
            "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>",
            "SELECT ?s {",
            "  ?s spatial:nearbyGeom (?o ?radius uom:metre )",
            "}"));

    public static class CustomClusterable
        implements Clusterable
    {
        protected GeometryWrapper geom;
        protected NodeList value;

        public CustomClusterable(GeometryWrapper geom, NodeList value) {
            super();
            this.geom = geom;
            this.value = value;
        }

        public NodeList getValue() {
            return value;
        }

        public GeometryWrapper getGeometry() {
            return geom;
        }

        @Override
        public double[] getPoint() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            // The geometry is part of the value so no need to show it
            return "CustomClusterable [value=" + value + "]";
        }
    }


    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
            ExecutionContext execCxt) {

        List<Node> sList = PropFuncArgUtils.getAsList(argSubject);
        int sLength = sList.size();
        if (sLength < 4) {
            // TODO Raise during build
            throw new QueryExecException("Expected at least 4 arguments");
        }
        if (sLength > 6) {
            // TODO Raise during build
            throw new QueryExecException("Expected at most 6 arguments");
        }

        List<Node> oList = PropFuncArgUtils.getAsList(argObject);
        int oLength = oList.size();
        if (oLength > 2) {
            // TODO Raise during build
            throw new QueryExecException("At most 2 output argument expected");
        }


        NodeList arr = (NodeList)BindingUtils.getValue(binding, sList.get(0)).getLiteralValue();
        int geoIdx = BindingUtils.getNumber(binding, sList.get(1)).intValue();
        double eps = BindingUtils.getNumber(binding, sList.get(2)).doubleValue();
        int minPts = BindingUtils.getNumber(binding, sList.get(3)).intValue();

        return null;
    }


    public static NodeList dbscan(NodeCollection arr, int geoIdx, double eps, int minPts) {
        // String distanceFn = BindingUtils.getValue(binding, sList.get(4), null).getLiteralLexicalForm();

        Graph indexGraph = GraphFactory.createDefaultGraph();


        Stopwatch sw = Stopwatch.createStarted();
        int i = 0;
        List<CustomClusterable> clusterables = new ArrayList<>(arr.size());
        Map<Node, Integer> featureToIdx = new HashMap<>();
        for (Node tupleNode : arr) {
            Object tupleObj = tupleNode.getLiteralValue();
            NodeList tuple = (NodeList)tupleObj;

            Node geomLiteralNode = tuple.get(geoIdx);
            GeometryWrapper gw = GeometryWrapper.extract(geomLiteralNode);

            CustomClusterable clusterable = new CustomClusterable(gw, tuple);
            clusterables.add(clusterable);

            Node featureNode = NodeFactory.createURI("http://example.org/feature#_" + i);
            Node geomNode = NodeFactory.createURI("http://example.org/geometry#_" + i);
            indexGraph.add(featureNode, Geo.HAS_GEOMETRY_NODE, geomNode);
            indexGraph.add(geomNode, Geo.AS_WKT_NODE, geomLiteralNode);

            featureToIdx.put(featureNode, i);

            ++i;
        }
        logger.info(String.format("Built ad-hoc in-memory model from %d items in %.3f seconds",
                clusterables.size(), sw.elapsed(TimeUnit.MILLISECONDS) * 0.001));

        Dataset index = DatasetFactory.wrap(DatasetGraphFactory.wrap(indexGraph));
        sw.reset().start();
        try {
            SpatialIndex.buildSpatialIndex(index);
        } catch (SpatialIndexException e1) {
            throw new RuntimeException(e1);
        }

        logger.info(String.format("Built spatial index from %d items in %.3f seconds",
                clusterables.size(), sw.elapsed(TimeUnit.MILLISECONDS) * 0.001));

        DBSCANClusterer<CustomClusterable> clusterer = new DBSCANClusterer<CustomClusterable>(eps, minPts) {
            @Override
            protected double distance(final Clusterable xp1, final Clusterable xp2) {
                double r;
                CustomClusterable p1 = (CustomClusterable)xp1;
                CustomClusterable p2 = (CustomClusterable)xp2;

                try {
                    r = p1.getGeometry().distanceGreatCircle(p2.getGeometry());
                } catch (MismatchedDimensionException | UnitsConversionException | FactoryException
                        | TransformException e) {
                    throw new RuntimeException(e);
                }
                return r;
            };

            @Override
            protected List<CustomClusterable> getNeighbors(CustomClusterable point,
                    Collection<CustomClusterable> points) {
                List<CustomClusterable> neighbours;
                try (QueryExec qe = QueryExec.dataset(index.asDatasetGraph())
                    .query(NEARBY_QUERY)
                    .substitution("o", point.getGeometry().asNode())
                    .substitution("radius", NodeFactoryExtra.doubleToNode(eps))
                    .build()) {
                    neighbours = qe.select().stream().map(b -> b.get(Vars.s))
                            .map(f -> clusterables.get(featureToIdx.get(f)))
                            .collect(Collectors.toList());
                }
                return neighbours;
            }
        };

        sw.reset().start();
        List<Cluster<CustomClusterable>> clusters = clusterer.cluster(clusterables);
        logger.info(String.format("Clustering of %d items into %d results completed in %.3f seconds",
                clusterables.size(), clusters.size(), sw.elapsed(TimeUnit.MILLISECONDS) * 0.001));


        // Output is a tuple (?clusterId:long ?members:Array)

        NodeList result = new NodeListImpl(new ArrayList<>(clusters.size()));
        for (Cluster<CustomClusterable> cluster : clusters) {
            List<CustomClusterable> members = cluster.getPoints();
            NodeList memberList = new NodeListImpl(new ArrayList<>(members.size()));

            for (CustomClusterable member : members) {
                memberList.add(NodeFactory.createLiteralByValue(member.getValue(), RDFDatatypeNodeList.get()));
            }

            Node item = NodeFactory.createLiteralByValue(memberList, RDFDatatypeNodeList.get());
            result.add(item);
        }

        // Node result = NodeFactory.createLiteralByValue(resultItems, RDFDatatypeNodeList.get());

        return result;

//
//        Iterator<Binding> it = clusters.stream().flatMap(cluster -> {
//            long clusterId = idx[0]++;
//
//            BindingBuilder bb = BindingBuilder.create(binding);
//
//            bb = BindingUtils.add(bb, oList, 0, () -> {
//                return NodeFactoryExtra.intToNode(clusterId);
//            });
//
//            bb = BindingUtils.add(bb, oList, 1, () -> {
//                List<CustomClusterable> members = cluster.getPoints();
//                NodeList memberList = new NodeListImpl(new ArrayList<>(members.size()));
//
//                for (CustomClusterable member : members) {
//                    memberList.add(NodeFactory.createLiteralByValue(member.getValue(), RDFDatatypeNodeList.INSTANCE));
//                }
//
//                return NodeFactory.createLiteralByValue(memberList, RDFDatatypeNodeList.INSTANCE);
//            });
//
//            return Optional.ofNullable(bb).map(BindingBuilder::build).stream();
//        }).iterator();


        // Old result style returned one binding for each cluster member
        // However, storing the whole cluster in a single rdf term is easier to work with
//        if (false) {
//            long idx[] = {0};
//            Iterator<Binding> it = clusters.stream().flatMap(cluster -> {
//                long clusterIdx = idx[0]++;
//                BindingBuilder ybb = BindingBuilder.create(binding);
//                ybb = BindingUtils.add(ybb, oList, 0, () -> {
//                    return NodeValue.makeInteger(clusterIdx).asNode();
//                });
//
//                return Optional.ofNullable(ybb).map(BindingBuilder::build).stream().flatMap(clusterParent -> {
//                    return cluster.getPoints().stream().flatMap(member -> {
//                        BindingBuilder xbb = BindingBuilder.create(clusterParent);
//                        xbb = BindingUtils.add(xbb, oList, 1, () -> {
//                            NodeList nl = member.getValue();
//                            return NodeFactory.createLiteralByValue(nl, RDFDatatypeNodeList.INSTANCE);
//                        });
//                        return Optional.ofNullable(xbb).map(BindingBuilder::build).stream();
//                    });
//                });
//            }).iterator();
//        }

        // QueryIterator result = QueryIterPlainWrapper.create(it, execCxt);
        // return result;
    }


//  public static double[] toDoubleArray(Geometry geom) {
//  Coordinate[] cs = geom.getCoordinates();
//  double[] result = new double[cs.length];
//
//  int n = geom.getDimension();
//  for (Coordinate c : cs) {
//      double[] point = new double[n];
//      for (int i = 0; i < n; ++i) {
//          point[i] = c.getOrdinate(i);
//      }
//  }
//
//  return
//}

}
