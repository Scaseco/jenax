package org.aksw.jenax.dataaccess.sparql.linksource.track;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class ExecTracker {
    public record StartRecord(long requestId, Instant timestamp, Object requestObject, Runnable abortAction) {}

    public record CompletionRecord(StartRecord start, Instant timestamp, Throwable throwable) {
        public Duration duration() {
            return Duration.between(start.timestamp, timestamp);
        }

        public boolean isSuccess() {
            return throwable == null;
        }
    }

    protected AtomicLong nextId = new AtomicLong();
    protected ConcurrentMap<Long, StartRecord> idToStartRecord = new ConcurrentHashMap<>();
    protected int maxHistorySize = 1000;
    protected ConcurrentMap<Instant, CompletionRecord> history = new ConcurrentSkipListMap<>();

    public ConcurrentMap<Instant, CompletionRecord> getHistory() {
        return history;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    public long put(Object requestObject, Runnable abortAction) {
        long result = nextId.getAndIncrement();
        StartRecord record = new StartRecord(result, Instant.now(), requestObject, abortAction);
        idToStartRecord.put(result, record);
        return result;
    }

    protected void trimHistory() {
        if (history.size() >= maxHistorySize) {
            Iterator<Entry<Instant, CompletionRecord>> it = history.entrySet().iterator();
            while (history.size() >= maxHistorySize && it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }

    public CompletionRecord remove(long id, Throwable t) {
        StartRecord startRecord = idToStartRecord.remove(id);
        CompletionRecord result = null;
        if (startRecord != null) {
            trimHistory();
            Instant now = Instant.now();
            result = new CompletionRecord(startRecord, now, t);
            history.put(now, result);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Active: " + idToStartRecord.size() + ", History: " + history.size() + "/" + maxHistorySize;
    }

    public static final Symbol symTracker = SystemARQ.allocSymbol("execTracker");

    public static ExecTracker getTracker(Context context) {
        return context.get(symTracker);
    }

    public static ExecTracker requireTracker(Context context) {
        ExecTracker result = getTracker(context);
        Objects.requireNonNull("No ExecTracker registered in context");
        return result;
    }

    public static ExecTracker ensureTracker(Context context) {
        ExecTracker result = context.get(symTracker);
        if (result == null) {
            synchronized (context) {
                result = context.get(symTracker);
                if (result == null) {
                    result = new ExecTracker();
                    context.set(symTracker, result);
                }
            }
        }
        return result;
    }
}
