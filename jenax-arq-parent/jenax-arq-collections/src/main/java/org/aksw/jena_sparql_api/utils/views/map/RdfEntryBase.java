package org.aksw.jena_sparql_api.utils.views.map;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public abstract class RdfEntryBase<K extends RDFNode, V extends RDFNode>
    extends ResourceImpl
    implements RdfEntry<K, V>
{
    public RdfEntryBase(Node n, EnhGraph g) {
        super(n, g);
    }

    @Override
    public abstract RdfEntry<K, V> inModel(Model m);

    @Override
    public String toString() {
        return getKey() + "=" + getValue();
    }
}
