package org.aksw.jenax.graphql.sparql.v2.gon.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

// In principle RdfArray could extend RdfObject - similar to JavaScript - but probably sticking to
// the JSON model is easier to use.
public class GonArrayImpl<K, V>
    extends GonElementBase<K, V>
    implements GonArray<K, V>
{
    // Maybe elements should be restricted to RdfObject
    protected List<GonElement<K, V>> elements;

    public GonArrayImpl() {
        this(new ArrayList<>());
    }

    protected GonArrayImpl(List<GonElement<K, V>> elements) {
        super();
        this.elements = elements;
    }

    @Override
    public <T> T accept(GonElementVisitor<K, V, T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public GonArray<K, V> add(GonElement<K, V> element) {
        int i = elements.size();
        ((GonElementBase<K, V>)element).setParent(new ParentLinkArrayImpl<>(this, i));
        elements.add(element);
        return this;
    }

    @Override
    public int size() {
        return elements.size();
    }

    // @Override
    public GonArray<K, V> addAll(GonArray<K, V> other) {
        other.forEach(elements::add);
        // elements.addAll(other);
        return this;
    }

    // @Override
    public GonArray<K, V> addAll(Collection<? extends GonElement<K, V>> c) {
        elements.addAll(c);
        return this;
    }

    @Override
    public GonElement<K, V> get(int index) {
        return elements.get(index);
    }

    @Override
    public Iterator<GonElement<K, V>> iterator() {
        return elements.iterator();
    }

    @Override
    public GonArray<K, V> set(int index, GonElement<K, V> element) {
        GonElement<K, V> elt = elements.get(index);
        if (elt != null) {
            GonElementBase<K, V> x = (GonElementBase<K, V>)elt;
            x.setParent(null);
            ((GonElementBase<K, V>)element).setParent(new ParentLinkArrayImpl<>(this, index));
        }
        return this;
    }

    @Override
    public GonArray<K, V> remove(int index) {
        elements.remove(index);
        Iterator<GonElement<K, V>> it = elements.listIterator(index);

        // Decrement parent link index of all elements after the removed one
        while (it.hasNext()) {
            GonElementBase<K, V> elt = (GonElementBase<K, V>)it.next();
            ParentLinkArray<K, V> oldLink = elt.getParent().asArrayLink();
            elt.setParent(new ParentLinkArrayImpl<>(oldLink.getParent(), oldLink.getIndex() - 1));
        }
        return this;
    }
}
