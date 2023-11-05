package org.aksw.jenax.connectionless;

import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionModular;
import org.aksw.jenax.dataaccess.sparql.connection.query.SparqlQueryConnectionJsa;
import org.aksw.jenax.dataaccess.sparql.connection.update.SparqlUpdateConnectionJsa;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.factory.execution.update.UpdateExecutionFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetDescription;

/**
 * FIXME This class is archaic but some code still refers to it.
 * This class should be turned into a connectionless RDFLinkModular.
 *
 * A SparqlService is an object that bundles together related sparql features
 * - i.e. querying and updating.
 *
 *
 * @author raven
 *
 */
@Deprecated /** Use {@link RdfDataSource} instead */
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
