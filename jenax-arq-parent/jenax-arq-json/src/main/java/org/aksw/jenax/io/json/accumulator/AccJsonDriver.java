package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.util.stream.CollapseRunsSpec;
import org.aksw.commons.util.stream.StreamOperatorCollapseRuns;
import org.aksw.jenax.io.rdf.json.RdfElement;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

import com.google.common.base.Preconditions;

/**
 * This class implements the driver for accumulating (json) objects from a sequence of edges (triples/quads).
 * The AccJson objects can be seen as states in a state automaton, and this class drives
 * the transition between the states based on the input.
 */
public class AccJsonDriver
// its like an accumulator but depending on the context the final value may be absent (null) if it was in streaming mode
// 	implements Accumulator<Quad, AccContext, >
{
    protected AccJson currentState;
    protected Node currentSource;

    protected long sourcesSeen = 0;
    protected boolean isSingle;

    protected AccJsonDriver(AccJson rootAcc, boolean isSingle) {
        super();
        Preconditions.checkArgument(rootAcc.getParent() == null, "Root accumulator must not have a parent");
        this.currentState = rootAcc;
        this.isSingle = isSingle;
    }

    public static AccJsonDriver of(AccJson rootAcc, boolean isSingle) {
        return new AccJsonDriver(rootAcc, isSingle);
    }

    public long getSourcesSeen() {
        return sourcesSeen;
    }

    /**
     * We expect each root node to be announced with a dummy quad that does not carry any
     * edge information (s, s, ANY, ANY)
     *
     * @param input
     * @param cxt
     * @throws IOException
     */
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

        boolean isNewSource = false;
        if (currentSource == null) {
            // flush the writer whenever we encounter a new item
            if (false) {
                if (cxt.isSerialize()) {
                    cxt.getJsonWriter().flush();
                }
            }

            isNewSource = true;
            ++sourcesSeen;

            if (isSingle && sourcesSeen > 1) {
                throw new RuntimeException("Too many results. Maybe use @one(self: false)?");
            }

            currentSource = source;
            // XXX Should we filter out the 'root quad' that announces the existence of a node?
            currentState.begin(currentSource, cxt, false);
            isNewSource = true;
        }
        AccJson nextState;

        // Effectively skip the first quad that introduces a new source
        if (!isNewSource) {
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
    }

    public void begin(AccContext cxt) throws IOException {

    }

    public void end(AccContext cxt) throws IOException {
        endCurrentItem(cxt);
        this.currentSource = null;
    }

    public RdfElement getValue() {
        return currentState.getValue();
    }

    /** Recursively calls end() on the current accumulator and all its ancestors */
    protected void endCurrentItem(AccContext cxt) throws IOException {
        while (true) {
            // If no item was seen then begin was not called on the currentState
            // XXX Can we design the process in a way such that we don't have to check for hasBegun here?
            if (currentState.hasBegun()) {
                currentState.end(cxt);
            }

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
    public Stream<Entry<Node, RdfElement>> asStream(AccContext cxt, Stream<Quad> quadStream) {
        Preconditions.checkArgument(!quadStream.isParallel(), "Json aggregation requires sequential stream");

        AccJsonDriver driver = this;
        CollapseRunsSpec<Quad, Node, AccJsonDriver> spec = CollapseRunsSpec.create(
                Quad::getGraph,
                (accNum, collapseKey) -> driver,
                (acc, quad) -> {
                    try {
                        acc.accumulate(quad, cxt);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Stream<Entry<Node, RdfElement>> result = StreamOperatorCollapseRuns.create(spec)
            .transform(quadStream)
            .map(entry -> {
                AccJsonDriver tmp = entry.getValue();
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
