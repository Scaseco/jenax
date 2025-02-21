package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.Timeouts;
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
        this(Context::emptyContext);
    }

    public QueryExecModCustomBase(Supplier<Context> baseContextSupplier) {
        this(ContextAccumulator.newBuilder(baseContextSupplier));
    }

    public QueryExecModCustomBase(ContextAccumulator contextAccumulator) {
        super();
        this.contextAccumulator = contextAccumulator;
    }

    public QueryExecModCustomBase(QueryExecModCustomBase<?> that) {
        super();
        this.contextAccumulator = that.contextAccumulator.clone();
        this.initialTimeoutValue = that.initialTimeoutValue;
        this.initialTimeoutUnit = that.initialTimeoutUnit;
        this.overallTimeoutValue = that.overallTimeoutValue;
        this.overallTimeoutUnit = that.overallTimeoutUnit;
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
    public QueryExecMod timeout(long timeout) {
        overallTimeout(timeout, TimeUnit.MILLISECONDS);
        return self();
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

    // Set times from context if not set directly. e..g Context provides default values.
    // Contrast with SPARQLQueryProcessor where the context is limiting values of the protocol parameter.
    public static void defaultTimeoutsFromContext(QueryExecModCustomBase<?> builder, Context cxt) {
        applyTimeouts(builder, cxt.get(ARQ.queryTimeout));
    }

    /** Take obj, find the timeout(s) and apply to the builder */
    public static <T extends QueryExecMod> T overwriteTimeouts(T builder, Object obj) {
        if (obj != null) {
            try {
                if ( obj instanceof Number ) {
                    long x = ((Number)obj).longValue();
                    builder.overallTimeout(x, TimeUnit.MILLISECONDS);
                } else if ( obj instanceof String ) {
                    String str = obj.toString();
                    Pair<Long, Long> pair = Timeouts.parseTimeoutStr(str, TimeUnit.MILLISECONDS);
                    if ( pair != null ) {
                        builder.initialTimeout(pair.getLeft(), TimeUnit.MILLISECONDS);
                        builder.overallTimeout(pair.getRight(), TimeUnit.MILLISECONDS);
                    } else {
                        Log.warn(builder, "Bad timeout string: "+str);
                    }
                } else
                    Log.warn(builder, "Can't interpret timeout: " + obj);
            } catch (Exception ex) {
                Log.warn(builder, "Exception setting timeouts (context) from: "+obj, ex);
            }
        }
        return builder;
    }

    /** Take obj, find the timeout(s) and apply to the builder */
    public static void applyTimeouts(QueryExecModCustomBase<?> builder, Object obj) {
        if ( obj == null )
            return ;
        try {
            if ( obj instanceof Number ) {
                long x = ((Number)obj).longValue();
                if ( builder.overallTimeoutValue < 0 )
                    builder.overallTimeout(x, TimeUnit.MILLISECONDS);
            } else if ( obj instanceof String ) {
                String str = obj.toString();
                Pair<Long, Long> pair = Timeouts.parseTimeoutStr(str, TimeUnit.MILLISECONDS);
                if ( pair == null ) {
                    Log.warn(builder, "Bad timeout string: "+str);
                    return ;
                }
                if ( builder.initialTimeoutValue < 0 )
                    builder.initialTimeout(pair.getLeft(), TimeUnit.MILLISECONDS);
                if ( builder.overallTimeoutValue < 0 )
                    builder.overallTimeout(pair.getRight(), TimeUnit.MILLISECONDS);
            } else
                Log.warn(builder, "Can't interpret timeout: " + obj);
        } catch (Exception ex) {
            Log.warn(builder, "Exception setting timeouts (context) from: "+obj, ex);
        }
    }

    public <X  extends QueryExecMod> X applySettings(X dst) {
        if (initialTimeoutUnit != null) {
            dst.initialTimeout(initialTimeoutValue, initialTimeoutUnit);
        }

        if (overallTimeoutUnit != null) {
            dst.initialTimeout(overallTimeoutValue, overallTimeoutUnit);
        }

        // Be careful to Only apply added settings!
        // E.g. using a non-empty base context in this builder's contextAccumulator will override
        // those settings in the context of the delegate query exec.
        Context context = getContext();
        if (context != null) {
            Context dstCxt = dst.getContext();
            if (dstCxt != null) {
                dstCxt.putAll(context);
            }
        }

        return dst;
    }
}
