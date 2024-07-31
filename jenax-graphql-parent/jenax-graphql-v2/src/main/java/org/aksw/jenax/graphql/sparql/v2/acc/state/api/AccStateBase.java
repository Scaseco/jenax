package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

import java.io.IOException;

import graphql.com.google.common.base.Preconditions;

/** Base class for acc states that produce GON output.
 *  Keeps track of the current input being processed.
 */
public abstract class AccStateBase<I, E, K, V>
    implements AccStateGon<I, E, K, V>
{
    protected AccStateGon<I, E, K, V> parent;

    // Usually the mapping of input to stateId is independent of the current state and could thus be part of the context
    // But it seems more general if we store it here.
    // protected BiFunction<I, E, Object> inputToStateId;

    /** The materialized value - requires materialization to be enabled in the context */
    // protected JsonElement value = null;
    // protected O value = null;
    protected Object oldSourceNode; // Old value stored for debugging

    protected boolean hasBegun = false;
    protected Object currentSourceNode; // can be null

    protected I parentInput;

    /** Copy of the input passed to the most recent call of {@link #transition(Object, Object)}. */
    protected I currentInput;

    protected boolean skipOutput = false;
    protected AccContext<K, V> context;

    protected Object stateId;

//    protected Object getInputStateId(I input, E env) {
//        Object result = inputToStateId.apply(input, env);
//        return result;
//    }

    @Override
    public Object getStateId() {
        return stateId;
    }

    @Override
    public void setParent(AccStateGon<I, E, K, V> parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Parent already set");
        }
        this.parent = parent;
    }

    @Override
    public void setContext(AccContext<K, V> context) {
        this.context = context;
        setContextOnChildren(context);
    }

    protected void setContextOnChildren(AccContext<K, V> context) {
        children().forEachRemaining(child -> child.setContext(context));
    }

    @Override
    public AccStateGon<I, E, K, V> getParent() {
        return parent;
    }

    @Override
    public boolean hasBegun() {
        return hasBegun;
    }

    public void ensureBegun() {
        Preconditions.checkState(hasBegun == true);
    }

    @Override
    public final AccStateGon<I, E, K, V> transition(Object inputStateId, I input, E env) throws IOException {
        ensureBegun();
        this.currentInput = input;
        AccStateGon<I, E, K, V> result = transitionActual(inputStateId, input, env);
        return result;
    }


    /**
     *
     * @param joinTuple The values by which the tuple that is about to be passed to transition joined with the parent accumulator.
     * @param cxt
     * @param skipOutput
     * @throws IOException
     */
    @Override
    public final void begin(Object fromStateId, I parentInput, E env, boolean skipOutput) throws IOException {
        if (hasBegun) {
            throw new IllegalStateException("begin() has already been called() with " + currentSourceNode);
        }
        this.hasBegun = true;
        this.currentSourceNode = null; //sourceId;
        this.currentInput = null;
        this.skipOutput = skipOutput;

        this.parentInput = parentInput;
        // this.stateId = stateId;

        beginActual();
    }

    @Override
    public final void end() throws IOException {
        if (!hasBegun) {
            throw new IllegalStateException("end() was called without matching begin() at " + currentSourceNode);
        }
        endActual();

        this.oldSourceNode = currentSourceNode;
        this.currentSourceNode = null;
        this.hasBegun = false;
        // this.context = null;
    }

    public abstract AccStateGon<I, E, K, V> transitionActual(Object inputStateId, I input, E env) throws IOException;

    /**
     * @throws IOException
     */
    protected void beginActual() throws IOException {}

    /**
     * @throws IOException
     */
    protected void endActual() throws IOException {}



//    public static String toString(ConnectiveNode node) {
//        String result;
//        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//            try (PrintStream ps = new PrintStream(baos, false, StandardCharsets.UTF_8)) {
//                ConnectiveVisitorToString visitor = new ConnectiveVisitorToString(ps, "");
//                node.accept(visitor);
//                ps.flush();
//            }
//            baos.flush();
//            result = baos.toString(StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            // Should never happen
//            throw new RuntimeException(e);
//        }
//        return result;
//    }
//
//    public static void print(PrintStream out, String baseIndent, String baseText, String value) {
//        if (value == null) {
//            out.print(baseIndent);
//            out.println(Objects.toString(value));
//        } else {
//            String blank = " ".repeat(baseText.length());
//            String[] strs = value.split(System.lineSeparator());
//            for (int i = 0; i < strs.length; ++i) {
//                out.print(baseIndent);
//                out.print(i == 0 ? baseText : blank);
//                out.println(strs[i]);
//            }
//        }
//    }
}
