package org.aksw.jenax.sparql.relation.dataset;

import java.util.Set;

import org.apache.jena.query.Dataset;

public interface NodesInDataset
{
    Dataset getDataset();
    Set<GraphNameAndNode> getGraphNameAndNodes();
}
