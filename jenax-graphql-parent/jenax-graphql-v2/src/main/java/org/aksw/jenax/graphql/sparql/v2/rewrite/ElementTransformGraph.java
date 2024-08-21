package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.Objects;
import java.util.Set;

import org.aksw.jenax.graphql.sparql.v2.api2.ElementTransform;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementNamedGraph;

public class ElementTransformGraph
    implements ElementTransform
{
    protected Node node;


    public ElementTransformGraph(Node node) {
        super();
        this.node = Objects.requireNonNull(node);
    }

    @Override
    public Element apply(Element t) {
        return new ElementNamedGraph(node, t);
    }

    @Override
    public Set<Var> getDeclaredVariables() {
        return node instanceof Var v ? Set.of(v) : Set.of();
    }

    @Override
    public String toString() {
        return "ElementTransformGraph [node=" + node + "]";
    }
}
