package org.aksw.jenax.arq.aggregation;

import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;

public class AccTransform2<B, E, I, O>
    implements Accumulator<B, E, O>
{
    protected Accumulator<B, E, I> subAcc;
    protected Function<? super I, O> transform;

    public AccTransform2(Accumulator<B, E, I> subAcc, Function<? super I, O> transform) {
        this.subAcc = subAcc;
        this.transform = transform;
    }

    @Override
    public void accumulate(B binding, E env) {
        subAcc.accumulate(binding, env);
    }

    @Override
    public O getValue() {
        I input = subAcc.getValue();
        O result = transform.apply(input);
        return result;
    }

    public static <B, E, I, O> Accumulator<B, E, O> create(Accumulator<B, E, I> subAcc, Function<? super I, O> transform) {
        Accumulator<B, E, O> result = create(subAcc, transform);
        return result;
    }
}
