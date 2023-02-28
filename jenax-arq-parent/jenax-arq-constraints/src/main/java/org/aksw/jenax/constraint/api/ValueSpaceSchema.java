package org.aksw.jenax.constraint.api;

import java.util.Map;

/** ValueSpaceMetadata */
public interface ValueSpaceSchema {
    Map<Object, Dimension> getDimensions();

    // Create a new open value space
    // Create a new closed value space
    // Create a value space for a certain dimension (should this method be part of the dimension or the schema?)
}
