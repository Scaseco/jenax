package org.aksw.jenax.graphql.api;

import java.util.Map;

import graphql.language.Value;

@FunctionalInterface
public interface GraphQlExecFactoryString {
    GraphQlExec create(String documentString, Map<String, Value<?>> assignments);
}
