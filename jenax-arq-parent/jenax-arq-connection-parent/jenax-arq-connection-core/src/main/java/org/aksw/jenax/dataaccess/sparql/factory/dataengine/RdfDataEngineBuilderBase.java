package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceSpecBasicFromMap;

public abstract class RdfDataEngineBuilderBase<X extends RDFEngineBuilder<X>>
    extends RdfDataSourceSpecBasicFromMap<X>
    implements RDFEngineBuilder<X>
{
//    @Override
//    public RdfDataEngine build() throws Exception {
//        RdfDataEngine result = factory.create(map);
//        return result;
//    }
}
