package org.aksw.jenax.dataaccess.sparql.factory.execution.query;

import org.aksw.jenax.dataaccess.sparql.factory.engine.query.QueryEngineFactoryProvider;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDataset;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;
import org.apache.jena.sparql.util.Context;

public class QueryExecutionFactoryDataset
    // implements QueryExecutionFactoryQuery
    extends QueryExecutionFactoryBackQuery
{
    protected Dataset dataset;
    protected Context context;
    protected QueryEngineFactoryProvider queryEngineFactoryProvider;


    public QueryExecutionFactoryDataset() {
        this(DatasetFactory.create());
    }

    public QueryExecutionFactoryDataset(Dataset dataset) {
        this(dataset, null);
    }

    public QueryExecutionFactoryDataset(Dataset dataset, Context context) {
        this(dataset, context, QueryEngineRegistry.get()::find);
    }

    public QueryExecutionFactoryDataset(Dataset dataset, Context context, QueryEngineFactoryProvider queryEngineFactoryProvider) {
        super();
        this.dataset = dataset;
        this.context = context;
        this.queryEngineFactoryProvider = queryEngineFactoryProvider;
    }

 // QueryEngineRegistry.get().find(query, dsg, context);

    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        // Copied from internals of jena's QueryExecutionFactory.create(query, dataset);
        query.setResultVars() ;
        if ( context == null )
            context = ARQ.getContext();  // .copy done in QueryExecutionBase -> Context.setupContext.
        DatasetGraph dsg = null ;
        if ( dataset != null )
            dsg = dataset.asDatasetGraph() ;
        QueryEngineFactory f = queryEngineFactoryProvider.find(query, dsg, context);
        if ( f == null )
        {
            Log.warn(QueryExecutionFactory.class, "Failed to find a QueryEngineFactory for query: "+query) ;
            return null ;
        }

        // Merge the contexts
        Context cxt = Context.setupContextForDataset(context, dsg);

        //dataset.begin(ReadWrite.WRITE);
        QueryExec qExec = new QueryExecDataset(query, query.toString(), dsg, cxt, f, Timeout.UNSET, null) {};
        QueryExecution result = QueryExecutionAdapter.adapt(qExec);
        // TODO We shouldn't wrap with txn here
        //QueryExecution result = QueryExecution.a// new QueryExecutionDecoratorTxn<QueryExecution>(tmp, dsg);
        return result;
    }

    @Override
    public String getId() {
        return "" + dataset.hashCode();
    }

    @Override
    public String getState() {
        return "" + dataset.hashCode();
    }
}
