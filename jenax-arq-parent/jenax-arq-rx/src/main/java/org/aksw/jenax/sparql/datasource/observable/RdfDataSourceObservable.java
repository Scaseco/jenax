package org.aksw.jenax.sparql.datasource.observable;

import org.aksw.jenax.arq.util.binding.ResultTable;
import org.apache.jena.query.Query;

import io.reactivex.rxjava3.core.Flowable;

public interface RdfDataSourceObservable {
    /**
     * Returns a flowable for the result table of the given query.
     * The flowable is idle until subscription.
     * Upon subscription either the query is executed or if there is a cached value from a prior
     * execution then this one will be re-emitted.
     * To re-execute a query it needs to be refreshed e.g. via {@link #refreshAll()}.
     *
     * If no change is detected after a refresh then no additional event should be sent.
     */
    Flowable<ResultTable> observeSelect(Query query);

    void refreshAll(boolean cancelRunning);
}
