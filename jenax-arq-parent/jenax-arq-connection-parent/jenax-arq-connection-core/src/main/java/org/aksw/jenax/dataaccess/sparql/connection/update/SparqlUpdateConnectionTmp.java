package org.aksw.jenax.dataaccess.sparql.connection.update;

import org.aksw.jenax.dataaccess.sparql.common.TransactionalWrapper;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public interface SparqlUpdateConnectionTmp
    extends TransactionalWrapper, SparqlUpdateConnection
{
    // ---- SparqlUpdateConnection

    default UpdateRequest parse(String updateString) {
        return UpdateFactory.create(updateString);
    }

    /** Execute a SPARQL Update.
     *
     * @param update
     */
    @Override
    public default void update(Update update) {
        update(new UpdateRequest(update));
    }


    /** Execute a SPARQL Update.
     *
     * @param updateString
     */
    @Override
    default void update(String updateString) {
        update(parse(updateString));
    }
}
