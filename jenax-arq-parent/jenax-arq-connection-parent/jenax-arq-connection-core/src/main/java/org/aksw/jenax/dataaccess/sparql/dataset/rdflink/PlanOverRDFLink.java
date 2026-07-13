package org.aksw.jenax.dataaccess.sparql.dataset.rdflink;

import java.util.Objects;

import org.aksw.jenax.arq.util.binding.QueryIterOverQueryExec;
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.PlanBase;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

public class PlanOverRDFLink
    extends PlanBase
{
    protected DatasetGraphOverRDFLink datasetGraph;
    protected Query query;
    protected Context context;

    public PlanOverRDFLink(DatasetGraphOverRDFLink datasetGraph, Query query, Op op, Context context) {
        super(op, null);
        this.datasetGraph = Objects.requireNonNull(datasetGraph);
        this.query = Objects.requireNonNull(query);
        this.context = Objects.requireNonNull(context);
    }

    public DatasetGraphOverRDFLink getDatasetGraph() {
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
        ExecutionContext execCxt = ExecutionContext.create(datasetGraph, context);
        @SuppressWarnings("resource")
        RDFLink link = datasetGraph.newLink();
        QueryExec qExecRaw;
        try {
            qExecRaw = link.query(query);
        } catch (Exception e) {
            link.close();
            throw new RuntimeException(e);
        }
        QueryExec qExec = new QueryExecWrapperCloseRDFLink(qExecRaw, link);
        QueryIterator result = new QueryIterOverQueryExec(execCxt, qExec);
        return result;
    }
}
