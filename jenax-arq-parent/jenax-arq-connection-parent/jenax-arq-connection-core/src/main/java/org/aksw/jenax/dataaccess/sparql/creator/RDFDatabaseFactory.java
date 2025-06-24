package org.aksw.jenax.dataaccess.sparql.creator;

public interface RDFDatabaseFactory {
    RDFDatabaseBuilder<?> newBuilder();
}
