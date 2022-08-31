package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.aksw.jenax.arq.datatype.RDFDatatypeNodeList;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.node.NodeList;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.UnitsConversionException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;


/**
 * DBscan. By default relies on metres and jena's default geometry.greatCircleDistance distance function.
 *
 * (?tuple ?geometryComponentIdx ?eps ?minPts) geo:dbscan (?clusterId ?tuple)
 */
public class DbscanPf
    extends PropertyFunctionBase
{
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
            throw new QueryExecException("At most 2 output arguments expected");
        }

        NodeList arr = (NodeList)BindingUtils.getValue(binding, sList.get(0)).getLiteralValue();
        int geoIdx = BindingUtils.getNumber(binding, sList.get(1)).intValue();
        float eps = BindingUtils.getNumber(binding, sList.get(2)).floatValue();
        int minPts = BindingUtils.getNumber(binding, sList.get(3)).intValue();
        // String distanceFn = BindingUtils.getValue(binding, sList.get(4), null).getLiteralLexicalForm();

        int i = 0;
        List<CustomClusterable> clusterables = new ArrayList<>(arr.size());
        for (Node tupleNode : arr) {
            Object tupleObj = tupleNode.getLiteralValue();
            NodeList tuple = (NodeList)tupleObj;

            Node geomNode = tuple.get(geoIdx);
            GeometryWrapper gw = GeometryWrapper.extract(geomNode);

            CustomClusterable clusterable = new CustomClusterable(gw, tuple);
            clusterables.add(clusterable);

            ++i;
        }


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
        };

        List<Cluster<CustomClusterable>> clusters = clusterer.cluster(clusterables);

        long idx[] = {0};
        Iterator<Binding> it = clusters.stream().flatMap(cluster -> {
            long clusterIdx = idx[0]++;
            BindingBuilder ybb = BindingBuilder.create(binding);
            ybb = BindingUtils.add(ybb, oList, 0, () -> {
                return NodeValue.makeInteger(clusterIdx).asNode();
            });

            return Optional.ofNullable(ybb).map(BindingBuilder::build).stream().flatMap(clusterParent -> {
                return cluster.getPoints().stream().flatMap(member -> {
                    BindingBuilder xbb = BindingBuilder.create(clusterParent);
                    xbb = BindingUtils.add(xbb, oList, 1, () -> {
                        NodeList nl = member.getValue();
                        return NodeFactory.createLiteralByValue(nl, RDFDatatypeNodeList.INSTANCE);
                    });
                    return Optional.ofNullable(xbb).map(BindingBuilder::build).stream();
                });
            });
        }).iterator();

        QueryIterator result = QueryIterPlainWrapper.create(it, execCxt);
        return result;
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
