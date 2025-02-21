package org.aksw.jenax.facete.treequery2.impl;

import java.util.List;

import org.aksw.commons.io.slice.Slice;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Table;

public class ResultNode2 {
    private ResultNode2 parent;
    private RDFDataSource dataSource;
    private NodeQueryOld queryNode;

    // Maybe the cache is not part of the result set but part of the execution layer?
    private Table<Node, FacetStep, Slice<ResultNode2>> children;
    private Binding partitionBinding;
    private List<Node> nodes;
    // private

    // protected ClaimingCache<org.aksw.commons.path.core.Path<Node>, AdvancedRangeCacheImpl<Node[]>> cache;
    // ClaimingCache<org.aksw.commons.path.core.Path<Node>, AdvancedRangeCacheImpl<Node[]>> cache;
}
