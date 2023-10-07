package org.aksw.jenax.arq.util.node;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


/**
 * A {@link Node} implementation that can hold an arbitrary object as its payload.
 * This class provides static utility methods to find NodeCustom instances in expressions and transform
 * those expressions into new ones.
 *
 * @param <T>
 */
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

    public static Stream<NodeCustom<?>> streamCustomNodes(Expr expr) {
        Stream<NodeCustom<?>> result = ExprUtils.nodesMentioned(expr).stream()
                .flatMap(x -> ObjectUtils.tryCastAs(NodeCustom.class, x).stream())
                .map(x -> (NodeCustom<?>) x);
        return result;
    }

    public static Stream<?> streamMentionedValues(Expr expr) {
        Stream<?> result = streamCustomNodes(expr)
                .map(NodeCustom::getValue);
        return result;
    }

    /**
     * Scans all NodeCustom instances in the given expression and returns
     * all payloads of type 'payloadClass' as instances of type 'T'
     *
     * You need to ensure yourself that 'payloadClass' is compatible with 'T'.
     * The reason is that the payloadClass may be a generic type.
     */
    public static <T> Set<T> mentionedValues(Class<?> payloadClass, Expr expr) {
        @SuppressWarnings("unchecked")
        Set<T> result = streamMentionedValues(expr)
                .flatMap(v -> ObjectUtils.tryCastAs(payloadClass, v).stream())
                .map(x -> (T)x)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return result;
    }

    /** Substitute all referenced paths in an expression w.r.t. the given path mapping */
    public static <T> Expr resolveExpr(Expr expr, Class<?> payloadClass, Function<T, ? extends Node> mapping) {
        NodeTransform nodeTransform = createNodeTransform(payloadClass, mapping);
        Expr result = ExprTransformer.transform(new NodeTransformExpr(nodeTransform), expr);
        return result;
    }

    /** Create a NodeTransform for substituting NodeFacetPath instances with variables */
    @SuppressWarnings("unchecked")
    public static <T> NodeTransform createNodeTransform(Class<?> payloadClass, Function<? super T, ? extends Node> mapping) {
        return NodeTransformLib2.wrapWithNullAsIdentity(node -> {
            Object payload = ObjectUtils.tryCastAs(NodeCustom.class, node)
                .flatMap(custom -> ObjectUtils.tryCastAs(payloadClass, custom.getValue()))
                    .orElse(null);
            Node r = payload != null
                ? mapping.apply((T)payload)
                : null;
            return r;
        });
    }

    /** Create a NodeTransform that transforms the payload of NodeCustom instances */
    public static <I, O> NodeTransform mapValue(Class<?> payloadClass, Function<? super I, O> mapping) {
        return createNodeTransform(payloadClass, (I before) -> {
            O after = mapping.apply(before);
            NodeCustom<O> r = NodeCustom.of(after);
            return r;
        });
    }
}
