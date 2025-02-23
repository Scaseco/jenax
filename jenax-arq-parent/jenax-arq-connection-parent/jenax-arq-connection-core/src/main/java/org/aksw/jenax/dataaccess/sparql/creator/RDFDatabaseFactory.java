package org.aksw.jenax.dataaccess.sparql.creator;

public interface RDFDatabaseFactory {
    RdfDatabaseBuilder<?> newBuilder();
}
