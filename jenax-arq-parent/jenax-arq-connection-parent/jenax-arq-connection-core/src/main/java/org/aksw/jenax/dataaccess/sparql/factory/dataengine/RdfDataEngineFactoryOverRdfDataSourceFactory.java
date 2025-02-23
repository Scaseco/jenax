package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceFactory;

public class RdfDataEngineFactoryOverRdfDataSourceFactory
    implements RDFEngineFactory
{
    protected RdfDataSourceFactory rdfDataSourceFactory;

    public RdfDataEngineFactoryOverRdfDataSourceFactory(RdfDataSourceFactory rdfDataSourceFactory) {
        super();
        this.rdfDataSourceFactory = Objects.requireNonNull(rdfDataSourceFactory);
    }

    @Override
    public RDFEngineBuilder<?> newEngineBuilder() {
        return new RdfDataEngineBuilderBase() {
            @Override
            public RDFEngine build() throws Exception {
                RDFDataSource dataSource = rdfDataSourceFactory.create(map);
                RDFEngine result = null;
                if (true) {
                    throw new RuntimeException("TODO Migrate");
                }
                // RDFEngine result = RdfDataEngines.of(dataSource);
                return result;
            }
        };
    }
}
