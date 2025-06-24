package org.aksw.jenax.dataaccess.sparql.linksource.track;

import org.apache.jena.sparql.engine.Plan;

public class PlanWrapperBase
    implements PlanWrapper
{
    protected Plan delegate;

    public PlanWrapperBase(Plan delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public Plan getDelegate() {
        return delegate;
    }
}
