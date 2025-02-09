package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceFactory;

public class RdfDataEngineFactoryOverRdfDataSourceFactory
    implements RdfDataEngineFactory
{
    protected RdfDataSourceFactory rdfDataSourceFactory;

    public RdfDataEngineFactoryOverRdfDataSourceFactory(RdfDataSourceFactory rdfDataSourceFactory) {
        super();
        this.rdfDataSourceFactory = Objects.requireNonNull(rdfDataSourceFactory);
    }

    @Override
    public RdfDataEngineBuilder<?> newEngineBuilder() {
        return new RdfDataEngineBuilderBase() {
            @Override
            public RdfDataEngine build() throws Exception {
                RdfDataSource dataSource = rdfDataSourceFactory.create(map);
                RdfDataEngine result = RdfDataEngines.of(dataSource);
                return result;
            }
        };
    }
}