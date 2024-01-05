package org.aksw.jenax.graphql.sparql;

public class ScopedCardinality
    extends Scoped
{
    protected Cardinality cardinality;

    public ScopedCardinality(Cardinality cardinality, boolean all) {
        super(all);
        this.cardinality = cardinality;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }
}
