package org.aksw.jenax.io.json.accumulator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

public class AggJsonFragmentHead
    implements AggJsonEdge
{
    /** The property (json key) being aggregated */
    protected P_Path0 jsonKey;

    protected Node matchFieldId; // AccJsonObject indexes AccJsonEdge instances by this attribute
    protected boolean isForward;

    /** The aggregator for the value */
    protected AggJsonNode targetAggregator;


    protected AggJsonFragmentHead(P_Path0 jsonKey, Node matchFieldId, boolean isForward, AggJsonNode targetAggregator) {
        super();
        this.jsonKey = jsonKey;
        this.matchFieldId = matchFieldId;
        this.isForward = isForward;
        this.targetAggregator = targetAggregator;
    }

    public static AggJsonFragmentHead of(P_Path0 jsonKey, Node matchFieldId, boolean isForward) {
        return of(jsonKey, matchFieldId, isForward, null);
    }

    public static AggJsonFragmentHead of(P_Path0 jsonKey, Node matchFieldId, boolean isForward, AggJsonNode targetAggregator) {
        return new AggJsonFragmentHead(jsonKey, matchFieldId, isForward, targetAggregator);
    }

    @Override
    public AccJsonEdge newAccumulator() {
        AccJsonNode valueAcc = targetAggregator.newAccumulator();
        AccJsonFragmentHead result = new AccJsonFragmentHead(jsonKey, matchFieldId, isForward, valueAcc);
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
    public AggJsonFragmentHead setTargetAgg(AggJsonNode targetAggregator) {
        this.targetAggregator = targetAggregator;
        return this;
    }
}
