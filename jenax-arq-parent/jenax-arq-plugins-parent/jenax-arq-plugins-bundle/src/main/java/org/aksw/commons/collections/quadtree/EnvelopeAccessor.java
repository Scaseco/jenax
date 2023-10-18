package org.aksw.commons.collections.quadtree;

/**
 * Abstraction to allow use of any envelope-like object
 */
public interface EnvelopeAccessor<E> {
    double getMinX(E envelope);
    double getMaxX(E envelope);
    double getMinY(E envelope);
    double getMaxY(E envelope);

    boolean overlaps(E a, E b);
}
