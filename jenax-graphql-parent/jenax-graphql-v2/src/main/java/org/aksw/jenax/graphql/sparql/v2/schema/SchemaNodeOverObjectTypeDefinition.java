package org.aksw.jenax.graphql.sparql.v2.schema;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import graphql.language.DirectivesContainer;
import graphql.language.ObjectTypeDefinition;

public class SchemaNodeOverObjectTypeDefinition
    implements SchemaNode
{
    protected SchemaNavigator navigator;
    protected ObjectTypeDefinition objectTypeDefinition;

    protected Map<String, SchemaNode> superNodes = new LinkedHashMap<>();
    protected Map<String, SchemaEdge> edges = null;

    // A SPARQL pattern that specifies the relation for this type. May be null.
    protected Fragment fragment;

    public SchemaNodeOverObjectTypeDefinition(SchemaNavigator navigator, ObjectTypeDefinition objectTypeDefinition) {
        super();
        this.navigator = navigator;
        this.objectTypeDefinition = objectTypeDefinition;
    }

    public ObjectTypeDefinition getObjectTypeDefinition() {
        return objectTypeDefinition;
    }

//    public static Fragment parseFragment(DirectivesContainer<?> node) {
//        return null;
//    }

    @Override
    public Fragment getFragment() {
        return fragment;
    }

    protected void initEdges() {
        if (edges == null) {
            edges = objectTypeDefinition.getFieldDefinitions().stream().map(fd -> {
                return new SchemaEdge(navigator, objectTypeDefinition, fd);
            }).collect(Collectors.toMap(SchemaEdge::getName, x -> x));
        }
    }

    @Override
    public Optional<SchemaEdge> getEdge(String name) {
        initEdges();
        return Optional.ofNullable(edges.get(name));
    }

    @Override
    public Collection<SchemaEdge> listEdges() {
        initEdges();
        return edges.values();
    }

    @Override
    public String toString() {
        return "SchemaNode [" + objectTypeDefinition.getName() + "]";
    }
}

// If the type corresponds to inputs or outputs of a property then the type
// can reference it
// @fromOf(type: "typeDefinitionName", field: "fieldDefinitionName")
// @toOf(type: "typeDefinitionName", field: "fieldDefinitionName")

