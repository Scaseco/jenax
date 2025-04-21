package org.aksw.jenax.graphql.sparql.v2.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import graphql.language.ScalarTypeDefinition;

public class SchemaNodeOverScalarTypeDefinition
    extends SchemaNodeBase
{
    protected ScalarTypeDefinition scalarTypeDefinition;

    public SchemaNodeOverScalarTypeDefinition(SchemaNavigator navigator, ScalarTypeDefinition scalarTypeDefinition) {
        super(navigator);
        this.scalarTypeDefinition = scalarTypeDefinition;
    }

    public ScalarTypeDefinition getScalarTypeDefinition() {
        return scalarTypeDefinition;
    }

    @Override
    public Optional<SchemaEdge> getEdge(String name) {
        return Optional.empty();
    }

    @Override
    public Collection<SchemaEdge> listEdges() {
        return Collections.emptyList();
    }

    @Override
    public Fragment getFragment() {
        return null;
    }
}
