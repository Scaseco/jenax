package org.aksw.jenax.io.json.accumulator;

import org.apache.jena.graph.Node;

interface AccJsonEdge
    extends AccJson
{
    Node getMatchFieldId();
    String getJsonKey();
    boolean isForward();


    void setSingle(boolean onOrOff);
    boolean isSingle();
    void setTargetAcc(AccJsonNode targetAcc);
}
