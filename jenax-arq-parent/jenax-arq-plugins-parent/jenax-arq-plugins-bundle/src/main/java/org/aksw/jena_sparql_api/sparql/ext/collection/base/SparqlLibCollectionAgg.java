package org.aksw.jena_sparql_api.sparql.ext.collection.base;

import java.util.function.BiFunction;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.jena_sparql_api.sparql.ext.collection.array.SparqlLibArrayAgg;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.AccAdapterJena;
import org.aksw.jenax.arq.util.binding.BindingEnv;
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
            BiFunction<? super Expr, ? super Boolean, ? extends Aggregator<BindingEnv, T>> ctor, RDFDatatype rdfDatatype) {
        return (aggCustom, distinct) -> {
            Expr expr = aggCustom.getExpr();
            Aggregator<BindingEnv, NodeValue> coreAgg = ctor.apply(expr, distinct)
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
    public static <T> Aggregator<BindingEnv, T> aggNodesFromExpr(
            Expr expr,
            ParallelAggregator<Node, T, ?> agg) {

        return
            AggBuilder.errorHandler(
                AggBuilder.inputTransform(
                    (BindingEnv benv) -> {
                        Node node = null;
                        try {
                            Binding b = benv.getBinding();
                            FunctionEnv env = benv.getFunctionEnv();
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
