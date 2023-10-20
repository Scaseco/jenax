package org.aksw.jenax.dataaccess.sparql.link.update;

import org.aksw.jenax.dataaccess.sparql.common.TransactionalWrapper;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

/** Default methods that delegate everything to the updateBuilder */
public interface LinkSparqlUpdateTmp
    extends TransactionalWrapper, LinkSparqlUpdate
{
    @Override
    default void update(String updateString) {
        newUpdate().update(updateString).build().execute();
    }

    @Override
    default void update(Update update) {
        newUpdate().update(update).build().execute();
    }

    @Override
    default void update(UpdateRequest updateRequest) {
        newUpdate().update(updateRequest).build().execute();
    }
}
