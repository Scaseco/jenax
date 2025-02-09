package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;

/** Adapter to the existing infrastructure - may become deprecated. */
public abstract class RdfDataEngineFactoryLegacyBase
    implements RdfDataEngineFactory
{
    @Override
    public RdfDataEngineBuilder<?> newEngineBuilder() {
        return new RdfDataEngineBuilderBase() {
            @Override
            public RdfDataEngine build() throws Exception {
                // XXX Clash with RdfDataSourceSpecBasicFromMap.create()
                return RdfDataEngineFactoryLegacyBase.this.create(map);
            }
        };
    }

    @Override
    public abstract RdfDataEngine create(Map<String, Object> config) throws Exception;
}
