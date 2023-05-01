package org.aksw.jenax.sparql.datasource.observable;

import java.util.function.BiConsumer;

import org.aksw.commons.collection.observable.Registration;

public interface ObservableSource2<S, Q> {
    /** Register a listener for a certain query. */
    Registration addDataChangedListener(Q query, BiConsumer<S, Q> callback);
    Registration addDataChangedListener(Runnable action);

    void refreshAll(boolean cancelRunning);

}
