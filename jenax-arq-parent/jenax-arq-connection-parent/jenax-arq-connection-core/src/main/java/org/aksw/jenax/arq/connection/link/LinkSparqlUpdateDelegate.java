package org.aksw.jenax.arq.connection.link;

import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public interface LinkSparqlUpdateDelegate
    extends LinkSparqlUpdateTmp
{
    @Override
    LinkSparqlUpdate getDelegate();

    @Override
    default void update(Update update) {
        newUpdate().update(update).execute();
        // getDelegate().update(update);
    }

    @Override
    default void update(UpdateRequest update) {
        newUpdate().update(update).execute();
        // getDelegate().update(update);
    }

    @Override
    default void update(String updateString) {
        newUpdate().update(updateString).execute();
        // getDelegate().update(updateString);
    }

//    @Override
//    default UpdateExecBuilder newUpdate() {
//        return getDelegate().newUpdate();
//    }

    @Override
    default void close() {
        getDelegate().close();
    }
}