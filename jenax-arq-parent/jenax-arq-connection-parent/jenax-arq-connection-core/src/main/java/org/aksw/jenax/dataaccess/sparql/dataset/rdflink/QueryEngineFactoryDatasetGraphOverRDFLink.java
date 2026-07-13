package org.aksw.jenax.dataaccess.sparql.dataset.rdflink;

import org.aksw.jenax.dataaccess.sparql.dataset.engine.DatasetGraphOverRDFEngine;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

/** A query engine factory over {@link DatasetGraphOverRDFEngine}.*/
public class QueryEngineFactoryDatasetGraphOverRDFLink
    implements QueryEngineFactory
{
    @Override
    public boolean accept(Query query, DatasetGraph dataset, Context context) {
        boolean result = dataset instanceof DatasetGraphOverRDFLink;
        return result;
    }

    @Override
    public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
        DatasetGraphOverRDFLink engineDsg = (DatasetGraphOverRDFLink)dataset;
        Op op = Algebra.compile(query);

        Query finalQuery = query.isSelectType()
            ? query
            : OpAsQuery.asQuery(op);

        Plan result = new PlanOverRDFLink(engineDsg, finalQuery, op, context);
        return result;
    }

    @Override
    public boolean accept(Op op, DatasetGraph dataset, Context context) {
        boolean result = dataset instanceof DatasetGraphOverRDFLink;
        return result;
    }

    @Override
    public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
        DatasetGraphOverRDFLink engineDsg = (DatasetGraphOverRDFLink)dataset;
        Query query = OpAsQuery.asQuery(op);
        Plan result = new PlanOverRDFLink(engineDsg, query, op, context);
        return result;
    }
}
