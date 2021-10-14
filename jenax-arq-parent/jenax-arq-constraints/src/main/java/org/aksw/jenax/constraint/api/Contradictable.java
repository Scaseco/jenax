package org.aksw.jenax.constraint.api;

/** Interface for the potential of something reaching a state of contradiction */
public interface Contradictable {
    /** Whether such a state has been reached */
    boolean isConflicting();
}
