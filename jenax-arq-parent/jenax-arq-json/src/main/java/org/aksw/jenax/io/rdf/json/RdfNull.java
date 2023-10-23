package org.aksw.jenax.io.rdf.json;

public class RdfNull
    implements RdfElement
{
    private static final RdfNull INSTANCE = new RdfNull();

    public static RdfNull get() {
        return INSTANCE;
    }

    @Override
    public <T> T accept(RdfElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
