package org.aksw.jenax.io.json.accumulator;

import org.apache.jena.graph.Node;

public interface AccJsonEdge
    extends AccJson
{
    Node getMatchFieldId();
    Node getJsonKey();
    boolean isForward();


    void setSingle(boolean onOrOff);
    boolean isSingle();
    void setTargetAcc(AccJsonNode targetAcc);
}
