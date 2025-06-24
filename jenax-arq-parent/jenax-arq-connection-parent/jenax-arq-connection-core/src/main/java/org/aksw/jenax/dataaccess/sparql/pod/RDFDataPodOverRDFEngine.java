package org.aksw.jenax.dataaccess.sparql.pod;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;

public class RDFDataPodOverRDFEngine
    implements RDFDataPod
{
    protected RDFEngine engine;

    public RDFDataPodOverRDFEngine(RDFEngine engine) {
        super();
        this.engine = Objects.requireNonNull(engine);
    }

    @Override
    public RDFDataSource getDataSource() {
        return engine.getLinkSource().asDataSource();
    }

    @Override
    public void close() throws Exception {
        engine.close();
    }

}
