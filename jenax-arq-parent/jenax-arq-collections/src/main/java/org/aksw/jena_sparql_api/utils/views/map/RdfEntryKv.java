package org.aksw.jena_sparql_api.utils.views.map;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

public class RdfEntryKv
    extends RdfEntryOwnerBase<RDFNode, RDFNode>
{
    // Note: These properties could be read from a map resource
    protected Property valueProperty;

    public RdfEntryKv(Node n, EnhGraph g, Property ownerProperty, Property keyProperty, Property valueProperty) {
        super(n, g, ownerProperty, keyProperty);
        this.valueProperty = valueProperty;
    }

    @Override
    public RDFNode getKey() {
        RDFNode result = ResourceUtils.getPropertyValue(this, keyProperty);
        return result;
    }

    @Override
    public RDFNode getValue() {
        RDFNode result = ResourceUtils.getPropertyValue(this, valueProperty);
        return result;
    }

    @Override
    public RdfEntryKv inModel(Model m) {
        return m == this.getModel() ? this : new RdfEntryKv(node, (EnhGraph)m, ownerProperty, keyProperty, valueProperty);
    }

    @Override
    public RDFNode setValue(RDFNode value) {
        RDFNode result = getValue();
        ResourceUtils.setProperty(this, valueProperty, value);
        return result;
    }

    public void clear() {
        this.removeAll(valueProperty);
        this.removeAll(keyProperty);
    }
}
