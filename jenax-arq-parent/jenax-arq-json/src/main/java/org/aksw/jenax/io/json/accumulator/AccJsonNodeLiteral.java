package org.aksw.jenax.io.json.accumulator;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.arq.json.RdfJsonUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.gson.JsonElement;

class AccJsonNodeLiteral
    extends AccJsonBase
    implements AccJsonNode
{
    @Override
    public PathJson getPath() {
        return (parent != null ? parent.getPath() : PathJson.newRelativePath()).resolve(Step.of("literal"));
    }

    @Override
    public void begin(Node node, AccContext context, boolean skipOutput) throws Exception {
        super.begin(node, context, skipOutput);

        // Always materialize literals
        value = RdfJsonUtils.toJson(Graph.emptyGraph, node, 0, 1, false);

        if (!skipOutput && context.isSerialize()) {
            context.getGson().toJson(value, context.getJsonWriter());
        }
    }

    @Override
    public AccJson transition(Triple edge, AccContext context) {
        ensureBegun();
        // Literals reject all edges (indicated by null)
        return null;
    }

    @Override
    public void end(AccContext context) throws Exception {
        ensureBegun();
        if (!skipOutput && context.isMaterialize() && parent != null) {
            parent.acceptContribution(value, context);
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
