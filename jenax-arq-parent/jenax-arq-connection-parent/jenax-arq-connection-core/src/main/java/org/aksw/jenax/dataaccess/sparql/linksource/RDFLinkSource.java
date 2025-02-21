package org.aksw.jenax.dataaccess.sparql.linksource;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

/**
 * Interface for a link-based DataSource.
 * In your application logic, prefer use of {@link RDFDataSource}.
 * For implementing custom data source transformations use this interface.
 */
@FunctionalInterface
public interface RDFLinkSource {
    RDFLink newLink();

    /**
     * Builder that executes a query statement on its own link.
     * Implementations must only acquire a link when the actual execution is requested.
     * The builder itself must not acquire any resources that need to be freed.
     */
    default QueryExecBuilder newQuery() {
        return RDFLinkSources.newQueryBuilder(this);
    }

    /**
     * Builder that executes an update statement on its own link.
     * Implementations must only acquire a link when the actual execution is requested.
     * The builder itself must not acquire any resources that need to be freed.
     */
    default UpdateExecBuilder newUpdate() {
        return RDFLinkSources.newUpdateBuilder(this);
    }

    // ----- Shorthands: Query -----

    default QueryExec query(String queryString) {
        return newQuery().query(queryString).build();
    }

    default QueryExec query(Query query) {
        return newQuery().query(query).build();
    }

    // ----- Shorthands: Update -----

    default void update(String updateRequestString) {
        newUpdate().update(updateRequestString).execute();
    }

    default void update(UpdateRequest updateRequest) {
        newUpdate().update(updateRequest).execute();
    }

    default void update(Update update) {
        newUpdate().update(update).execute();
    }
}
