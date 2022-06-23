package org.aksw.jena_sparql_api.sparql.ext.datatypes;

import javax.xml.datatype.Duration;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.jenax.arq.util.binding.BindingEnv;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

public class AggregatorsDuration {

    public static Duration extractDuration(NodeValue nv) {
        Duration result = null;
        try {
            result = nv.getDuration();
        } catch (Exception e) {
            // Nothing to do
        }

        return result;
    }

    public static Aggregator<BindingEnv, NodeValue> aggSum(Expr expr, boolean distinct) {
        return aggSum(expr);
    }

    public static Aggregator<BindingEnv, NodeValue> aggSum(Expr expr) {
        return AggBuilder.outputTransform(
                AggBuilder.inputTransform(
                        (BindingEnv benv) -> {
                            Binding b = benv.getBinding();
                            FunctionEnv env = benv.getFunctionEnv();
                            NodeValue nv = expr.eval(b, env);
                            return AggregatorsDuration.extractDuration(nv);
                        },
                        AggBuilder.inputFilter(input -> input != null,
                                AggregatorsDuration.aggSumDuration())),
                NodeValue::makeDuration
        );
    }

    public static ParallelAggregator<Duration, Duration, ?> aggSumDuration() {
        return AggBuilder.fold(() -> NodeValue.makeDuration("PT0H").getDuration(), Duration::add);
    }


}
