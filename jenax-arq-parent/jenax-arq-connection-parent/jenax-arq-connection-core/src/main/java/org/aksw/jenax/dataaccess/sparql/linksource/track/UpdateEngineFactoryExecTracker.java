package org.aksw.jenax.dataaccess.sparql.linksource.track;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.util.Context;

public class UpdateEngineFactoryExecTracker
    implements UpdateEngineFactory
{
    @Override
    public boolean accept(DatasetGraph datasetGraph, Context context) {
        boolean result = false;
        if (datasetGraph instanceof DatasetGraphWithExecTracker tracker) {
            DatasetGraph backend = tracker.getWrapped();
            // Find factory for the unwrapped dataset but pass the wrapped one.
            // Otherwise the update processor will create queries against the unwrapped one
            //   which bypasses the exec tracker.
            result = UpdateEngineRegistry
                .findFactory(backend, context)
                .accept(tracker, context);
        }
        return result;
    }

    @Override
    public UpdateEngine create(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        DatasetGraphWithExecTracker tracker = (DatasetGraphWithExecTracker)datasetGraph;
        ExecTracker execTracker = ExecTracker.requireTracker(tracker.getContext());
        DatasetGraph backend = tracker.getWrapped();
        UpdateEngine base = UpdateEngineRegistry
            .findFactory(backend, context)
            .create(tracker, inputBinding, context);

        long[] idRef = {-1};

        UpdateEngine result = new UpdateEngineWrapperBase(base) {
            @Override
            public void startRequest() {
                AtomicBoolean cancelSignal = Context.getCancelSignal(context);
                Runnable cancelAction = (cancelSignal == null)
                    ? null
                    : () -> cancelSignal.set(true);

                idRef[0] = execTracker.put("Update request", cancelAction);
                super.startRequest();
            }

            @Override
            public void finishRequest() {
                execTracker.remove(idRef[0], null);
                super.finishRequest();
            }
        };

        return result;
    }
}
