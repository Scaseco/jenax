package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceAdapter;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderOverLinkSupplier;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecBuilderAdapter;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilderAdapter;
import org.apache.jena.update.UpdateExecutionBuilder;

public class RDFLinkSourceAdapter
    implements RDFLinkSource
{
    protected RDFDataSource delegate;

    public RDFLinkSourceAdapter(RDFDataSource delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public DatasetGraph getDatasetGraph() {
        Dataset ds = getDelegate().getDataset();
        DatasetGraph result = ds == null ? null : ds.asDatasetGraph();
        return result;
    }

    public RDFDataSource getDelegate() {
        return delegate;
    }

    @Override
    public RDFLinkBuilder<?> newLinkBuilder() {
        return new RDFLinkBuilderOverLinkSupplier<>(this::newLink);
    }

    @Override
    public RDFLink newLink() {
        RDFConnection conn = getDelegate().getConnection();
        RDFLink result = RDFLinkAdapter.adapt(conn);
        return result;
    }

    @Override
    public QueryExecBuilder newQuery() {
        QueryExecutionBuilder builder = getDelegate().newQuery();
        QueryExecBuilder result = QueryExecBuilderAdapter.adapt(builder);
        return result;
    }

    @Override
    public UpdateExecBuilder newUpdate() {
        UpdateExecutionBuilder builder = getDelegate().newUpdate();
        UpdateExecBuilder result = UpdateExecBuilderAdapter.adapt(builder);
        return result;
    }

    public static RDFLinkSource adapt(RDFDataSource dataSource) {
        RDFLinkSource result = dataSource instanceof RDFDataSourceAdapter adapter
            ? adapter.getLinkSource()
            : new RDFLinkSourceAdapter(dataSource);
        return result;
    }
}
