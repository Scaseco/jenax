package org.aksw.jenax.graphql.sparql.v2.gon.model;

/**
 * A literal simply wraps a node. Unlike RdfObject, it cannot have additional properties.
 * It thus represents a leaf in a tree structure.
 */
public class GonLiteralImpl<K, V>
    extends GonElementBase<K, V>
    implements GonLiteral<K, V>
{
    // XXX final?
    protected V value;

    public GonLiteralImpl(V value) {
        super();
        this.value = value;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public <T> T accept(GonElementVisitor<K, V, T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
