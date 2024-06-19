package org.aksw.jenax.io.json.accumulator;

import org.apache.jena.graph.Node;

public interface AggJsonEdge
    extends AggJson
{
    /** An edge-based aggregator must declare which edge id it matches */
    Node getMatchFieldId();

    AggJsonEdge setTargetAgg(AggJsonNode targetAgg);
    // AggJsonEdge setSingle(boolean value);

    @Override
    AccJsonEdge newAccumulator();
}
