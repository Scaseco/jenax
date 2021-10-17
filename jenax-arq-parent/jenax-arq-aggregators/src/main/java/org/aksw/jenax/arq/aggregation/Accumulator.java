package org.aksw.jenax.arq.aggregation;

public interface Accumulator<B, T>
{
    void accumulate(B binding);

    T getValue();
}
