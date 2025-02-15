package org.aksw.jenax.dataaccess.sparql.linksource;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilder;

/**
 * Interface for a link-based DataSource.
 * In your application logic, prefer use of {@link RdfDataSource}.
 * For implementing custom data source transformations use this interface.
 */
public interface RdfLinkSource {
    RDFLink newLink();

    /**
     * Builder that executes a query statement on its own link.
     * Implementations must only acquire a link when the actual execution is requested.
     * The builder itself must not acquire any resources that need to be freed.
     */
    QueryExecBuilder newQuery();

    /**
     * Builder that executes an update statement on its own link.
     * Implementations must only acquire a link when the actual execution is requested.
     * The builder itself must not acquire any resources that need to be freed.
     */
    UpdateExecBuilder newUpdate();
}
