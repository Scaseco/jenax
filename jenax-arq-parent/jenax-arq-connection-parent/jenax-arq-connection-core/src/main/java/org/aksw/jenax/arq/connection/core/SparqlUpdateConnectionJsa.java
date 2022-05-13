package org.aksw.jenax.arq.connection.core;

import org.aksw.jenax.arq.connection.SparqlUpdateConnectionJsaBase;
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
