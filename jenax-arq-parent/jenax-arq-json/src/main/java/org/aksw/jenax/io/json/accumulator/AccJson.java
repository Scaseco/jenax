package org.aksw.jenax.io.json.accumulator;

import java.io.IOException;

import org.aksw.commons.path.json.PathJson;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.gson.JsonElement;


/**
 * Interface for accumulating a JSON object from a stream of triples (edges in a graph).
 * The accumulator is like a state in a state automaton:
 * An accumulator receives an individual edge in order to decide whether it can transition to a child state.
 * If it can't then it returns null.
 * {@link AccJsonDriver} drives the lookup. If a state (=accumulator) cannot handle the edge, it searches whether
 * any of that state's ancestors accepts it.
 * It is an error if no suitable accumulator is found for an edge - because that means that it is unclear
 * which accumulator should match the subsequent edges.
 */
public interface AccJson {

    PathJson getPath();

    /**
     * Sets the parent of this accumulator.
     * This method should never be called by application code.
     *
     * @throws {@link IllegalStateException} if a parent has already been set.
     */
    void setParent(AccJson parent);


    AccJson getParent();

    /**
     * Start accumulation based on a given node in the underlying graph.
     * Calls cannot be nested.
     *
     * @throws IllegalStateException if there was a prior call to begin() without corresponding end()
     *
     * @param node The source node to which the next incoming edge(s) will connect to
     * @param context The context which holds the JSON serializers
     * @param skipOutput When output should be disabled (used to skip over lists of items where just one was expected)
     */
    void begin(Node node, AccContext context, boolean skipOutput) throws IOException;

    /**
     * Process an edge.
     * Based on the given edge, this accumulator attempts to transition to another AccJson instance and return it.
     * If there is no valid transition then this method returns null.
     */
    AccJson transition(Triple edge, AccContext cxt) throws IOException;

    /**
     * End the accumulator's current node
     *
     * @throws IllegalStateException if there was no prior call to begin()
     */
    void end(AccContext cxt) throws IOException;

    /** True after begin() and before end()*/
    boolean hasBegun();

    /**
     * If cxt.isMaterialize is enabled then this method returns the json
     * data assembled for the current node.
     * It is only valid to call this method after end().
     */
    JsonElement getValue();


    /**
     * For materialization: Whenever end() is called on a state with materialization enabled, then
     * it passes its accumulated value to the parent using this method.
     */
    void acceptContribution(JsonElement value, AccContext context);
}
