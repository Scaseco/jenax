package org.aksw.commons.collections.quadtree;

/**
 * Abstraction to allow use of any envelope-like object
 *
 * @param E A class that represents an envelope (aka bounding box)
 */
public interface EnvelopeAccessor<E> {
    double getMinX(E envelope);
    double getMaxX(E envelope);
    double getMinY(E envelope);
    double getMaxY(E envelope);

    // XXX Implement as default?
    boolean overlaps(E a, E b);
}
