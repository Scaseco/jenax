package org.aksw.jenax.graphql.sparql.v2.gon.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An RDF counterpart to a JSON object.
 * Member properties can have a forward or backward direction which
 * allows for deriving an RDF graph.
 */
public class GonObjectImpl<K, V>
    extends GonElementBase<K, V>
    implements GonObject<K, V>
{
    protected Map<K, GonElement<K, V>> members = new LinkedHashMap<>();

    public GonObjectImpl() {
        this(new LinkedHashMap<>());
    }

    protected GonObjectImpl(HashMap<K, GonElement<K, V>> members) {
        super();
        // this.members = members;
        members.forEach(this::add);
    }

    @Override
    public GonObject<K, V> add(K key, GonElement<K, V> value) {
        value.unlinkFromParent();
        ((GonElementBase<K, V>)value).setParent(new ParentLinkObjectImpl<>(this, key));
        members.put(key, value);
        return this;
    }

    @Override
    public GonObject<K, V> remove(Object key) {
        GonElement<K, V> elt = members.get(key);
        if (elt != null) {
            ((GonElementBase<K, V>)elt).setParent(null);
            members.remove(key);
        }
        return this;
    }

    @Override
    public Map<K, GonElement<K, V>> getMembers() {
        return members;
    }

    @Override
    public <T> T accept(GonElementVisitor<K, V, T> visitor) {
        return visitor.visit(this);
    }
}

