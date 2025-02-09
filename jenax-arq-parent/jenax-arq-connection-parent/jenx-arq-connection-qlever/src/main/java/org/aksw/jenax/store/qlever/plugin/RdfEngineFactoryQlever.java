package org.aksw.jenax.store.qlever.plugin;

import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseBuilder;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineBuilder;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactory;
import org.aksw.jenax.engine.qlever.RdfDatabaseBuilderQlever;

public class RdfEngineFactoryQlever
    implements RdfDataEngineFactory
{
    @Override
    public RdfDataEngineBuilder<?> newEngineBuilder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RdfDatabaseBuilder<?> newDatabaseBuilder() {
        return new RdfDatabaseBuilderQlever();
    }
}
