package org.aksw.jenax.io.json.accumulator;

public class AggJsonLiteral
    implements AggJsonNode
{
    @Override
    public AccJsonNode newAccumulator() {
        return new AccJsonLiteral();
    }
}
