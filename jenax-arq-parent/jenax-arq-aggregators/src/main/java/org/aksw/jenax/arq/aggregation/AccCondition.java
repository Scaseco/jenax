package org.aksw.jenax.arq.aggregation;

import java.util.function.Predicate;

import org.aksw.commons.collector.domain.Accumulator;

public class AccCondition<B, V>
    implements Accumulator<B, V>
{
    protected Predicate<B> predicate;
    protected Accumulator<B, V> subAcc;

    public AccCondition(Predicate<B> predicate, Accumulator<B, V> subAcc) {
        super();
        this.predicate = predicate;
        this.subAcc = subAcc;
    }

    @Override
    public void accumulate(B binding) {
        boolean accept = predicate.test(binding);
        if(accept) {
            subAcc.accumulate(binding);;
        }
    }

    @Override
    public V getValue() {
        V result = subAcc.getValue();
        return result;
    }

    public static <B, V> Accumulator<B, V> create(Predicate<B> predicate, Accumulator<B, V> subAcc) {
        Accumulator<B, V> result = new AccCondition<>(predicate, subAcc);
        return result;
    }
}
