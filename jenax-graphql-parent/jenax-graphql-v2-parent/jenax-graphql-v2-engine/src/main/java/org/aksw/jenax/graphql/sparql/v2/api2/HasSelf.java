package org.aksw.jenax.graphql.sparql.v2.api2;

public interface HasSelf<T> {
    @SuppressWarnings("unchecked")
    default T self() {
        return (T)this;
    }
}
