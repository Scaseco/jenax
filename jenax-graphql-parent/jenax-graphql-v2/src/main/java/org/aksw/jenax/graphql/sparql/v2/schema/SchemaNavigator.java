package org.aksw.jenax.graphql.sparql.v2.schema;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

import graphql.language.ObjectTypeDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;

public class SchemaNavigator {
    protected TypeDefinitionRegistry schema;
    protected Map<String, SchemaNode> typeNameToSchemaNode = new HashMap<>();

    /** Global prefixes (supplied externally - not part of the schema) */
    protected PrefixMap globalPrefixMap;

    protected volatile PrefixMap queryPrefixMap;

    /** base = global union query */
    protected PrefixMap basePrefixMap;

    public SchemaNavigator(TypeDefinitionRegistry schema, PrefixMap globalPrefixMap) {
        super();
        this.schema = schema;
        this.globalPrefixMap = globalPrefixMap;
    }

    public PrefixMap getGlobalPrefixMap() {
        return globalPrefixMap;
    }

    public PrefixMap getBasePrefixMap() {
        return basePrefixMap;
    }

//    Optional<SchemaNode> get(String typeName) {
//        TypeDefinition typeDef = schema.getType(typeName).orElse(null);
//
//        if (typeDef instanceof ObjectTypeDefinition odt) {
//        }
//
//    }

    public SchemaNode getOrCreateSchemaNode(String typeName) {
        TypeDefinition<?> typeDefinition = schema.getType(typeName)
                .orElseThrow(() -> new RuntimeException("No type: " + typeName));

        SchemaNode result;
        if (typeDefinition instanceof ObjectTypeDefinition otd) {
            result = typeNameToSchemaNode.computeIfAbsent(typeName, t -> {
                return new SchemaNodeOverObjectTypeDefinition(this, otd);
            });
        } else if (typeDefinition instanceof ScalarTypeDefinition stt){
            return new SchemaNodeOverScalarTypeDefinition(this, stt);
        } else {
            throw new RuntimeException("Currently unsupported type definition: " + typeDefinition);
        }
        return result;
    }

    public static SchemaNavigator of(TypeDefinitionRegistry schema) {
        return new SchemaNavigator(schema, PrefixMapFactory.emptyPrefixMap());
    }
}
