package org.aksw.jenax.graphql.sparql.v2.gon.model;

import java.util.Map;

public interface GonObject<K, V>
    extends GonElement<K, V>
{
    /** Get the members of this object. */
    // XXX We may want to add support for dedicated forward / backward views
    Map<K, GonElement<K, V>> getMembers();

    GonObject<K, V> add(K key, GonElement<K, V> value);

    default GonElement<K, V> get(K name) {
        return get(name);
    }

    default GonObject<K, V> getObject(K name) {
        GonElement<K, V> elt = get(name);
        GonObject<K, V> result = elt == null ? null : elt.getAsObject();
        return result;
    }

    GonObject<K, V> remove(Object name);



//
//    default RdfObject getOrCreateContext() {
//        String name = "@context";
//        RdfObject result;
//        RdfElement elt = get(name);
//        if (elt == null) {
//            result = new RdfObjectImpl();
//            P_Path0 key = new P_Link(NodeFactory.createLiteralString(name));
//            add(key, result);
//        } else {
//            result = elt.getAsObject();
//        }
//        return result;
//    }


//    static RdfObject extractRdfObject(Model model) {
//    	Graph g = model.getGraph();
//    	if (g instanceof GraphOverRdfObject)
//    }

    /** Return a mutable sub view of all forward keys that are strings and that can thus be viewed as json objects. */
    // getJsonSubView()
}
