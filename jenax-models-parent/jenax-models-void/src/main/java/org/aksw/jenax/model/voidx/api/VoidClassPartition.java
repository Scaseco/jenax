package org.aksw.jenax.model.voidx.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;

@ResourceView
public interface VoidClassPartition
    extends HasPropertyPartition
{
    @Iri(VoidTerms._class)
    Node getVoidClass();
}
