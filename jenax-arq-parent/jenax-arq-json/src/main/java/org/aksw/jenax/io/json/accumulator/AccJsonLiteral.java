package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.arq.json.RdfJsonUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class AccJsonLiteral
    extends AccJsonBase
    implements AccJsonNode
{
    @Override
    public PathJson getPath() {
        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of("literal"));
    }

    @Override
    public void begin(Node node, AccContext context, boolean skipOutput) throws IOException {
        super.begin(node, context, skipOutput);

        // Always materialize literals
        // Only emit them when calling end()
        value = RdfJsonUtils.toJson(Graph.emptyGraph, node, 0, 1, false);
    }

    @Override
    public AccJson transition(Triple edge, AccContext context) {
        ensureBegun();
        // Literals reject all edges (indicated by null)
        return null;
    }

    @Override
    public void end(AccContext context) throws IOException {
        ensureBegun();
        if (!skipOutput) {
            JsonElement effectiveValue = value == null ? JsonNull.INSTANCE : value;

            if (context.isSerialize()) {
                context.getGson().toJson(effectiveValue, context.getJsonWriter());
            }

//            if (context.isSerialize() && value == null) {
//                context.getJsonWriter().nullValue();
//            }

            if (context.isMaterialize() && parent != null) {
                parent.acceptContribution(effectiveValue, context);
            }
        }
        super.end(context);
    }

    @Override
    public String toString() {
        return "AccJsonNodeLiteral(source: " + currentSourceNode + ")";
    }

    @Override
    public void acceptContribution(JsonElement value, AccContext context) {
        throw new UnsupportedOperationException("Literals cannot expect json elemnts as contributions");
    }
}
