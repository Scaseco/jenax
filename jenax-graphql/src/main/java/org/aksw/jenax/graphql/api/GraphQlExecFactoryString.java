package org.aksw.jenax.graphql.api;

@FunctionalInterface
public interface GraphQlExecFactoryString {
    GraphQlExec create(String documentString);
}
