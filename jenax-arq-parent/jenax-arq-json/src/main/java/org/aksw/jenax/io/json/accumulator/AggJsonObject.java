package org.aksw.jenax.io.json.accumulator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.graph.Node;

public class AggJsonObject
    implements AggJsonNode
{
    protected LinkedHashMap<Node, AggJsonEdge> propertyAggregators = new LinkedHashMap<>();

    public static AggJsonObject of(AggJsonEdge ...edgeAggregators) {
        AggJsonObject result = new AggJsonObject();
        for (AggJsonEdge agg : edgeAggregators) {
            result.addPropertyAggregator(agg);
        }
        return result;
    }

    public LinkedHashMap<Node, AggJsonEdge> getPropertyAggregators() {
        return propertyAggregators;
    }

    public void addPropertyAggregator(AggJsonEdge propertyAggregator) {
        // XXX Validate that no json keys are unique
        Node matchFieldId = propertyAggregator.getMatchFieldId();
        propertyAggregators.put(matchFieldId, propertyAggregator);
    }

    @Override
    public AccJsonNode newAccumulator() {
        int n = propertyAggregators.size();

        Map<Node, Integer> fieldIdToIndex = new HashMap<>();
        AccJsonEdge[] edgeAccs = new AccJsonEdge[n];

        int fieldIndex = 0;
        for (Entry<Node, AggJsonEdge> e : propertyAggregators.entrySet()) {
            Node fieldId = e.getKey();
            AggJsonEdge agg = e.getValue();

            AccJsonEdge acc = agg.newAccumulator();

            fieldIdToIndex.put(fieldId, fieldIndex);
            edgeAccs[fieldIndex] = acc;
            ++fieldIndex;
        }

        AccJsonObject result = AccJsonObject.of(fieldIdToIndex, edgeAccs);
        return result;
    }
}
