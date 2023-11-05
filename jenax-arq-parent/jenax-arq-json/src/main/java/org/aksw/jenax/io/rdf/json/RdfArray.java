package org.aksw.jenax.io.rdf.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RdfArray
    // extends AbstractList<RdfElement>
    implements RdfElement, Iterable<RdfElement>
{
    // Maybe elements should be restricted to RdfObject
    protected List<RdfElement> elements;

    public RdfArray() {
        this(new ArrayList<>());
    }

    protected RdfArray(List<RdfElement> elements) {
        super();
        this.elements = elements;
    }

    @Override
    public <T> T accept(RdfElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public RdfArray add(RdfElement element) {
        elements.add(element);
        return this;
    }

    public int size() {
        return elements.size();
    }

    // @Override
    public RdfArray addAll(RdfArray other) {
        elements.addAll(other.elements);
        return this;
    }

    // @Override
    public RdfArray addAll(Collection<? extends RdfElement> c) {
        elements.addAll(c);
        return this;
    }

    // @Override
    public RdfElement get(int index) {
        return elements.get(index);
    }

    @Override
    public Iterator<RdfElement> iterator() {
        return elements.iterator();
    }
}
