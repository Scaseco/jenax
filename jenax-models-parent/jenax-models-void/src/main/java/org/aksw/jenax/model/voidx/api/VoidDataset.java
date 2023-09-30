package org.aksw.jenax.model.voidx.api;

import java.util.Map;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;

@ResourceView
public interface VoidDataset
    extends HasPropertyPartition
{
//    @Iri(VoidTerms.classPartition)
//    Set<VoidClassPartition> getClassPartitions();

    @Iri(VoidTerms.classPartition)
    @KeyIri(VoidTerms._class)
    Map<Node, VoidClassPartition> getClassPartitionMap();
}
