package org.aksw.jena_sparql_api.sparql.ext.array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiFunction;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.AccAdapterJena;
import org.aksw.jenax.arq.datatype.RDFDatatypeNodeList;
import org.aksw.jenax.arq.util.binding.BindingEnv;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
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

public class SparqlLibArrayAgg {
    private static final Logger logger = LoggerFactory.getLogger(SparqlLibArrayAgg.class);

    public static AccumulatorFactory wrap1(BiFunction<? super Expr, ? super Boolean, ? extends Aggregator<BindingEnv, NodeList>> ctor) {
        return (aggCustom, distinct) -> {
            Expr expr = aggCustom.getExpr();
            Aggregator<BindingEnv, NodeValue> coreAgg = ctor.apply(expr, distinct)
                    .finish(nodeList -> {
                        Node node = nodeList == null
                                ? null
                                : NodeFactory.createLiteralByValue(
                                        nodeList,
                                        RDFDatatypeNodeList.INSTANCE);

                        NodeValue r = node == null
                                ? null
                                : NodeValue.makeNode(node);

                        return r;
                    });

            return new AccAdapterJena(coreAgg.createAccumulator());
        };
    }


    public static Aggregator<BindingEnv, NodeList> aggNodeList(
            Expr expr,
            boolean distinct) {

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
                    aggArray(distinct)),
                false,
                ex -> logger.warn("Error while aggregating a nodes", ex),
                null);
    }

    /**
     * Creates an aggregator that collects geometries into a geometry collection
     * All geometries must have the same spatial reference system (SRS).
     * The resulting geometry will be in the same SRS.
     *
     * @param distinct Whether to collect geometries in a set or a list
     * @param geomFactory The geometry factory. If null then jena's default one is used.
     */
    public static ParallelAggregator<Node, NodeList, ?> aggArray(
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
