package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

public interface Provider<T> {
    T create(String name);
}
