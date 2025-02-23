package org.aksw.jenax.dataaccess.sparql.pod;

import org.aksw.jenax.dataaccess.sparql.datasource.HasRDFDataSource;

/** Dataset-level pendant to RDFEngine. */
public interface RDFDataPod
    extends HasRDFDataSource, AutoCloseable
{

}
