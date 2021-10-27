package org.aksw.jenax.connectionless;

import org.aksw.jenax.arq.connection.RDFConnectionModular;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.connection.core.SparqlQueryConnectionJsa;
import org.aksw.jenax.arq.connection.core.SparqlUpdateConnectionJsa;
import org.aksw.jenax.arq.connection.core.UpdateExecutionFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetDescription;

/**
 * TODO This class should be turned into a connectionless RDFLinkModular.
 *
 * A SparqlService is an object that bundles together related sparql features
 * - i.e. querying and updating.
 *
 *
 * @author raven
 *
 */
public interface SparqlService
//    extends AutoCloseable
{
    /**
     * Returns the default dataset description associated with this service.
     * May be null.
     * @return
     */
    String getServiceUri();
    DatasetDescription getDatasetDescription();

    QueryExecutionFactory getQueryExecutionFactory();
    UpdateExecutionFactory getUpdateExecutionFactory();

    default RDFConnection getRDFConnection() {
        return new RDFConnectionModular(
                new SparqlQueryConnectionJsa(getQueryExecutionFactory()),
                new SparqlUpdateConnectionJsa(getUpdateExecutionFactory()),
                null);
    }
}
