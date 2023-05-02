package org.aksw.jena_sparql_api.sparql.ext.collection.base;

import java.util.function.BiFunction;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.jena_sparql_api.sparql.ext.collection.array.SparqlLibArrayAgg;
import org.aksw.jena_sparql_api.sparql.ext.util.AccAdapterJena;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory;
import org.apache.jena.sparql.function.FunctionEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlLibCollectionAgg {

    private static final Logger logger = LoggerFactory.getLogger(SparqlLibArrayAgg.class);

    public static <T> AccumulatorFactory wrap1(
            BiFunction<? super Expr, ? super Boolean, ? extends Aggregator<Binding, FunctionEnv, T>> ctor, RDFDatatype rdfDatatype) {
        return (aggCustom, distinct) -> {
            Expr expr = aggCustom.getExpr();
            Aggregator<Binding, FunctionEnv, NodeValue> coreAgg = ctor.apply(expr, distinct)
                    .finish(nodeCollection -> {
                        Node node = nodeCollection == null
                                ? null
                                : NodeFactory.createLiteralByValue(nodeCollection, rdfDatatype);

                        NodeValue r = node == null
                                ? null
                                : NodeValue.makeNode(node);

                        return r;
                    });

            return new AccAdapterJena(coreAgg.createAccumulator());
        };
    }


    /** Create an aggregator of nodes based on an expression */
    public static <T> Aggregator<Binding, FunctionEnv, T> aggNodesFromExpr(
            Expr expr,
            ParallelAggregator<Node, FunctionEnv, T, ?> agg) {

        return
            AggBuilder.errorHandler(
                AggBuilder.inputTransform2(
                    (Binding b, FunctionEnv env) -> {
                        Node node = null;
                        try {
                            NodeValue nv = expr.eval(b, env);
                            node = nv == null ? null : nv.asNode();
                        } catch (VariableNotBoundException e) {
                            // Ignored
                        }
                        return node;
                    },
                    agg),
                false,
                ex -> logger.warn("Error while aggregating a nodes", ex),
                null);
    }
}
