package org.aksw.jena_sparql_api.sparql.ext.collection.array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiFunction;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.jena_sparql_api.sparql.ext.collection.base.SparqlLibCollectionAgg;
import org.aksw.jenax.arq.datatype.RDFDatatypeNodeList;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory;
import org.apache.jena.sparql.function.FunctionEnv;

public class SparqlLibArrayAgg {

    public static <T> AccumulatorFactory wrap1(
            BiFunction<? super Expr, ? super Boolean, ? extends Aggregator<Binding, FunctionEnv, T>> ctor) {
        return SparqlLibCollectionAgg.wrap1(ctor, RDFDatatypeNodeList.get());
    }

    public static Aggregator<Binding, FunctionEnv, NodeList> aggNodeList(
            Expr expr,
            boolean distinct) {
        return SparqlLibCollectionAgg.aggNodesFromExpr(expr, SparqlLibArrayAgg.aggArray(distinct));
    }

    /**
     * Creates an aggregator that collects geometries into a geometry collection
     * All geometries must have the same spatial reference system (SRS).
     * The resulting geometry will be in the same SRS.
     *
     * @param distinct Whether to collect geometries in a set or a list
     * @param geomFactory The geometry factory. If null then jena's default one is used.
     */
    public static ParallelAggregator<Node, FunctionEnv, NodeList, ?> aggArray(
            boolean distinct
    ) {
        SerializableSupplier<Collection<Node>> collectionSupplier = distinct
                ? LinkedHashSet::new
                : ArrayList::new; // LinkedList?

        return AggBuilder.outputTransform(
            AggBuilder.collectionSupplier(collectionSupplier),
            col -> {
                List<Node> nodes = col instanceof List
                        ? (List<Node>)col
                        : new ArrayList<>(col);

                NodeList r = new NodeListImpl(nodes);
                return r;
            });
    }

}
