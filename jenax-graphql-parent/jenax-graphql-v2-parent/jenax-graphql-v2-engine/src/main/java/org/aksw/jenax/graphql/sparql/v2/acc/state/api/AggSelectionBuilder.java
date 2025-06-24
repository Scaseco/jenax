package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A builder for a set of sub-selections.
 *
 */
public class AggSelectionBuilder<K> {

    protected final AggSelectionBuilder<K> parent;

    protected final Map<String, AggSelectionBuilder<K>> subSelectionsByName = new LinkedHashMap<>();
    protected final Map<K, AggSelectionBuilder<K>> subSelectionsByKey = new LinkedHashMap<>();

    public AggSelectionBuilder(AggSelectionBuilder<K> parent) {
        super();
        this.parent = parent;
    }

    void newMapBuilder() {}

    /** Introduce a new field with a fixed name that does not introduce a new graph pattern.
     *  The parent's graph patterns variables are accessible from it. */
    void newHollow() {}

    /** Introduce a new field with a fixed name. */
    AggFieldBuilder newField() { return null; }

    /** Return an object that references a variable. Can be used to pass variables from ancestors to a builder. */
    void newVarRef(String varName) {}
}
