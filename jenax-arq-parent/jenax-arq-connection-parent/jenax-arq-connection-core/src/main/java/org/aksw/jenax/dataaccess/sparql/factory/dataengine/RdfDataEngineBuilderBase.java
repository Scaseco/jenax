package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceSpecBasicFromMap;

public abstract class RdfDataEngineBuilderBase<X extends RdfDataEngineBuilder<X>>
    extends RdfDataSourceSpecBasicFromMap<X>
    implements RdfDataEngineBuilder<X>
{
//    @Override
//    public RdfDataEngine build() throws Exception {
//        RdfDataEngine result = factory.create(map);
//        return result;
//    }
}
