package org.aksw.jenax.dataaccess.sparql.dataset.engine;

import java.util.Objects;

import org.aksw.jenax.arq.util.binding.QueryIterOverQueryExec;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.PlanBase;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

/** A query engine factory over DatasetGraphOverRDFDataSource.*/
public class QueryEngineFactoryOverRDFDataSource
    implements QueryEngineFactory
{
    @Override
    public boolean accept(Query query, DatasetGraph dataset, Context context) {
        boolean result = dataset instanceof DatasetGraphOverRDFEngine;
        return result;
    }

    @Override
    public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
        DatasetGraphOverRDFEngine engineDsg = (DatasetGraphOverRDFEngine)dataset;
        Op op = Algebra.compile(query);

        Query finalQuery = query.isSelectType()
            ? query
            : OpAsQuery.asQuery(op);

        Plan result = new PlanOverRDFEngine(engineDsg, finalQuery, op, context);
        return result;
    }

    @Override
    public boolean accept(Op op, DatasetGraph dataset, Context context) {
        boolean result = dataset instanceof DatasetGraphOverRDFEngine;
        return result;
    }

    @Override
    public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
        DatasetGraphOverRDFEngine engineDsg = (DatasetGraphOverRDFEngine)dataset;
        Query query = OpAsQuery.asQuery(op);
        Plan result = new PlanOverRDFEngine(engineDsg, query, op, context);
        return result;
    }

    private static class PlanOverRDFEngine
        extends PlanBase
    {
        protected DatasetGraphOverRDFEngine datasetGraph;
        protected Query query;
        protected Context context;

        public PlanOverRDFEngine(DatasetGraphOverRDFEngine datasetGraph, Query query, Op op, Context context) {
            super(op, null);
            this.datasetGraph = Objects.requireNonNull(datasetGraph);
            this.query = Objects.requireNonNull(query);
            this.context = Objects.requireNonNull(context);
        }

        public DatasetGraphOverRDFEngine getDatasetGraph() {
            return datasetGraph;
        }

        public Query getQuery() {
            return query;
        }

        public Context getContext() {
            return context;
        }

        @Override
        public QueryIterator iteratorOnce() {
            RDFEngine engine = datasetGraph.getEngine();
            ExecutionContext execCxt = ExecutionContext.create(datasetGraph, context);
            QueryExec qExec = engine.getLinkSource().query(query);
            QueryIterator result = new QueryIterOverQueryExec(execCxt, qExec);
            return result;
        }
    }
}
