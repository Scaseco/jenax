package org.aksw.jenax.dataaccess.sparql.datasource;

import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.exec.QueryExecBuilder;

/**
 * Interface for a link-based DataSource.

 * Prefer {@link RdfDataSource} over this interface in your application logic!
 * You can obtain a link source view over an rdf data source using {@link RdfDataSource#asLinkSource()}.
 */
public interface RdfLinkSource {
    RDFLink newLink();
    QueryExecBuilder newQuery();
    RdfDataSource asDataSource();
}
