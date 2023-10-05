package org.aksw.jenax.dataaccess.sparql.execution.factory.update;

import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

@FunctionalInterface
public interface UpdateProcessorFactory
//    extends Function<UpdateRequest, UpdateProcessor>
{
    UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest);
}
