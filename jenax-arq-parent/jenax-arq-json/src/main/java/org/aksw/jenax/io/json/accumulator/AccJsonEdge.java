package org.aksw.jenax.io.json.accumulator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

public interface AccJsonEdge
    extends AccJson
{
    Node getMatchFieldId();
    P_Path0 getJsonKey();
    boolean isForward();


    // void setSingle(boolean onOrOff);
    // boolean isSingle();
    // void setTargetAcc(AccJsonNode targetAcc);
}
