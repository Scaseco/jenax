package org.aksw.jena_sparql_api.sparql.ext.collection.set;

import java.util.LinkedHashSet;
import java.util.function.BiFunction;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.jena_sparql_api.sparql.ext.collection.base.SparqlLibCollectionAgg;
import org.aksw.jenax.arq.datatype.RDFDatatypeNodeSet;
import org.aksw.jenax.arq.util.node.NodeSet;
import org.aksw.jenax.arq.util.node.NodeSetImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory;
import org.apache.jena.sparql.function.FunctionEnv;

public class SparqlLibSetAgg {
    public static <T> AccumulatorFactory wrap1(
            BiFunction<? super Expr, ? super Boolean, ? extends Aggregator<Binding, FunctionEnv, T>> ctor) {
        return SparqlLibCollectionAgg.wrap1(ctor, RDFDatatypeNodeSet.get());
    }

    /** Distinct flag is irrelevant for sets */
    public static Aggregator<Binding, FunctionEnv, NodeSet> aggNodeSet(Expr expr, boolean distinct) {
        return SparqlLibCollectionAgg.aggNodesFromExpr(expr, aggSet());
    }

    public static Aggregator<Binding, FunctionEnv, NodeSet> aggNodeSetCollection(Expr expr, boolean distinct) {
        return SparqlLibCollectionAgg.aggNodesFromCollectionExpr(expr, aggSet());
    }

    /**
     * Creates an aggregator that collects geometries into a geometry collection
     * All geometries must have the same spatial reference system (SRS).
     * The resulting geometry will be in the same SRS.
     *
     * @param distinct Whether to collect geometries in a set or a list
     * @param geomFactory The geometry factory. If null then jena's default one is used.
     */
    public static ParallelAggregator<Node, FunctionEnv, NodeSet, ?> aggSet() {
        return AggBuilder.outputTransform(
            AggBuilder.setSupplier(LinkedHashSet::new), NodeSetImpl::new);
    }
}
