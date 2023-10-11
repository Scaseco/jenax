package org.aksw.jenax.io.json.accumulator;

interface AccJsonEdge
    extends AccJson
{
    String getMatchFieldId();
    String getJsonKey();
    boolean isForward();


    void setSingle(boolean onOrOff);
    boolean isSingle();
    void setTargetAcc(AccJsonNode targetAcc);
}
