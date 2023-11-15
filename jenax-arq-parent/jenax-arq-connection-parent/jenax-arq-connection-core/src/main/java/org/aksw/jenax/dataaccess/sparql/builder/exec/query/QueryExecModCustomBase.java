package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import java.util.concurrent.TimeUnit;

import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecMod;
import org.apache.jena.sparql.util.Context;

public abstract class QueryExecModCustomBase<T extends QueryExecMod>
    implements QueryExecMod
{
    protected Context context;

    protected long initialTimeoutValue;
    protected TimeUnit initialTimeoutUnit;

    protected long overallTimeoutValue;
    protected TimeUnit overallTimeoutUnit;

    public QueryExecModCustomBase(Context context) {
        super();
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T)this;
    }

    @Override
    public T initialTimeout(long timeout, TimeUnit timeUnit) {
        this.initialTimeoutValue = timeout;
        this.initialTimeoutUnit = timeUnit;
        return self();
    }

    @Override
    public T overallTimeout(long timeout, TimeUnit timeUnit) {
        this.overallTimeoutValue = timeout;
        this.overallTimeoutUnit = timeUnit;
        return self();
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public abstract QueryExec build();


    public void applySettings(QueryExecMod dst) {
        if (initialTimeoutUnit != null) {
            dst.initialTimeout(initialTimeoutValue, initialTimeoutUnit);
        }

        if (overallTimeoutUnit != null) {
            dst.initialTimeout(overallTimeoutValue, overallTimeoutUnit);
        }

        dst.getContext().putAll(context);
    }
}
