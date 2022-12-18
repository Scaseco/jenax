package org.aksw.jenax.arq.aggregation;

import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;

public class AggTransform2<B, E, I, O, C extends Aggregator<B, E, I>>
    implements Aggregator<B, E, O>
{
    private C subAgg;
    private Function<? super I, O> transform;

    @Deprecated
    public AggTransform2(C subAgg, com.google.common.base.Function<? super I, O> transform) {
        this.subAgg = subAgg;
        this.transform = (arg) -> transform.apply(arg);
    }

    public AggTransform2(C subAgg, Function<? super I, O> transform) {
        this.subAgg = subAgg;
        this.transform = transform;
    }

    @Override
    public Accumulator<B, E, O> createAccumulator() {
        Accumulator<B, E, I> baseAcc = subAgg.createAccumulator();
        Accumulator<B, E, O> result = new AccTransform2<>(baseAcc, transform);
        return result;
    }

    public static <B, E, I, O, C extends Aggregator<B, E, I>> AggTransform2<B, E, I, O, C> create(C subAgg, Function<? super I, O> transform) {
        AggTransform2<B, E, I, O, C> result = new AggTransform2<>(subAgg, transform);
        return result;
    }

//    public static <I, O> AggTransform<I, O> create(Agg<I> subAgg, Function<I, O> transform) {
//        AggTransform<I, O> result = new AggTransform<I, O>(subAgg, transform);
//        return result;
//    }

}