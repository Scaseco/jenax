package org.aksw.jenax.graphql.sparql;

public class ScopedCardinality
    extends Scope
{
    protected Cardinality cardinality;

    public ScopedCardinality(Cardinality cardinality, boolean cascade, boolean self) {
        super(cascade, self);
        this.cardinality = cardinality;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }
}
