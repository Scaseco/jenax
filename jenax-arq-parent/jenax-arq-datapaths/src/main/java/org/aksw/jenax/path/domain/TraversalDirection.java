package org.aksw.jenax.path.domain;

import org.apache.jena.graph.Node;

public interface TraversalDirection<TA, TP>
{
    TA fwd();
    TP bwd();

    TP fwd(Node property);
    TP bwd(Node property);
}
