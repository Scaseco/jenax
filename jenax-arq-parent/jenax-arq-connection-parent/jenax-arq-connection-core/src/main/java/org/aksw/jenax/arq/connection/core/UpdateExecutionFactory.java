package org.aksw.jenax.arq.connection.core;

import org.aksw.jenax.connection.update.UpdateProcessorFactory;
import org.apache.jena.update.UpdateProcessor;

public interface UpdateExecutionFactory
    extends UpdateProcessorFactory, AutoCloseable
{
    UpdateProcessor createUpdateProcessor(String updateRequestStr);

    <T> T unwrap(Class<T> clazz);

//    @Override
//    void close();
}
