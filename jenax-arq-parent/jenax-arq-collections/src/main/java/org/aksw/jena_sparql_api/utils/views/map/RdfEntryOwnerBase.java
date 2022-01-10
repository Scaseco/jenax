package org.aksw.jena_sparql_api.utils.views.map;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

public abstract class RdfEntryOwnerBase<K extends RDFNode, V extends RDFNode>
    extends RdfEntryBase<K, V>
{
    /** The property which links from this entry to the owning map */
    protected Property ownerProperty;
    protected Property keyProperty;

    public RdfEntryOwnerBase(Node n, EnhGraph g, Property ownerProperty, Property keyProperty) {
        super(n, g);
        this.ownerProperty = ownerProperty;
        this.keyProperty = keyProperty;
    }

    @Override
    public Property getOwnerProperty() {
        return ownerProperty;
    }

    public Property getKeyProperty() {
        return keyProperty;
    }
}
