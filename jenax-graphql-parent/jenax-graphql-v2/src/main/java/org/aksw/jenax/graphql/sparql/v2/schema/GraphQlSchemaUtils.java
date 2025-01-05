package org.aksw.jenax.graphql.sparql.v2.schema;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;

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
}
