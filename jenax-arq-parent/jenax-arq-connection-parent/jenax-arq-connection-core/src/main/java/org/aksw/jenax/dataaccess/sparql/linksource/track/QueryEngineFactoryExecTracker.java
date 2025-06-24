package org.aksw.jenax.dataaccess.sparql.linksource.track;

import org.aksw.jenax.dataaccess.sparql.linksource.track.ExecTracker.CompletionRecord;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactoryWrapper;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.apache.jena.sparql.util.Context;

public class QueryEngineFactoryExecTracker
    extends QueryEngineFactoryWrapper
{
    @Override
    public boolean accept(Query query, DatasetGraph dataset, Context context) {
        boolean result = false;
        if (dataset instanceof DatasetGraphWithExecTracker tracker) {
            DatasetGraph backend = tracker.getWrapped();
            result = QueryEngineRegistry
                .findFactory(query, backend, context)
                .accept(query, backend, context);
        }
        return result;
    }

    @Override
    public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
        DatasetGraphWithExecTracker tracker = (DatasetGraphWithExecTracker)dataset;
        ExecTracker execTracker = ExecTracker.requireTracker(tracker.getContext());
        DatasetGraph next = tracker.getWrapped();
        Plan base = QueryEngineRegistry
            .findFactory(query, next, context)
            .create(query, next, inputBinding, context);
        return new TrackingPlan(base, execTracker, context, query);
    }

    @Override
    public boolean accept(Op op, DatasetGraph dataset, Context context) {
        boolean result = false;
        if (dataset instanceof DatasetGraphWithExecTracker tracker) {
            DatasetGraph next = tracker.getWrapped();
            result = QueryEngineRegistry
                .findFactory(op, next, context)
                .accept(op, next, context);
        }
        return result;
    }

    @Override
    public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
        DatasetGraphWithExecTracker tracker = (DatasetGraphWithExecTracker)dataset;
        ExecTracker execTracker = ExecTracker.requireTracker(tracker.getContext());
        DatasetGraph next = tracker.getWrapped();
        Plan base = QueryEngineRegistry
            .findFactory(op, next, context)
            .create(op, next, inputBinding, context);
        return new TrackingPlan(base, execTracker, context, op);
    }

    private static class TrackingPlan
        extends PlanWrapperBase
    {
        protected ExecTracker execTracker;
        protected Object label;
        protected Context context;

        public TrackingPlan(Plan delegate, ExecTracker execTracker, Context context, Object label) {
            super(delegate);
            this.execTracker = execTracker;
            this.context = context;
            this.label = label;
        }

        @Override
        public QueryIterator iterator() {
            QueryIterator base = getDelegate().iterator();

            // Set before this method returns.
            long[] idRef = {-1};

            QueryIterator result = new QueryIteratorWrapper(base) {
                protected Throwable t = null;

                @Override
                protected boolean hasNextBinding() {
                    try {
                        return super.hasNextBinding();
                    } catch (Throwable throwable) {
                        throwable.addSuppressed(new RuntimeException("Tracked exception"));
                        t = throwable;
                        throw throwable;
                    }
                }

                @Override
                protected Binding moveToNextBinding() {
                    try {
                        return super.moveToNextBinding();
                    } catch (Throwable throwable) {
                        throwable.addSuppressed(new RuntimeException("Tracked exception"));
                        t = throwable;
                        throw throwable;
                    }
                }

                @Override
                protected void closeIterator() {
                    // execTracker.remove(idRef[0], t);
                    CompletionRecord completionRecord = execTracker.remove(idRef[0], t);
                    System.err.println(completionRecord);
                    super.closeIterator();
                }
            };
            idRef[0] = execTracker.put(label, result::cancel);
            return result;
        }
    }
}
