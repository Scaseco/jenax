package org.aksw.jenax.io.json.accumulator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;

public class AggJsonProperty
    implements AggJsonEdge
{
    /** The property (json key) being aggregated */
    protected P_Path0 jsonKey;

    protected Node matchFieldId; // AccJsonObject indexes AccJsonEdge instances by this attribute
    protected boolean isForward;
    protected boolean isSingle = false;

    /** The aggregator for the value */
    protected AggJsonNode targetAggregator;

    protected AggJsonProperty(P_Path0 jsonKey, Node matchFieldId, boolean isForward, AggJsonNode targetAggregator) {
        super();
        this.jsonKey = jsonKey;
        this.matchFieldId = matchFieldId;
        this.isForward = isForward;
        this.targetAggregator = targetAggregator;
    }

    public static AggJsonProperty of(Node jsonKey, Node matchFieldId, boolean isForward) {
        return of(new P_Link(jsonKey), matchFieldId, isForward, null);
    }

    public static AggJsonProperty of(Node jsonKey, Node matchFieldId, boolean isForward, AggJsonNode targetAggregator) {
        return of(new P_Link(jsonKey), matchFieldId, isForward, targetAggregator);
    }

    public static AggJsonProperty of(P_Path0 jsonKey, Node matchFieldId, boolean isForward) {
        return of(jsonKey, matchFieldId, isForward, null);
    }

    public static AggJsonProperty of(P_Path0 jsonKey, Node matchFieldId, boolean isForward, AggJsonNode targetAggregator) {
        return new AggJsonProperty(jsonKey, matchFieldId, isForward, targetAggregator);
    }

    @Override
    public AccJsonEdge newAccumulator() {
        AccJsonNode valueAcc = targetAggregator.newAccumulator();
        AccJsonProperty result = new AccJsonProperty(jsonKey, matchFieldId, isForward, valueAcc, isSingle);
        valueAcc.setParent(result);
        // result.setSingle(isSingle);
        return result;
    }

    @Override
    public Node getMatchFieldId() {
        return matchFieldId;
    }

    public P_Path0 getJsonKey() {
        return jsonKey;
    }

    public boolean isForward() {
        return isForward;
    }

    public AggJson getTargetAggregator() {
        return targetAggregator;
    }

    @Override
    public AggJsonProperty setTargetAgg(AggJsonNode targetAggregator) {
        this.targetAggregator = targetAggregator;
        return this;
    }

    // @Override
    /** Only for testing; should not be used in production code */
    public AggJsonProperty setSingle(boolean value) {
        this.isSingle = value;
        return this;
    }
}
