package org.aksw.jenax.io.json.accumulator;

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

class AccNodeDriver {
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

    public void accumulate(Quad input, AccContext cxt) throws Exception {
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

    public void begin(AccContext cxt) throws Exception {

    }

    public void end(AccContext cxt) throws Exception {
        endCurrentItem(cxt);
        this.currentSource = null;
    }

    public JsonElement getValue() {
        return currentState.getValue();
    }

    /** Recursively calls end() on the current accumulator and all its ancestors */
    protected void endCurrentItem(AccContext cxt) throws Exception {
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

        // AccContext cxt = null; // enable materialize

        AccNodeDriver driver = this;
        CollapseRunsSpec<Quad, Node, AccNodeDriver> spec = CollapseRunsSpec.create(
                Quad::getGraph,
                (accNum, collapseKey) -> {
//                    try {
//                        driver.begin(cxt);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                	if (accNum != 0) { try {
//                    // driver.end(cxt);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } }
                return driver;
                },
                (acc, quad) -> {
                    try {
                        acc.accumulate(quad, cxt);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Stream<Entry<Node, JsonElement>> result = StreamOperatorCollapseRuns.create(spec)
            .transform(quadStream)
            .map(e -> {
                AccNodeDriver tmp = e.getValue();
                try {
                    tmp.end(cxt);
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
                return Map.entry(e.getKey(), tmp.getValue());
            });

        return result;
    }
}
