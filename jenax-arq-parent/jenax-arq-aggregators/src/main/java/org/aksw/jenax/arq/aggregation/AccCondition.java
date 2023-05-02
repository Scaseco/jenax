package org.aksw.jenax.arq.aggregation;

import java.util.function.Predicate;

import org.aksw.commons.collector.domain.Accumulator;

public class AccCondition<B, E, V>
    implements Accumulator<B, E, V>
{
    protected Predicate<B> predicate;
    protected Accumulator<B, E, V> subAcc;

    public AccCondition(Predicate<B> predicate, Accumulator<B, E, V> subAcc) {
        super();
        this.predicate = predicate;
        this.subAcc = subAcc;
    }

    @Override
    public void accumulate(B binding, E env) {
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

    public static <B, E, V> Accumulator<B, E, V> create(Predicate<B> predicate, Accumulator<B, E, V> subAcc) {
        Accumulator<B, E, V> result = new AccCondition<>(predicate, subAcc);
        return result;
    }
}
