package org.aksw.jenax.arq.aggregation;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;


/**
 * An accumulator similar to that of Jena, however it uses a generic for the
 * value.
 *
 * @author raven
 *
 * @param <T>
 */
public class AccList<T> implements Acc<List<T>> {
    private Agg<T> subAgg;
    private List<Acc<T>> state = new ArrayList<Acc<T>>();

    public AccList(Agg<T> subAgg) {
        this.subAgg = subAgg;
    }

    @Override
    public void accumulate(Binding binding, FunctionEnv env) {
        Acc<T> acc = subAgg.createAccumulator();
        acc.accumulate(binding, env);
        state.add(acc);
    }

    @Override
    public List<T> getValue() {
        List<T> result = new ArrayList<T>();
        for (Acc<T> item : state) {
            T val = item.getValue();
            result.add(val);
        }

        return result;
    }
}
