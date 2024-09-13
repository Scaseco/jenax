package org.aksw.jenax.graphql.sparql.v2.api.low;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccContext;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateTypeProduceNode;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateArrayInit;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateDriver;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateInit;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.rewrite.Bind;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.function.FunctionEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQlFieldExecBuilderImpl<K>
    implements GraphQlFieldExecBuilder<K>
{
    private static final Logger logger = LoggerFactory.getLogger(GraphQlFieldExecBuilderImpl.class);

    private QueryMapping<K> mapping;
    private Creator<QueryExecBuilder> queryExecBuilderCreator;

    public GraphQlFieldExecBuilderImpl(QueryMapping<K> mapping) {
        super();
        this.mapping = Objects.requireNonNull(mapping);
    }

    /** Method takes a creator of {@link QueryExecBuilder}s to abstract from the actual amount of queries being sent. */
    @Override
    public GraphQlFieldExecBuilder<K> service(Creator<QueryExecBuilder> queryExecBuilderCreator) {
        this.queryExecBuilderCreator = queryExecBuilderCreator;
        return this;
    }

    @Override
    public GraphQlFieldExec<K> build() {
        // this.queryExecBuilderCreator = Objects.requireNonNull(queryExecBuilderCreator);
        QueryExecBuilder queryExecBuilder = queryExecBuilderCreator.create();
        Objects.requireNonNull(queryExecBuilder);

        // String name = mapping.name();
        Var stateVar = mapping.stateVar();
        Node rootStateId = mapping.rootStateId();
        // BiFunction<Binding, FunctionEnv, Object> stateIdExtractor = mapping.stateIdExtractor();
        Query query = mapping.query();
        Map<?, Map<Var, Var>> stateVarMap = mapping.stateVarMap();
        boolean isSingle = mapping.isSingle();
        AggStateGon<Binding, FunctionEnv, K, Node> agg = mapping.agg();

        BiFunction<Binding, FunctionEnv, Object> stateIdExtractor = Bind.var(stateVar).andThen(node -> node);

        AccContext<K, Node> cxt = new AccContext<>(null, false, true);
        AccStateGon<Binding, FunctionEnv, K, Node> acc = agg.newAccumulator();

        AccStateTypeProduceNode<Binding, FunctionEnv, K, Node> tmp = (AccStateTypeProduceNode<Binding, FunctionEnv, K, Node>)acc;

        AccStateGon<Binding, FunctionEnv, K, Node> accInit;
        if (isSingle) {
            accInit = new AccStateInit<>(rootStateId, tmp);
        } else {
            accInit = new AccStateArrayInit<>(rootStateId, tmp);
        }
        acc.setParent(accInit); // XXX Having to manually link the parent is clumsy

        // Create a super root state that transitions to state 0
        AccStateDriver<Binding, FunctionEnv, K, Node> driver = AccStateDriver.of(cxt, accInit, true, stateIdExtractor);
        // FunctionEnv env = ExecutionContextUtils.createFunctionEnv();

        if (logger.isDebugEnabled()) {
            logger.debug("GraphQl Accumulator: " + accInit);
            logger.debug("GraphQl SPARQL Query: " + query);
        }
//        System.err.println("GraphQl Accumulator: " + accInit);
//        System.err.println("GraphQl SPARQL Query: " + query);

        QueryExec queryExec = queryExecBuilder.query(query).build();

        GraphQlFieldExec<K> result = new GraphQlFieldExecImpl<>(isSingle, query, queryExec, stateVarMap, driver, mapping);
        return result;
    }

    //
    //  // @Override
    //  public GraphQlDataProviderExecBuilder initialTimeout(long timeout, TimeUnit timeUnit) {
    //      queryExecBuilder.initialTimeout(timeout, timeUnit);
    //      return this;
    //  }
    //
    //  // @Override
    //  public GraphQlDataProviderExecBuilder overallTimeout(long timeout, TimeUnit timeUnit) {
    //      queryExecBuilder.overallTimeout(timeout, timeUnit);
    //      return this;
    //  }
    //
    //  // @Override
    //  public Context getContext() {
    //      return queryExecBuilder.getContext();
    //  }
}
