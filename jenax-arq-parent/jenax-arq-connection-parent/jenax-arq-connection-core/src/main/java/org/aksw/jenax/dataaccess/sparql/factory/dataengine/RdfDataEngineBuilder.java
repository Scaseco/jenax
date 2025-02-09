package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceSpecBasicMutable;

public interface RdfDataEngineBuilder<X extends RdfDataEngineBuilder<X>>
    extends RdfDataSourceSpecBasicMutable<X>
{
    RdfDataEngine build() throws Exception;
}
