package org.aksw.jenax.analytics.core;

import org.aksw.jenax.arq.aggregation.Agg;
import org.aksw.jenax.sparql.fragment.api.EntityFragment;

public class MappedEntityFragment<T> {
    protected EntityFragment entityFragment;
    protected Agg<T> aggregator;

    public MappedEntityFragment(EntityFragment entityFragment, Agg<T> aggregator) {
        super();
        this.entityFragment = entityFragment;
        this.aggregator = aggregator;
    }

    public EntityFragment getEntityFragment() {
        return entityFragment;
    }

    public Agg<T> getAggregator() {
        return aggregator;
    }
}
