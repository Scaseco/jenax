package org.aksw.jenax.graphql.sparql.v2.schema;


import java.nio.file.Path;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class SchemaProcessor {
    public static void main(String[] args) {
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry schema = schemaParser.parse(Path.of("/home/raven/Projects/Eclipse/graphql-eval/lingbm-rpt/tentris-lingbm.graphql").toFile());

        // Basic Idea:
        // In the graphql to sparql converter use the schema annotations as a base
        // The main question is: Do annotations on the field override or extend schema annotations?
        // Probably we can have some flag like @override - but I suppose override is the default.
        // I mean: if the field is defined in the schema then why would one override it?

        SchemaNavigator navigator = SchemaNavigator.of(schema);

        // System.out.println(schema.types());
//        ObjectTypeDefinition ot = schema.getType("Query", ObjectTypeDefinition.class).orElse(null);
//        ot.getFieldDefinitions();
//        ot.getimple
//        System.out.println(ot);
    }
}
