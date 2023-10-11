package org.aksw.jenax.io.json.accumulator;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.arq.json.RdfJsonUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

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
    public void end(AccContext context) throws Exception {
        super.end(context);
    }

    @Override
    public AccJson transition(Triple edge, AccContext context) {
        // Literals reject all edges (indicated by null)
        return null;
    }

    @Override
    public String toString() {
        return "AccJsonNodeLiteral(source: " + currentSourceNode + ")";
    }
}
