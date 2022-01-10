package org.aksw.jena_sparql_api.utils.views.map;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class RdfEntryK
    extends RdfEntryOwnerBase<RDFNode, Resource>
{
    // Note: These properties could be read from a map resource
    protected Property keyProperty;

    public RdfEntryK(Node n, EnhGraph g, Property ownerProperty, Property keyProperty) {
        super(n, g, ownerProperty, keyProperty);
    }

    @Override
    public RDFNode getKey() {
        RDFNode result = ResourceUtils.getPropertyValue(this, keyProperty);
        return result;
    }

    @Override
    public Resource getValue() {
        return this;
    }

    @Override
    public Resource setValue(Resource value) {
        throw new UnsupportedOperationException("Cannot replace value in this entry-model because the value also acts as the entry");
    }

    @Override
    public RdfEntryK inModel(Model m) {
        return m == this.getModel() ? this : new RdfEntryK(node, (EnhGraph)m, ownerProperty, keyProperty);
    }

//    public void clear() {
//        this.removeAll(valueProperty);
//        this.removeAll(keyProperty);
//    }
}
