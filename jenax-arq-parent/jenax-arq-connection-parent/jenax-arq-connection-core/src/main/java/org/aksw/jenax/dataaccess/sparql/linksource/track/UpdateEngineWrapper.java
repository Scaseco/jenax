package org.aksw.jenax.dataaccess.sparql.linksource.track;

import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateSink;

public interface UpdateEngineWrapper
    extends UpdateEngine
{
    UpdateEngine getDelegate();

    @Override
    default void startRequest() {
        getDelegate().startRequest();
    }

    @Override
    default void finishRequest() {
        getDelegate().finishRequest();
    }

    @Override
    default UpdateSink getUpdateSink() {
        return getDelegate().getUpdateSink();
    }
}