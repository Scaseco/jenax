package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.transform.QueryExecutionFactoryDecorator;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.util.dataset.DatasetDescriptionUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetDescription;

/**
 * QueryExecutionFactory that injects dataset description into queries
 * that do not defined their own default / named graphs.
 *
 * @author raven
 *
 */
public class QueryExecutionFactoryDatasetDescription
    extends QueryExecutionFactoryDecorator
{
    protected DatasetDescription datasetDescription;

    public QueryExecutionFactoryDatasetDescription(QueryExecutionFactory delegate, DatasetDescription datasetDescription) {
        super(delegate);
        this.datasetDescription = datasetDescription;
    }

    @Override
    public String getState() {
        String result = super.getState() + DatasetDescriptionUtils.toString(datasetDescription);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {

        Query clone = query.cloneQuery();
        QueryUtils.applyDatasetDescription(clone, datasetDescription);

        QueryExecution result = super.createQueryExecution(clone);
        return result;
    }

    public QueryExecution createQueryExecution(String queryString) {
        throw new UnsupportedOperationException("This query execution requires a query to be passed as an object");
    }
}
