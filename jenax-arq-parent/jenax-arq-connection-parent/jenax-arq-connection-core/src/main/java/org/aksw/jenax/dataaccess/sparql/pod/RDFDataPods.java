package org.aksw.jenax.dataaccess.sparql.pod;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;

public class RDFDataPods {
    public static RDFDataPod of(RDFDataSource dataSource, AutoCloseable closeAction) {
        return new RDFDataPodSimple(dataSource, closeAction);
    }
}
