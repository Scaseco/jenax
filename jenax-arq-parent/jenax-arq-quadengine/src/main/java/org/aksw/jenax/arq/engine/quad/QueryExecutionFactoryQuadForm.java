package org.aksw.jenax.arq.engine.quad;

import org.aksw.jena_sparql_api.arq.core.query.QueryExecutionFactoryDataset;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionFactoryQuadForm {
    public static QueryExecution create(Query query, Dataset dataset) {
        return new QueryExecutionFactoryDataset(
                    dataset, null, (q, d, c) -> QueryEngineMainQuadForm.FACTORY)
                .createQueryExecution(query);

        // return new QueryExecutionBase(query, dataset, null, QueryEngineMainQuadForm.FACTORY);
    }
}
