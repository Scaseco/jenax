package org.aksw.jenax.dataaccess.sparql.creator;

public interface RdfDatabaseFactory {
    RdfDatabaseBuilder<?> newBuilder();
}
