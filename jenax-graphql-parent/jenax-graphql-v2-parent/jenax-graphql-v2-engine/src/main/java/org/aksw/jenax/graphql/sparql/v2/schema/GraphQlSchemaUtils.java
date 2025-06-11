package org.aksw.jenax.graphql.sparql.v2.schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformHarmonizeTentris;
import org.aksw.jenax.graphql.sparql.v2.util.GraphQlUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.system.stream.StreamManager;

import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.parser.Parser;

public class GraphQlSchemaUtils {

    public static class TypeInfo {
        private final String typeName;
        private final boolean isList;
        private final boolean isNonNull;

        public TypeInfo(String typeName, boolean isList, boolean isNonNull) {
            this.typeName = typeName;
            this.isList = isList;
            this.isNonNull = isNonNull;
        }

        public String getTypeName() {
            return typeName;
        }

        public boolean isList() {
            return isList;
        }

        public boolean isNonNull() {
            return isNonNull;
        }

        @Override
        public String toString() {
            return (isNonNull ? "NonNull " : "") +
                   (isList ? "List<" : "") +
                   typeName +
                   (isList ? ">" : "");
        }
    }

    public static TypeInfo extractTypeInfo(Type<?> type) {
        boolean isList = false;
        boolean isNonNull = false;

        // Traverse the type hierarchy
        while (true) {
            if (type instanceof NonNullType nonNullType) {
                isNonNull = true;
                type = nonNullType.getType();
            } else if (type instanceof ListType listType) {
                isList = true;
                type = listType.getType();
            } else if (type instanceof TypeName typeName) {
                return new TypeInfo(typeName.getName(), isList, isNonNull);
            } else {
                throw new IllegalArgumentException("Unknown type: " + type);
            }
        }
    }

    public static Document loadMetaSchema() {
        try {
            return loadDocument("jenax.meta.gqls");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document loadDocument(String resource) throws IOException {
        StreamManager streamMgr = StreamManager.get();
        Parser parser = new Parser();
        String metaSchemaRawStr = toStringUtf8(streamMgr, resource);
        Document metaDoc = parser.parseDocument(metaSchemaRawStr);
        return metaDoc;
    }

    public static Document merge(Document ...documents) {
        List<Definition> mergedDefinitions = new ArrayList<>();
        for (Document doc : documents) {
            mergedDefinitions.addAll(doc.getDefinitions());
        }
        Document mergedDoc = Document.newDocument().definitions(mergedDefinitions).build();
        return mergedDoc;
    }

    public static Document harmonize(Document doc) {
        Document result = GraphQlUtils.applyTransform(doc, new TransformHarmonizeTentris());
        return result;
    }

    /** Load a sparql/graphql schema resource and merge it against the meta schema. */
    public static Document loadSchema(String graphQlSchemaResource) throws IOException {
        Document metaDoc = loadMetaSchema();
        Document schemaDoc = loadDocument(graphQlSchemaResource);

        List<Definition> mergedDefinitions = new ArrayList<>();
        mergedDefinitions.addAll(metaDoc.getDefinitions());
        mergedDefinitions.addAll(schemaDoc.getDefinitions());

        // Create a new merged document
        Document mergedDoc = merge(metaDoc, schemaDoc);

        mergedDoc = harmonize(mergedDoc);
        return mergedDoc;
        // graphqlSchemaNavigator = SchemaNavigator.of(schema);
        // return graphqlSchemaNavigator;

        // GraphQlUtils.println(System.out, schemaDoc);
        // graphQlSchemaPrettyStr = AstPrinter.printAst(mergedDoc);

        // System.out.println(schemaDoc); //GraphQlUtils.toString(schemaDoc));
        // System.out.println("========");

        // SchemaParser schemaParser = new SchemaParser();
        // TypeDefinitionRegistry schema = schemaParser.buildRegistry(mergedDoc);
    }

    public static String toStringUtf8(StreamManager streamMgr, String resourceName) throws IOException {
        String result;
        try (InputStream in = streamMgr.open(resourceName)) {
            if (in == null) {
                throw new NoSuchFileException("Resource not found: " + resourceName);
            }
            result = IOUtils.toString(in, StandardCharsets.UTF_8);
        }
        return result;
    }
}
