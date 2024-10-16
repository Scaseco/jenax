package org.aksw.jenax.dataaccess.sparql.factory.dataset.connection;

import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.factory.engine.query.QueryEngineFactoryProvider;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDataset;
import org.apache.jena.sparql.util.Context;

public class QueryExecDatasetBuilderEx<T extends QueryExecDatasetBuilderEx<T>>
    extends QueryExecBuilderCustomBase<T> {

    protected DatasetGraph dataset;
    protected QueryEngineFactoryProvider queryEngineFactoryProvider;

    public QueryExecDatasetBuilderEx() {
        super();
    }

    public QueryExecDatasetBuilderEx(DatasetGraph dataset, QueryEngineFactoryProvider queryEngineFactoryProvider) {
        this();
        dataset(dataset);
        queryEngineFactoryProvider(queryEngineFactoryProvider);
    }

    public T dataset(DatasetGraph dataset) {
        this.dataset = dataset;
        return self();
    }

    public T queryEngineFactoryProvider(QueryEngineFactoryProvider queryEngineFactoryProvider) {
        this.queryEngineFactoryProvider = queryEngineFactoryProvider;
        return self();
    }

    @Override
    public QueryExec build() {
        Query query = getParsedQuery();
        Context localCxt = getContext();
        Context finalCxt = Context.setupContextForDataset(localCxt, dataset);

        // finalCxt = ObjectUtils.mergeNonNull(ARQ.getContext(), finalCxt, Context::mergeCopy);

        query.setResultVars();
        QueryEngineFactory f = queryEngineFactoryProvider.find(query, dataset, finalCxt);
        if (f == null) {
            Log.warn(QueryExecutionFactory.class, "Failed to find a QueryEngineFactory for query: " + query);
            return null ;
        }

        // Merge the contexts
        // The delegate context should already include the dataset context
        // Context cxt = Context.setupContextForDataset(context, dsg);

        //dataset.begin(ReadWrite.WRITE);
        Binding initialBinding = substitution.build();

        defaultTimeoutsFromContext(this, finalCxt);

        QueryExec qExec = new QueryExecDataset(query, query.toString(), dataset, finalCxt, f, initialTimeoutValue, initialTimeoutUnit, overallTimeoutValue, overallTimeoutUnit, initialBinding) {};
        return qExec;
    }
}
