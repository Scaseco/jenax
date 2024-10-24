package org.aksw.jenax.arq.aggregation;

import java.util.Set;

import org.aksw.commons.collector.domain.Aggregator;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;


public interface Agg<T>
    extends Aggregator<Binding, FunctionEnv, T>
{
    @Override
    Acc<T> createAccumulator();

    /**
     * An accumulator may declare the variable it references.
     * The variables can be derived from e.g. underlying Sparql expressions or
     * sub aggregators.
     * @return
     */

    Set<Var> getDeclaredVars();
}
