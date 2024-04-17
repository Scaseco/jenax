package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import java.util.concurrent.TimeUnit;

import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecMod;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;

public abstract class QueryExecModCustomBase<T extends QueryExecMod>
    implements QueryExecMod
{
    // Use ContextAccumulator?
    protected ContextAccumulator contextAccumulator;

    protected long initialTimeoutValue = -1;
    protected TimeUnit initialTimeoutUnit;

    protected long overallTimeoutValue = -1;
    protected TimeUnit overallTimeoutUnit;

    public QueryExecModCustomBase() {
        this(ContextAccumulator.newBuilder());
    }

    public QueryExecModCustomBase(ContextAccumulator contextAccumulator) {
        super();
        this.contextAccumulator = contextAccumulator;
    }

    public TimeUnit getInitialTimeoutUnit() {
        return initialTimeoutUnit;
    }

    public long getInitialTimeoutValue() {
        return initialTimeoutValue;
    }

    public long getOverallTimeoutValue() {
        return overallTimeoutValue;
    }

    public TimeUnit getOverallTimeoutUnit() {
        return overallTimeoutUnit;
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
        return contextAccumulator.context();
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

        // XXX Only apply added settings
        Context context = getContext();
        if (context != null) {
            Context dstCxt = dst.getContext();
            if (dstCxt != null) {
                dstCxt.putAll(context);
            }
        }
    }
}
