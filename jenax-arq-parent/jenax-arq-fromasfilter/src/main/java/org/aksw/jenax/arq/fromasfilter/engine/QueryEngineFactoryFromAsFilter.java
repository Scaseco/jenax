package org.aksw.jenax.arq.fromasfilter.engine;

import org.aksw.jenax.arq.fromasfilter.dataset.DatasetGraphFromAsFilter;
import org.aksw.jenax.arq.util.syntax.ElementTransformDatasetDescription;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DynamicDatasets.DynamicDatasetGraph;
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

    public static DatasetGraph unwrapDynamicDataset(DatasetGraph dataset) {
        return dataset instanceof DynamicDatasetGraph
                ? ((DynamicDatasetGraph)dataset).getProjected()
                : dataset;
    }

    @Override
    public boolean accept(Query query, DatasetGraph dataset, Context context) {
        return unwrapDynamicDataset(dataset) instanceof DatasetGraphFromAsFilter;
    }

    public static Pair<Query, DatasetGraph> unwrapDynamicDataset(Query query, DatasetGraph dataset) {
        Pair<Query, DatasetGraph> result;
        if (dataset instanceof DynamicDatasetGraph) {
            DynamicDatasetGraph ddg = (DynamicDatasetGraph)dataset;
            Query copy = query.cloneQuery();
            copy.getGraphURIs().clear();
            copy.getNamedGraphURIs().clear();
            ddg.getDefaultGraphs().forEach(n -> copy.getGraphURIs().add(n.getURI()));
            ddg.getNamedGraphs().forEach(n -> copy.getNamedGraphURIs().add(n.getURI()));

            result = Pair.create(copy, ddg.getProjected());
        } else {
            result = Pair.create(query, dataset);
        }
        return result;
    }

    @Override
    public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
        Pair<Query, DatasetGraph> effective = unwrapDynamicDataset(query, dataset);
        Query effectiveQuery = effective.getLeft();
        DatasetGraph affectiveDataset = effective.getRight();

        DatasetGraphFromAsFilter wrapper = (DatasetGraphFromAsFilter)affectiveDataset;
        DatasetGraph base = wrapper.getBase();

        Query rewrittenQuery = ElementTransformDatasetDescription.rewrite(effectiveQuery);

        // Switch to debug?
        if (logger.isInfoEnabled()) {
            logger.info("Transformed FROM (NAMED) clauses to filters:\n" + rewrittenQuery);
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
