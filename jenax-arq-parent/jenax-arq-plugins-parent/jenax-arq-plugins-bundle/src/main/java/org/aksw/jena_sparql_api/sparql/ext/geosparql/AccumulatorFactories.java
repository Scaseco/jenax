package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.function.BiFunction;

import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.jenax.arq.util.binding.BindingEnv;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory;
import org.apache.jena.sparql.function.FunctionEnv;

public class AccumulatorFactories {
    /**
     * Create a AccumulatorFactory from a function that takes one {@link Expr} and a distinct flag as arguments and returns
     * an {@link Aggregator}.
     *
     * @param ctor The aggregator constructor function
     * @return
     */
    public static AccumulatorFactory wrap1(BiFunction<? super Expr, ? super Boolean, ? extends Aggregator<Binding, FunctionEnv, NodeValue>> ctor) {
        return (aggCustom, distinct) -> {
            Expr expr = aggCustom.getExpr();
            Aggregator<Binding, FunctionEnv, NodeValue> coreAgg = ctor.apply(expr, distinct);

            return new AccAdapterJena(coreAgg.createAccumulator());
        };
    }
}
