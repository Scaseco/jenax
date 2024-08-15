package org.aksw.jenax.io.json.accumulator;

import org.apache.jena.graph.Node;

public class AggJsonProperty
    implements AggJsonEdge
{
    /** The property (json key) being aggregated */
    protected Node jsonKey;

    protected Node matchFieldId; // AccJsonObject indexes AccJsonEdge instances by this attribute
    protected boolean isForward;
    protected boolean isSingle = false;

    /** The aggregator for the value */
    protected AggJsonNode targetAggregator;

    protected AggJsonProperty(Node jsonKey, Node matchFieldId, boolean isForward, AggJsonNode targetAggregator) {
        super();
        this.jsonKey = jsonKey;
        this.matchFieldId = matchFieldId;
        this.isForward = isForward;
        this.targetAggregator = targetAggregator;
    }

    public static AggJsonProperty of(Node jsonKey, Node matchFieldId, boolean isForward) {
        return of(jsonKey, matchFieldId, isForward, null);
    }

    public static AggJsonProperty of(Node jsonKey, Node matchFieldId, boolean isForward, AggJsonNode targetAggregator) {
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

    public Node getJsonKey() {
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
