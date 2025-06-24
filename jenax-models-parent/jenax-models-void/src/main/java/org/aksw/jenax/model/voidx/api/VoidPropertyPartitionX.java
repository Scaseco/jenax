package org.aksw.jenax.model.voidx.api;

import java.util.Map;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;

@ResourceView
public interface VoidPropertyPartitionX
    extends VoidPropertyPartition
{
    @Iri(VoidTerms.NS + "objectTypes")
    @KeyIri(VoidTerms._class)
    Map<Node, VoidClassPartition> getClassPartitionMap();
    // objectTypes -> classPartitionMap

    // NOTE: the sportal queries do not include literal datatypes!
}
