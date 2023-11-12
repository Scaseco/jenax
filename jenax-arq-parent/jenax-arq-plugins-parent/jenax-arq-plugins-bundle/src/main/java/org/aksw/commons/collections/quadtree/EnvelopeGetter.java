package org.aksw.commons.collections.quadtree;

public interface EnvelopeGetter<T, E> {
    E getEnvelope(T item);
}
