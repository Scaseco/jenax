package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.util.stream.CollapseRunsSpec;
import org.aksw.commons.util.stream.StreamOperatorCollapseRuns;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;

public class AccNodeDriver {
    protected AccJson currentState;
    protected Node currentSource;

    protected AccNodeDriver(AccJson rootAcc) {
        super();
        Preconditions.checkArgument(rootAcc.getParent() == null, "Root accumulator must not have a parent");
        this.currentState = rootAcc;
    }

    public static AccNodeDriver of(AccJson rootAcc) {
        return new AccNodeDriver(rootAcc);
    }

    public void accumulate(Quad input, AccContext cxt) throws IOException {
        Node source = input.getGraph();
        Triple triple = input.asTriple();

        // If currentSource is set it implies we invoked beginNode()
        if (currentSource != null) {
            // If the input's source differs from the current one
            // then invoke end() on the accumulators up to the root
            if (!source.equals(currentSource)) {
                endCurrentItem(cxt);
                currentSource = null;
            }
        }

        if (currentSource == null) {
            currentSource = source;
            // XXX Should we filter out the 'root quad' that announces the existence of a node?
            currentState.begin(currentSource, cxt, false);
        }
        AccJson nextState;

        // Find a state that accepts the transition
        while (true) {
            nextState = currentState.transition(triple, cxt);
            if (nextState == null) {
                currentState.end(cxt);
                AccJson parentAcc = currentState.getParent();
                if (parentAcc != null) {
                    currentState = parentAcc;
                } else {
                    throw new RuntimeException("No acceptable transition for " + triple);
                }
            } else {
                currentState = nextState;
                break;
            }
        }
    }

    public void begin(AccContext cxt) throws IOException {

    }

    public void end(AccContext cxt) throws IOException {
        endCurrentItem(cxt);
        this.currentSource = null;
    }

    public JsonElement getValue() {
        return currentState.getValue();
    }

    /** Recursively calls end() on the current accumulator and all its ancestors */
    protected void endCurrentItem(AccContext cxt) throws IOException {
        while (true) {
            currentState.end(cxt);
            AccJson parent = currentState.getParent();
            if (parent != null) {
                currentState = parent;
            } else {
                break;
            }
        }
    }


    // This method needs to go to the aggregator because it needs to create an accumulator specifically
    // for the stream
    public Stream<Entry<Node, JsonElement>> asStream(AccContext cxt, Stream<Quad> quadStream) {
        Preconditions.checkArgument(!quadStream.isParallel(), "Json aggregation requires sequential stream");

        AccNodeDriver driver = this;
        CollapseRunsSpec<Quad, Node, AccNodeDriver> spec = CollapseRunsSpec.create(
                Quad::getGraph,
                (accNum, collapseKey) -> driver,
                (acc, quad) -> {
                    try {
                        acc.accumulate(quad, cxt);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Stream<Entry<Node, JsonElement>> result = StreamOperatorCollapseRuns.create(spec)
            .transform(quadStream)
            .map(entry -> {
                AccNodeDriver tmp = entry.getValue();
                try {
                    tmp.end(cxt);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return Map.entry(entry.getKey(), tmp.getValue());
            });

        return result;
    }
}
