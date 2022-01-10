package org.aksw.jena_sparql_api.utils.views.map;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.rdf.model.RDFNode;

public abstract class RdfEntryDelegateBase<K extends RDFNode, V extends RDFNode>
    extends RdfEntryBase<K, V>
    implements RdfEntryDelegate<K, V>
{
    protected RdfEntry<?, ?> delegate;

    public RdfEntryDelegateBase(RdfEntry<?, ?> delegate) {
        super(delegate.asNode(), (EnhGraph)delegate.getModel());
        this.delegate = delegate;
    }

    @Override
    public RdfEntry<?, ?> getDelegate() {
        return delegate;
    }
}
