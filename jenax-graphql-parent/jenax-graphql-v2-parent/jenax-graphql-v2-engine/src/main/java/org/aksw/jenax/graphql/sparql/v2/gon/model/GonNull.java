package org.aksw.jenax.graphql.sparql.v2.gon.model;

public class GonNull<K, V>
    extends GonElementBase<K, V>
{
    public GonNull() {
        super();
    }
    @Override
    public <T> T accept(GonElementVisitor<K, V, T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
