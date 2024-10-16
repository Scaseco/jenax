package org.aksw.jenax.graphql.sparql.v2.agg.jena;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilder;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderTransition;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderTransitionWrapper;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.builder.AggStateBuilderWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;

public class JenaAggs {

    public static class JaggBuilder<K>
        extends AggStateBuilderWrapper<Binding, FunctionEnv, K, Node, AggStateBuilder<Binding,FunctionEnv, K, Node>>
    {
        public JaggBuilder(AggStateBuilder<Binding, FunctionEnv, K, Node> delegate) {
            super(delegate);
        }
    }

    public static class JaggBuilderTransition<K>
        extends AggStateBuilderTransitionWrapper<Binding, FunctionEnv, K, Node, AggStateBuilderTransition<Binding,FunctionEnv, K, Node>>
    {
        public JaggBuilderTransition(AggStateBuilderTransition<Binding, FunctionEnv, K, Node> delegate) {
            super(delegate);
        }
    }

    public static <K> JaggBuilder<K> adapt(AggStateBuilder<Binding, FunctionEnv, K, Node> raw) {
        JaggBuilder<K> result = raw instanceof JaggBuilder<K> x
            ? x
            : new JaggBuilder<>(raw);
        return result;
    }

    public static <K> JaggBuilderTransition<K> adapt(AggStateBuilderTransition<Binding, FunctionEnv, K, Node> raw) {
        JaggBuilderTransition<K> result = raw instanceof JaggBuilderTransition<K> x
            ? x
            : new JaggBuilderTransition<>(raw);
        return result;
    }

}
