package org.aksw.jenax.dataaccess.sparql.connection.update;

import org.aksw.jenax.dataaccess.sparql.factory.execution.update.UpdateExecutionFactory;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;

public class SparqlUpdateConnectionJsa
   extends SparqlUpdateConnectionJsaBase<UpdateExecutionFactory>
{
    public SparqlUpdateConnectionJsa(UpdateExecutionFactory updateExecutionFactory) {
        this(updateExecutionFactory, new TransactionalNull());
    }

    public SparqlUpdateConnectionJsa(UpdateExecutionFactory updateExecutionFactory, Transactional transactional) {
        super(updateExecutionFactory, transactional);
    }

    @Override
    public void close() {
        try {
            updateProcessorFactory.close();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
