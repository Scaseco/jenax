package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceAdapter;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderOverLinkSupplier;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilder;

/** All implemented methods are based on {@link RDFDataSource#getConnection()}. */
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
        Dataset ds = asDataSource().getDataset();
        DatasetGraph result = ds == null ? null : ds.asDatasetGraph();
        return result;
    }

    @Override
    public RDFDataSource asDataSource() {
        return delegate;
    }

    @Override
    public RDFLinkBuilder<?> newLinkBuilder() {
        return new RDFLinkBuilderOverLinkSupplier<>(this::newLink);
    }

    @Override
    public RDFLink newLink() {
        RDFConnection conn = asDataSource().getConnection();
        RDFLink result = RDFLinkAdapter.adapt(conn);
        return result;
    }

    @Override
    public QueryExecBuilder newQuery() {
        // Create a RDFLinkSource view over this. newLinkBuilder().
        RDFLinkSource view = () -> newLinkBuilder();
        QueryExecBuilder result = view.newQuery();
        return result;
    }

    @Override
    public UpdateExecBuilder newUpdate() {
        // Create a RDFLinkSource view over this. newLinkBuilder().
        RDFLinkSource view = () -> newLinkBuilder();
        UpdateExecBuilder result = view.newUpdate();

//        UpdateExecutionBuilder builder = asDataSource().newUpdate();
//        UpdateExecBuilder result = UpdateExecBuilderAdapter.adapt(builder);
        return result;
    }

    public static RDFLinkSource adapt(RDFDataSource dataSource) {
        RDFLinkSource result = dataSource instanceof RDFDataSourceAdapter adapter
            ? adapter.asLinkSource()
            : new RDFLinkSourceAdapter(dataSource);
        return result;
    }
}
