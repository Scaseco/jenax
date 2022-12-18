package org.aksw.jenax.arq.aggregation;

import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;

public class AccBindingTransform<B, E, V, U>
    implements Accumulator<B, E, V>
{
    protected Function<? super B, U> transform;
    protected Accumulator<? super U, E, V> subAcc;

    public AccBindingTransform(Function<? super B, U> transform, Accumulator<? super U, E, V> subAcc) {
        super();
        this.transform = transform;
        this.subAcc = subAcc;
    }

    @Override
    public void accumulate(B binding, E env) {
        U u = transform.apply(binding);
        subAcc.accumulate(u, env);
    }

    @Override
    public V getValue() {
        V result = subAcc.getValue();
        return result;
    }

    public static <B, E, V, U> Accumulator<B, E, V> create(Function<? super B, U> transform, Accumulator<? super U, E, V> subAcc) {
        Accumulator<B, E, V>  result = new AccBindingTransform<>(transform, subAcc);
        return result;
    }

}
