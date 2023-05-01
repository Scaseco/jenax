package org.aksw.jenax.arq.util.tuple;

public interface QuadAccessor<D, C> {
    C getGraph(D tuple);
    C getSubject(D tuple);
    C getPredicate(D tuple);
    C getObject(D tuple);
}
