package org.aksw.jenax.graphql.sparql.v2.api2;

import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;

public interface ConnectiveVisitor<T> {
    T visit(ElementNode field);
    T visit(FragmentSpread fragmentSpread);

    T visit(Connective connective);
    // T visit(Fragment connective);

    // T visit(SelectionSet selectionSet);
}
