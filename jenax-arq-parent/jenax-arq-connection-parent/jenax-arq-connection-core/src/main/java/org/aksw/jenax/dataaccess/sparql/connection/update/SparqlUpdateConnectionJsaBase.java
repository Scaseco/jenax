package org.aksw.jenax.dataaccess.sparql.connection.update;

import org.aksw.jenax.dataaccess.sparql.execution.factory.update.UpdateProcessorFactory;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.update.UpdateExecutionBuilder;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class SparqlUpdateConnectionJsaBase<T extends UpdateProcessorFactory>
    implements SparqlUpdateConnectionTmp
{
    protected T updateProcessorFactory;
    protected Transactional transactional;

    public SparqlUpdateConnectionJsaBase(T updateProcessorFactory) {
        this(updateProcessorFactory, new TransactionalNull());
    }

    public SparqlUpdateConnectionJsaBase(T updateProcessorFactory, Transactional transactional) {
        super();
        this.updateProcessorFactory = updateProcessorFactory;
        this.transactional = transactional;
    }

    @Override
    public void update(UpdateRequest updateRequest) {
        UpdateProcessor updateProcessor = updateProcessorFactory.createUpdateProcessor(updateRequest);
        updateProcessor.execute();
    }

    @Override
    public UpdateExecutionBuilder newUpdate() {
        // UpdateProcessor up = updateProcessorFactory.createUpdateProcessor(null);
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

    @Override
    public Transactional getDelegate() {
        return transactional;
    }
}
