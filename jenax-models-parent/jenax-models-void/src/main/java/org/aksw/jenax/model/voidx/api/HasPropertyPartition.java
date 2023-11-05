package org.aksw.jenax.model.voidx.api;

import java.util.Map;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.apache.jena.graph.Node;

public interface HasPropertyPartition
    extends VoidEntity
{
    @Iri(VoidTerms.propertyPartition)
    Set<VoidPropertyPartition> getPropertyPartitions();

    @Iri(VoidTerms.propertyPartition)
    @KeyIri(VoidTerms.property)
    Map<Node, VoidPropertyPartition> getPropertyPartitionMap();
}
