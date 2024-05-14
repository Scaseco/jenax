package org.aksw.jenax.dataaccess.sparql.link.update;

import org.aksw.jenax.arq.util.update.UpdateRequestUtils;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public interface LinkSparqlUpdateRequest
    extends LinkSparqlUpdateBase
{
    @Override
    LinkSparqlUpdate getDelegate();

    @Override
    default void update(Update update) {
        update(new UpdateRequest(update));
    }

    @Override
    default void update(UpdateRequest update) {
        getDelegate().update(update);
    }

    @Override
    default void update(String updateString) {
        update(UpdateRequestUtils.parse(updateString));
    }

    @Override
    default void close() {
        getDelegate().close();
    }

    @Override
    default UpdateExecBuilder newUpdate() {
        return getDelegate().newUpdate();
    }
}
