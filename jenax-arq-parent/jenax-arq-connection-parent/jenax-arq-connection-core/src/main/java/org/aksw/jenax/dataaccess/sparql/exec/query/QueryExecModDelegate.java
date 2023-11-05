package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.concurrent.TimeUnit;

import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecMod;
import org.apache.jena.sparql.util.Context;

public interface QueryExecModDelegate
    extends QueryExecMod
{
    QueryExecMod getDelegate();

    @Override
    default QueryExecMod initialTimeout(long timeout, TimeUnit timeUnit) {
        getDelegate().initialTimeout(timeout, timeUnit);
        return this;
    }

    @Override
    default QueryExecMod overallTimeout(long timeout, TimeUnit timeUnit) {
        getDelegate().overallTimeout(timeout, timeUnit);
        return this;
    }

    @Override
    default Context getContext() {
        return getDelegate().getContext();
    }

    @Override
    default QueryExec build() {
        return getDelegate().build();
    }
}
