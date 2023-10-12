package org.aksw.jenax.io.json.accumulator;

import org.apache.jena.graph.Node;

public class AggJsonProperty
    implements AggJsonEdge
{
    /** The property (json key) being aggregated */
    protected String jsonKey;

    protected Node matchFieldId; // AccJsonObject should index AccJsonEdge by this attribute
    protected boolean isForward;
    protected boolean isSingle = false;

    /** The aggregator for the value */
    protected AggJsonNode targetAggregator;

    protected AggJsonProperty(String jsonKey, Node matchFieldId, boolean isForward, AggJsonNode targetAggregator) {
        super();
        this.jsonKey = jsonKey;
        this.matchFieldId = matchFieldId;
        this.isForward = isForward;
        this.targetAggregator = targetAggregator;
    }

    public static AggJsonProperty of(String jsonKey, Node matchFieldId, boolean isForward) {
        return of(jsonKey, matchFieldId, isForward, null);
    }

    public static AggJsonProperty of(String jsonKey, Node matchFieldId, boolean isForward, AggJsonNode targetAggregator) {
        return new AggJsonProperty(jsonKey, matchFieldId, isForward, targetAggregator);
    }

    @Override
    public AccJsonEdge newAccumulator() {
        AccJsonNode valueAcc = targetAggregator.newAccumulator();
        AccJsonProperty result = new AccJsonProperty(jsonKey, matchFieldId, isForward, valueAcc);
        valueAcc.setParent(result);
        result.setSingle(isSingle);
        return result;
    }

    @Override
    public Node getMatchFieldId() {
        return matchFieldId;
    }

    public String getJsonKey() {
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

    @Override
    public AggJsonProperty setSingle(boolean value) {
        this.isSingle = value;
        return this;
    }
}
