package org.aksw.jenax.arq.connection.core;

import java.util.Arrays;
import java.util.Collection;

import org.aksw.jenax.arq.connection.MultiplexUtils;
import org.aksw.jenax.arq.connection.TransactionalMultiplex;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateExecutionBuilder;
import org.apache.jena.update.UpdateRequest;

public class SparqlUpdateConnectionMultiplex
    extends TransactionalMultiplex<SparqlUpdateConnection>
    implements SparqlUpdateConnection
{

    public SparqlUpdateConnectionMultiplex(SparqlUpdateConnection ... delegates) {
        this(Arrays.asList(delegates));
    }

    public SparqlUpdateConnectionMultiplex(Collection<? extends SparqlUpdateConnection> delegates) {
        super(delegates);
    }

    @Override
    public void update(Update update) {
        MultiplexUtils.forEach(delegates, d -> d.update(update));
    }

    @Override
    public void update(UpdateRequest update) {
        MultiplexUtils.forEach(delegates, d -> d.update(update));
    }

    @Override
    public void update(String updateString) {
        MultiplexUtils.forEach(delegates, d -> d.update(updateString));
    }

    @Override
    public void close() {
        MultiplexUtils.forEach(delegates, d -> d.close());
    }

    @Override
    public UpdateExecutionBuilder newUpdate() {
         throw new UnsupportedOperationException();
    }
}
