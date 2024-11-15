package org.aksw.jenax.arq.util.binding;

import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;

public abstract class QueryIterOverQueryIteratorSupplier<T extends QueryIterator>
    // extends QueryIteratorBase
    extends QueryIter
{
    protected T currentIt;
    protected boolean isFinished;

    public QueryIterOverQueryIteratorSupplier(ExecutionContext execCxt) {
        super(execCxt);
        this.currentIt = null;
        this.isFinished = false;
    }

//    public QueryIterOverQueryIteratorSupplier(AtomicBoolean cancelSignal) {
//        super(cancelSignal);
//        this.currentIt = null;
//        this.isFinished = false;
//    }

    @Override
    protected boolean hasNextBinding() {
        while (!isFinished && (currentIt == null || !currentIt.hasNext())) {
            synchronized (this) {
                if (currentIt != null) {
                    currentIt.close();
                }
                nextQueryIteratorInternal();
            }
        }
        return !isFinished;
    }

    @Override
    protected Binding moveToNextBinding() {
        Binding result = currentIt == null ? null : currentIt.next();
        return result;
    }

    @Override
    protected void closeIterator() {
        synchronized (this) {
            if (currentIt != null) {
                isFinished = true;
                currentIt.close();
            }
        }
    }

    @Override
    protected void requestCancel() {
        synchronized (this) {
            if (currentIt != null) {
                isFinished = true;
                currentIt.cancel();
            }
        }
    }

    protected final void nextQueryIteratorInternal() {
        synchronized (this) {
            // If currentIt is null then we consider this iterator closed.
            if (!isFinished) {
                currentIt = nextQueryIterator();
                if (currentIt == null) {
                    isFinished = true;
                }
            }
        }
    }

    protected abstract T nextQueryIterator();
}
