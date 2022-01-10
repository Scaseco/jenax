package org.aksw.jena_sparql_api.utils.views.map;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

public interface RdfEntryDelegate<K extends RDFNode, V extends RDFNode>
    extends RdfEntry<K, V>
{
    RdfEntry<?, ?> getDelegate();

    @Override
    default Property getOwnerProperty() {
        return getDelegate().getOwnerProperty();
    }


//    @Override
//    default K getKey() {
//        return (K)getDelegate().getKey();
//    }
//
//    @Override
//    default V getValue() {
//        return (V)getDelegate().getValue();
//    }
//
//    @Override
//    default V setValue(V value) {
//        // return getDelegate().setValue(value);
//    }
}
