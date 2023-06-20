package org.aksw.jenax.arq.util.node;

import java.util.Objects;

import org.apache.jena.graph.NodeVisitor;
import org.apache.jena.graph.Node_Fluid;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;


public class NodeCustom<T>
    extends Node_Fluid
{
    protected T value;

    protected NodeCustom(T value) {
        super(value);
        this.value = value;
    }

    @Override
    public Object visitWith(NodeVisitor v) {
        throw new UnsupportedOperationException();
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = this == o || this.getClass().equals(o.getClass()) && Objects.equals(value, ((NodeCustom<?>)o).getValue());
        return result;
    }

    public Expr asExpr() {
        return NodeValue.makeNode(this);
    }

    @Override
    public String toString() {
        return "NodeCustom [value=" + value + "]";
    }

    public static NodeCustom<?> create(Object value) {
        return new NodeCustom<>(value);
    }
}
