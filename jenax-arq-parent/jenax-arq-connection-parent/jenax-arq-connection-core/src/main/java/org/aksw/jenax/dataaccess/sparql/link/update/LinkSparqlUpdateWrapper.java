package org.aksw.jenax.dataaccess.sparql.link.update;

import org.apache.jena.rdflink.LinkSparqlUpdate;

public interface LinkSparqlUpdateWrapper
    extends LinkSparqlUpdateBase
{
    @Override
    LinkSparqlUpdate getDelegate();

//    @Override
//    default void update(Update update) {
//        newUpdate().update(update).execute();
//        // getDelegate().update(update);
//    }
//
//    @Override
//    default void update(UpdateRequest update) {
//        newUpdate().update(update).execute();
//        // getDelegate().update(update);
//    }
//
//    @Override
//    default void update(String updateString) {
//        newUpdate().update(updateString).execute();
//        // getDelegate().update(updateString);
//    }

//    @Override
//    default UpdateExecBuilder newUpdate() {
//        return getDelegate().newUpdate();
//    }

    @Override
    default void close() {
        getDelegate().close();
    }
}
