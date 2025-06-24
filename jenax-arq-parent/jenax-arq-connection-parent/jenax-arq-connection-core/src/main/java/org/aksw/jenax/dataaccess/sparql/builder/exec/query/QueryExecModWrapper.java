package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import java.util.concurrent.TimeUnit;

import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecMod;
import org.apache.jena.sparql.util.Context;

public interface QueryExecModWrapper<T extends QueryExecMod>
    extends QueryExecMod
{
    QueryExecMod getDelegate();

    @SuppressWarnings("unchecked")
    default T self() {
        return (T)this;
    }

    @Override
    default QueryExecMod timeout(long timeout) {
        return overallTimeout(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    default T initialTimeout(long timeout, TimeUnit timeUnit) {
        getDelegate().initialTimeout(timeout, timeUnit);
        return self();
    }

    @Override
    default T overallTimeout(long timeout, TimeUnit timeUnit) {
        getDelegate().overallTimeout(timeout, timeUnit);
        return self();
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
