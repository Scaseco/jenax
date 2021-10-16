package org.aksw.jena_sparql_api.aggregation;

public interface Accumulator<B, T>
{
    void accumulate(B binding);

    T getValue();
}
