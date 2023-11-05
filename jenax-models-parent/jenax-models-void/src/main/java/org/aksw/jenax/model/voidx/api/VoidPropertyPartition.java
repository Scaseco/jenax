package org.aksw.jenax.model.voidx.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;

@ResourceView
public interface VoidPropertyPartition
    extends VoidEntity
{
    @Iri(VoidTerms._class)
    Node getVoidProperty();
}
