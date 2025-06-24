package org.aksw.jenax.dataaccess.sparql.link.transform;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryWrapperBase;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecApp;
import org.apache.jena.sparql.exec.QueryExecBuilder;

/**
 * A wrapper that combines the generated query execution
 * with a dataset graph. This is needed to make the {@link Resource}
 * abstraction work. This wrapper should only be applied as the last one
 * in a sequence of link transformations.
 */
public class LinkSparqlQueryApp
    extends LinkSparqlQueryWrapperBase
{
    protected DatasetGraph datasetGraph;

    public LinkSparqlQueryApp(LinkSparqlQuery delegate, DatasetGraph datasetGraph) {
        super(Objects.requireNonNull(delegate));
        this.datasetGraph = Objects.requireNonNull(datasetGraph);
    }

    @Override
    public QueryExecBuilder newQuery() {
        return new QueryExecBuilderCustomBase<>() {
            @Override
            public QueryExec build() {
                QueryExecBuilder delegateQueryExecBuilder = getDelegate().newQuery();
                applySettings(delegateQueryExecBuilder);
                QueryExec r = QueryExecApp.create(delegateQueryExecBuilder, datasetGraph, query, queryString);
                return r;
            }
        };
    }
}
