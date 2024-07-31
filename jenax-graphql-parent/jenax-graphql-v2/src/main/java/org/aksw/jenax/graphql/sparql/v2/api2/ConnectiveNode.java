package org.aksw.jenax.graphql.sparql.v2.api2;

public interface ConnectiveNode {
    <T> T accept(ConnectiveVisitor<T> visitor);
}
