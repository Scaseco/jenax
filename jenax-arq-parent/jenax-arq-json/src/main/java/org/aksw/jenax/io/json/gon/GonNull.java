package org.aksw.jenax.io.json.gon;

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
