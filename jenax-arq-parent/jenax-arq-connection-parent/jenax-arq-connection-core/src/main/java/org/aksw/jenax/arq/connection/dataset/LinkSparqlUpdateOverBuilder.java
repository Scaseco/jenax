package org.aksw.jenax.arq.connection.dataset;

import java.util.function.Supplier;

import org.aksw.jenax.arq.connection.link.LinkSparqlUpdateTmp;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public class LinkSparqlUpdateOverBuilder
    implements LinkSparqlUpdateTmp
{
    protected Supplier<UpdateExecBuilder> updateExecBuilderFactory;
    protected Transactional transactional;

    public LinkSparqlUpdateOverBuilder(Supplier<UpdateExecBuilder> updateExecBuilderFactory) {
        this(updateExecBuilderFactory, new TransactionalNull());
    }

    public LinkSparqlUpdateOverBuilder(Supplier<UpdateExecBuilder> updateExecBuilderFactory,
            Transactional transactional) {
        super();
        this.updateExecBuilderFactory = updateExecBuilderFactory;
        this.transactional = transactional;
    }

    @Override
    public Transactional getDelegate() {
        return transactional;
    }

    @Override
    public void update(Update update) {
        newUpdate().update(update).execute();
    }

    @Override
    public void update(UpdateRequest update) {
        newUpdate().update(update).execute();

    }

    @Override
    public void update(String updateString) {
        newUpdate().update(updateString).execute();

    }

    @Override
    public UpdateExecBuilder newUpdate() {
        return updateExecBuilderFactory.get();
    }

    @Override
    public void close() {
    }
}
