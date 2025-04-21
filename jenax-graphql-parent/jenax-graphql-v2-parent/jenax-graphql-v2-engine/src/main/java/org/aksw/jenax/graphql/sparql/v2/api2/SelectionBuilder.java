package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.List;

import org.apache.jena.sparql.core.Var;

public interface SelectionBuilder {
    String getName();
    String getBaseName();

    Connective getConnective();
    List<Var> getParentVars();

    /**
     * Create a copy of the builder with different name for the selection. Needed to have the parent allocate a name.
     * FIXME Seems hacky.
     */
    // @Override
    // FieldLikeBuilder clone();

    SelectionBuilder clone(String finalName, List<Var> parentVars);

    // Name must be configurable
    // FieldLikeBuilder setName(String name);

    Selection build();
}
