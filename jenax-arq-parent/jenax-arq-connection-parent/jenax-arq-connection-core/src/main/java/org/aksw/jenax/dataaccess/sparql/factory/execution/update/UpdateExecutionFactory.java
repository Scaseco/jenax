package org.aksw.jenax.dataaccess.sparql.factory.execution.update;

import org.apache.jena.update.UpdateProcessor;

public interface UpdateExecutionFactory
    extends UpdateProcessorFactory, AutoCloseable
{
    UpdateProcessor createUpdateProcessor(String updateRequestStr);

    <T> T unwrap(Class<T> clazz);

//    @Override
//    void close();
}
