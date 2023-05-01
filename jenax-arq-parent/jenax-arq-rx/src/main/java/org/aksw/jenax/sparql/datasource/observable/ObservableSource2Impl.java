package org.aksw.jenax.sparql.datasource.observable;

import java.util.function.BiConsumer;

import org.aksw.commons.collection.observable.Registration;

public class ObservableSource2Impl<S, Q>
    implements ObservableSource2<S, Q>
{

    @Override
    public Registration addDataChangedListener(Q query, BiConsumer<S, Q> callback) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Registration addDataChangedListener(Runnable action) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refreshAll(boolean cancelRunning) {
        // TODO Auto-generated method stub

    }
}
