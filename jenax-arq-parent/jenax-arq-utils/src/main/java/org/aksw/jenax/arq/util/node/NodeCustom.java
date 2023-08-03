package org.aksw.jenax.arq.util.node;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeVisitor;
import org.apache.jena.graph.Node_Fluid;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformExpr;


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
        // return NodeValue.makeNode(this);
        return ExprLib.nodeToExpr(this);
    }

    @Override
    public String toString() {
        return "NodeCustom [value=" + value + "]";
    }

    @Deprecated // Use of()
    public static NodeCustom<?> create(Object value) {
        return new NodeCustom<>(value);
    }

    public static <T> NodeCustom<T> of(T value) {
        return new NodeCustom<>(value);
    }

    public static <T> Set<T> mentionedValues(Expr expr) {
        @SuppressWarnings("unchecked")
        Set<T> result = ExprUtils.nodesMentioned(expr).stream()
                .flatMap(x -> ObjectUtils.tryCastAs(NodeCustom.class, x).stream())
                .map(NodeCustom::getValue)
                .map(x -> (T)x)
                .collect(Collectors.toCollection(HashSet::new));
        return result;
    }


    /** Substitute all referenced paths in an expression w.r.t. the given path mapping */
    public static <T> Expr resolveExpr(Function<T, Node> mapping, Expr expr) {
        NodeTransform nodeTransform = createNodeTransform(mapping);
        Expr result = ExprTransformer.transform(new NodeTransformExpr(nodeTransform), expr);
        return result;
    }

    /** Create a NodeTransform for substituting NodeFacetPath instances with variables */
    @SuppressWarnings("unchecked")
    public static <T> NodeTransform createNodeTransform(Function<? super T, ? extends Node> mapping) {
        return NodeTransformLib2.wrapWithNullAsIdentity(x -> x instanceof NodeCustom
            ? mapping.apply(((NodeCustom<T>)x).getValue())
            : null);
    }
    
    /** Create a NodeTransform that transforms the payload of NodeCustom instances */
    @SuppressWarnings("unchecked")
    public static <I, O> NodeTransform mapValue(Function<? super I, O> mapping) {
    	return createNodeTransform((I before) -> {
			O after = mapping.apply(before);
			NodeCustom<O> r = NodeCustom.of(after);
    		return r;
    	});    	
    }
}
