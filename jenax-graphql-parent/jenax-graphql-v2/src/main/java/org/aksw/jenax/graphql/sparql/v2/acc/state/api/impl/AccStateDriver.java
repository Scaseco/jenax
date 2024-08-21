package org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiFunction;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccContext;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccStateGon;

/**
 * The driver is the class that has a reference to the accumulator that will receive the next input.
 *
 * This class implements the driver for accumulating tree structures or objects from a sequence of tuples.
 * The AccJson objects can be seen as states in a state automaton, and this class drives
 * the transition between the states based on the input.
 *
 * FIXME Design Issues:
 *   - Is the tuple type needed on class level? probably on method level is sufficient if we use accessors.
 *   - Should we wrap individual components as tuples? The alternative is to use Object - but without passing
 *     Class objects we can't decide which is which...
 *     UPDATE We can decide if we use tupleBridges with dimension == 1
 *   - Access to tuple components: Probably this could be separated using a tuple accessor.
 *
 * @param <D> tuple type
 * @param <C> component type
 */
public class AccStateDriver<I, E, K, V>
// its like an accumulator but depending on the context the final value may be absent (null) if it was in streaming mode
// 	implements Accumulator<Quad, AccContext, >
{
    protected AccContext<K, V> context;
    protected AccStateGon<I, E, K, V> currentState;
    protected Object currentSource;

    protected long sourcesSeen = 0;
    protected boolean isSingle;

    // protected List<Expr> idExprs;
    protected BiFunction<I, E, ?> inputToStateId;
    // protected VarExprList idExprs;
    // protected TupleBridge<D, C> primaryIdAccessor;

//    protected AccStateDriver(AccStateStructure<I, E, K, V> rootAcc, boolean isSingle) {
//        super();
//        Preconditions.checkArgument(rootAcc.getParent() == null, "Root accumulator must not have a parent");
//        this.currentState = rootAcc;
//        this.isSingle = isSingle;
//    }

    protected AccStateDriver(AccContext<K, V> context, AccStateGon<I, E, K, V> rootAcc, boolean isSingle, BiFunction<I, E, ?> inputToStateId) {
        super();
        if (rootAcc.getParent() != null) {
            throw new IllegalArgumentException("Root accumulator must not have a parent");
        }
        this.currentState = rootAcc;
        this.isSingle = isSingle;
        this.inputToStateId = inputToStateId;
        setContext(context);
    }

    public static <I, E, K, V> AccStateDriver<I, E, K, V> of(AccContext<K, V> context, AccStateGon<I, E, K, V> rootAcc, boolean isSingle, BiFunction<I, E, ?> inputToStateId) { // , TupleBridge<D, C> primaryIdAccessor) {
        // return new AccStateDriver<>(rootAcc, isSingle); //, primaryIdAccessor);
        return new AccStateDriver<>(context, rootAcc, isSingle, inputToStateId);
    }

    public BiFunction<I, E, ?> getInputToStateId() {
        return inputToStateId;
    }

    public AccContext<K, V> getContext() {
        return context;
    }

    public long getSourcesSeen() {
        return sourcesSeen;
    }

    public void setContext(AccContext<K, V> context) {
        this.context = context;
        currentState.setContext(context);
    }


    boolean hasPendingInput = false;
    protected Object pendingInputStateId;
    protected I pendingInput;
    protected E pendingEnv;

    protected boolean processPendingInput() throws IOException {
        boolean completedAnObject = false;
        if (hasPendingInput) {
            completedAnObject = processInput(pendingInputStateId, pendingInput, pendingEnv, false);
            if (completedAnObject) {
                return completedAnObject;
            } else {
                // continue
            }
        }
        return completedAnObject;
    }

    /**
     * We expect each root node to be announced with a dummy quad that does not carry any
     * edge information (s, s, ANY, ANY)
     *
     * @param input
     * @param cxt
     * @throws IOException
     */
    public boolean accumulate(I input, E env) throws IOException {

        // C[] source = primaryIdAccessor.toComponentArray(input);
        // Binding source = VarExprListUtils.eval(idExprs, input, env);
        // NodeValue[] source = ExprUtils.evalToArray(idExprs, input, env);
        Object source = "foo"; // inputToStateId.apply(input, env);

        Object inputStateId = inputToStateId.apply(input, env);

        if (processPendingInput()) {
            return true;
        }

        // System.out.println("State: " + inputStateId + " Input: " + input);
        // Node source = input.getGraph();
        // Triple triple = input.asTriple();

        // If currentSource is set it implies we invoked beginNode()
        if (currentSource != null) {
            // If the input's source differs from the current one
            // then invoke end() on the accumulators up to the root
            if (!Objects.equals(source, currentSource)) {
                endCurrentItem();
                currentSource = null;
            }
        }

        boolean isNewSource = false;
        if (currentSource == null) {
            // flush the writer whenever we encounter a new item
            if (false) {
                if (context.isSerialize()) {
                    context.getJsonWriter().flush();
                }
            }

            isNewSource = true;
            ++sourcesSeen;

            if (isSingle && sourcesSeen > 1) {
                throw new RuntimeException("Too many results. Maybe use @one(self: false)?");
            }

            currentSource = source;
            // XXX Should we filter out the 'root quad' that announces the existence of a node?
            currentState.begin(currentSource, input, env, false);
            isNewSource = true;
        }

//        currentStateId = inputStateId;
//        currentInput = input;
//        currentEnv = env;


        hasPendingInput = false;
        boolean completedAnObject = processInput(inputStateId, input, env, isNewSource);
        return completedAnObject;
    }

    public boolean processInput(Object inputStateId, I input, E env, boolean isNewSource) throws IOException {

        boolean completedAnItem = false;
        AccStateGon<I, E, K, V> nextState;

        // Effectively skip the first quad that introduces a new source
        if (true || !isNewSource) {
            // Find a state that accepts the transition
            while (true) {
                nextState = currentState.transition(inputStateId, input, env);
                if (nextState == null) {
                    currentState.end();
                    AccStateGon<I, E, K, V> parentAcc = currentState.getParent();
                    if (parentAcc != null) {
                        currentState = parentAcc;
                    } else {
                        // TODO Provide a trace of the states visited by backtracking and
                        // perhaps what transitions they accepted
                        throw new RuntimeException("No acceptable transition for " + input);
                    }

                    // If we just ended the root state then we have completed the next item
                    if (currentState.getParent() == null) {
                        completedAnItem = true;
                        hasPendingInput = true;
                        pendingInput = input;
                        pendingEnv = env;
                        pendingInputStateId = inputStateId;
                        // TODO We must return / callback before transitioning on the current input which will start the next item
                        // System.out.println("COMPLETED AN ITEM");
                        break;
                    }

                } else {
                    currentState = nextState;
                    break;
                }
            }
        }
        return completedAnItem;
    }

//    public void begin(AccContextRdf cxt) throws IOException {
//
//    }

    // True if another item was emitted
    public boolean end() throws IOException {
        boolean result;
        if (hasPendingInput) {
            result = processInput(pendingInputStateId, pendingInput, pendingEnv, false);
        } else {
            result = currentState.getParent() != null;
        }
        endCurrentItem();
        this.currentSource = null;
        return result;
    }

//    public O getValue() {
//        return currentState.getValue();
//    }

    /** Recursively calls end() on the current accumulator and all its ancestors */
    protected void endCurrentItem() throws IOException {
        while (true) {
            // If no item was seen then begin was not called on the currentState
            // XXX Can we design the process in a way such that we don't have to check for hasBegun here?
            if (currentState.hasBegun()) {
                currentState.end();
            }

            AccStateGon<I, E, K, V> parent = currentState.getParent();
            if (parent != null) {
                currentState = parent;
            } else {
                break;
            }
        }
    }

    // This method needs to go to the aggregator because it needs to create an accumulator specifically
    // for the stream
//    public Stream<Entry<Node, RdfElement>> asStream(AccContext cxt, Stream<Quad> quadStream) {
//        Preconditions.checkArgument(!quadStream.isParallel(), "Json aggregation requires sequential stream");
//
//        AccLateralDriver<D, C, O> driver = this;
//        CollapseRunsSpec<Quad, Node, AccLateralDriver> spec = CollapseRunsSpec.create(
//                Quad::getGraph,
//                (accNum, collapseKey) -> driver,
//                (acc, quad) -> {
//                    try {
//                        acc.accumulate(quad, cxt);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//
//        Stream<Entry<Node, RdfElement>> result = StreamOperatorCollapseRuns.create(spec)
//            .transform(quadStream)
//            .map(entry -> {
//                AccLateralDriver<D, C, O> tmp = entry.getValue();
//                try {
//                    tmp.end(cxt);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//                return Map.entry(entry.getKey(), tmp.getValue());
//            });
//
//        return result;
//    }
}
