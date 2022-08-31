package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;


/**
 * (?geometry ?eps ?minPts) geo:dbscan (?clusterId ?clusterMemberGeometry)
 */
public class DbscanPf
    extends PropertyFunctionBase
{
//    public static class GeometryClusterable
//        extends DoublePoint
//    {
//        public GeometryClusterable(Node feature, Node geometry) {
//
//            // super(point);
//        }
//
//
//    }
//


    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
            ExecutionContext execCxt) {

        List<Node> sList = PropFuncArgUtils.getAsList(argSubject);
        int n = sList.size();

        GeometryWrapper gw = null;
        Float eps = null;
        Integer minPts = null;

        if (n > 3) {
            // TODO Raise during build
            throw new QueryExecException("Too many arguments");
        }

        if (n > 0) {
             gw = GeometryWrapper.extract(sList.get(0));
        }

        if (n > 1) {
            eps = Optional.ofNullable(NodeUtils.getNumber(sList.get(1))).map(Number::floatValue).orElse(null);
        }

        if (n > 2) {
            eps = Optional.ofNullable(NodeUtils.getNumber(sList.get(1))).map(Number::floatValue).orElse(null);
        }

        DBSCANClusterer<Clusterable> clusterer = new DBSCANClusterer<Clusterable>(eps, minPts);
        Collection<Clusterable> points = Collections.emptyList();
        List<Cluster<Clusterable>> clusters = clusterer.cluster(points);
        clusters.stream().iterator().next().getPoints();


        Optional<BindingBuilder> bindingBuilder = Optional.of(BindingBuilder.create(binding));
        // BindingUtils.processArg(bindingBuilder, , 0, null)

        // BindingUtils.processArg(bindingBuilder.g

        return null;

    }

}
