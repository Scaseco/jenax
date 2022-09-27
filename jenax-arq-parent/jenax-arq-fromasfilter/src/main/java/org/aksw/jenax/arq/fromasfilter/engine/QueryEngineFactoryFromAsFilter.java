package org.aksw.jenax.arq.fromasfilter.engine;

import org.aksw.jenax.arq.util.syntax.ElementTransformDatasetDescription;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryEngineFactoryFromAsFilter
    implements QueryEngineFactory
{
    private static final Logger logger = LoggerFactory.getLogger(QueryEngineFactoryFromAsFilter.class);

    @Override
    public boolean accept(Query query, DatasetGraph dataset, Context context) {
        return dataset instanceof DatasetGraphFromAsFilter;
    }

    @Override
    public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
        DatasetGraphFromAsFilter wrapper = (DatasetGraphFromAsFilter)dataset;
        DatasetGraph base = wrapper.getBase();

        Query rewrittenQuery = ElementTransformDatasetDescription.rewrite(query);

        System.err.println("Rewritten Query: " + rewrittenQuery);
        if (logger.isDebugEnabled()) {
            logger.debug("Rewrote from/named clauses: " + rewrittenQuery);
        }


        QueryEngineFactory qef = QueryEngineRegistry.findFactory(rewrittenQuery, base, context);
        return qef.create(rewrittenQuery, base, inputBinding, context);
    }

    @Override
    public boolean accept(Op op, DatasetGraph dataset, Context context) {
        return false;
    }

    @Override
    public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
        throw new UnsupportedOperationException();
    }

}
