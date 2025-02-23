package org.aksw.jenax.dataaccess.sparql.engine;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineDecorator;
import org.aksw.jenax.dataaccess.sparql.linksource.HasRDFLinkSource;

/**
 * An RDFEngine represents a running database systems.
 * It provides a close method to shut it down and a link builder
 * in order to connect to it. The link builder can be cast
 * to specific subclasses in order to configure it. For example,
 * an RDFLinkBuilder that can be cast to an RDFLinkBuilderHTTP
 * supports configuring the content types for each SPARQL query form.
 *
 * {@link RDFDataSource} is the abstraction for a factory of readily configured links.
 * Multiple RDF data sources can be created over the same data engine, for example one
 * that retrieves SELECT queries using application/sparql-results-xml and another that uses
 * application/sparql-results+json.
 *
 * RDF data sources can be decorated with various (client-side) transformations, such
 * as macro expansion, result set limit injection, paginated execution and so on.
 *
 * The main class to bundle an engine with a datasource is
 * {@link RDFEngineDecorator}.
 */
public interface RDFEngine
    extends HasRDFLinkSource, AutoCloseable
{
    // DatasetGraph getDataset();
    // RDFLinkBuilder<?> newLinkBuilder();

//    default RDFDataSource asDataSource() {
//        return RDFDataSourceAdapter.adapt(RDFLinkSourceOverRDFEngine.of(this));
//    }
}
