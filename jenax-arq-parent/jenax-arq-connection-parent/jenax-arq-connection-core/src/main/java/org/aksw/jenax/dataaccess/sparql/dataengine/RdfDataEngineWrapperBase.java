package org.aksw.jenax.dataaccess.sparql.dataengine;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;

public class RdfDataEngineWrapperBase<T extends RdfDataEngine>
    extends RdfDataSourceWrapperBase<T>
    implements RdfDataEngine
{
    public RdfDataEngineWrapperBase(T delegate) {
        super(delegate);
    }

    @Override
    public void close() throws Exception {
        RdfDataEngine tmp = getDelegate();
        if (tmp != null) {
            tmp.close();
        }
    }
}
