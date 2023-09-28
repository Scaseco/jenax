package org.aksw.jenax.facete.treequery2.impl;

import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.apache.jena.sparql.engine.binding.Binding;

/** */
public interface ResultNode {

    /** The values for the partition variables */
    Binding getPartitionBinding();

    /** Get the query node which provides the schema (the set of available properties) for this result node */
    NodeQueryOld getQueryNode();


    // Map<FacetStep, >
    // TODO We some streaming?! way to iterate the result bindings
    // No, we don't need streaming here - but we need a way to 'cache' result sets under configurations
    //
}
