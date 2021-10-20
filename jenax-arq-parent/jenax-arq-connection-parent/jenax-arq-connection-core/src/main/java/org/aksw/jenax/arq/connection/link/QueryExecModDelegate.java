package org.aksw.jenax.arq.connection.link;

import java.util.concurrent.TimeUnit;

import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecMod;
import org.apache.jena.sparql.util.Context;

public interface QueryExecModDelegate
    extends QueryExecMod
{
    QueryExecMod getDelegate();

    default QueryExecMod initialTimeout(long timeout, TimeUnit timeUnit) {
        getDelegate().initialTimeout(timeout, timeUnit);
        return this;
    }

    default QueryExecMod overallTimeout(long timeout, TimeUnit timeUnit) {
        getDelegate().overallTimeout(timeout, timeUnit);
        return this;
    }

    default Context getContext() {
        return getDelegate().getContext();
    }

    default QueryExec build() {
        return getDelegate().build();
    }

}
