package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.io.rdf.json.RdfElement;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

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
        value = RdfElement.newLiteral(node);
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
            RdfElement effectiveValue = value == null ? RdfElement.nullValue() : value;

            if (context.isSerialize()) {
                RdfObjectNotationWriter writer = context.getJsonWriter();
                if (value == null) {
                    writer.nullValue();
                } else {
                    writer.value(value.getAsLiteral().getInternalId());
                }
            }

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
    public void acceptContribution(RdfElement value, AccContext context) {
        throw new UnsupportedOperationException("Literals cannot expect json elemnts as contributions");
    }
}
