package org.aksw.jenax.io.json.accumulator;

/**
 * Accumulate keys from keys and values.
 * An AccJsonObject is needed as the wrapping object.
 *
 * At AccJsonEdge where the keys are dynamically produced from the data.
 *
 * Most accumulators accept triples of the form (source, fieldId, value).
 * This accumulator accepts quads of the form (source, fieldId key, value).
 *
 * Also, cardinality could be based on key (and source).
 *
 *
 *
 * Consideration: Could we map this to triples with intermediate laterals?
 *
 * We would like to bind "p" only once, and then list all "o"s for it.
 * Without repeating patterns it doesn't seems like its doable: We'd need extra querys for distinct ?ps.
 *
 * This way, we could have a triple with a fieldId "p" and a p value, and a nested state that collects the o values.
 *
 * ?s a Foo .
 * LATERAL {
 *   {
 *     {
 *       { SELECT DISTINCT ?p { # Get distinct ?ps first, then find all objects for (s, p)
 *         ?s ?p []
 *       }
 *       BIND("p" AS ?fieldId) }
 *     }
 *     LATERAL {
 *       ?s ?p ?o
 *     }
 *   }
 *
 * }
 *
 * p: [o]
 *
 *
 */
//public class AccJsonIndex
//    extends AccJsonBase
//{
//    protected Fragment fragment;
//    protected Var sourceVar;
//    protected Var indexVar;
//    // protected P_Path0
//    // protected Map<P_Path0, Var> fieldToVar = new LinkedHashMap<>();
//
//    protected Map<Node, Integer> fieldIdToIndex = new HashMap<>();
//    protected AccJson[] subAccs = new AccJsonEdge[0];
//
//    // protected AccJson[] subMappers;
//
//    // Submapper for each variable
//    // protected Map<Var, AccJsonBase> subMappers = new LinkedHashMap<>();
//
//
//    /** Should not be used directly; use {@link AggJsonEdge} as the builder */
////    public AccJsonObject() {
////        this(new HashMap<>(), new AccJsonEdge[0]);
////    }
//
//    protected AccJsonIndex(Map<Node, Integer> fieldIdToIndex, AccJsonEdge[] edgeAccs) {
//        super(fieldIdToIndex, edgeAccs);
//    }
//
//    /** Create a new instance and set it as the parent on all the property accumulators */
//    public static AccJsonObject of(Map<Node, Integer> fieldIdToIndex, AccJsonEdge[] edgeAccs) {
//        AccJsonObject result = new AccJsonObject(fieldIdToIndex, edgeAccs);
//        for (AccJsonEdge acc : edgeAccs) {
//            acc.setParent(result);
//        }
//        return result;
//    }
//
//    @Override
//    public void begin(Node source, AccContext context, boolean skipOutput) throws IOException {
//        super.begin(source, context, skipOutput);
//
//        // Reset fields
//        currentFieldIndex = -1;
//        currentFieldAcc = null;
//
//        if (!skipOutput) {
//            if (context.isMaterialize()) {
//                value = new RdfObjectImpl(source); // new JsonObject();
//            }
//
//            if (context.isSerialize()) {
//                context.getJsonWriter().beginObject();
//            }
//        }
//    }
//
//    @Override
//    public AccJson transition(Triple input, AccContext context) throws IOException { throw new UnsupportedOperationException(); }
//
//    // New design operates on tuples rather than triples
//    public AccJson transition(Tuple<Node> input, AccContext context) throws IOException {
//        ensureBegun();
//
//        Node inputFieldId = input.get(1); //input.getPredicate();
//
//        AccJson result = null;
//        Integer inputFieldIndex = fieldIdToIndex.get(inputFieldId);
//        if (inputFieldIndex != null) {
//            int requestedFieldIndex = inputFieldIndex.intValue();
//
//            // Detect if the requested field comes before the current field
//            // This should only happen if there is a new source
//            // Sanity check: Check that the source of this field is different from the current sourceNode
//            if (requestedFieldIndex < currentFieldIndex) {
//                AccJsonEdge edgeAcc = edgeAccs[requestedFieldIndex];
//                Node inputSource = TripleUtils.getSource(input, edgeAcc.isForward());
//                if (Objects.equals(inputSource, currentSourceNode)) {
//                    throw new RuntimeException("fields appear to have arrived out of order - should not happen");
//                    // TODO Improve error message: on sourceNode data for field [] was expected to arrive after field []
//                }
//            }
//
//            // Skip over the remaining fields - allow them to produce
//            // values such as null or empty arrays
//            for (int i = currentFieldIndex + 1; i < requestedFieldIndex; ++i) {
//                AccJsonEdge edgeAcc = edgeAccs[i];
//                edgeAcc.begin(null, context, skipOutput);
//                edgeAcc.end(context);
//            }
//
//            currentFieldIndex = requestedFieldIndex;
//            currentFieldAcc = edgeAccs[requestedFieldIndex];
//
//            boolean isForward = currentFieldAcc.isForward();
//            Node edgeInputSource = TripleUtils.getSource(input, isForward);
//
//            if (!Objects.equals(edgeInputSource, currentSourceNode)) {
//                throw new RuntimeException("should not happen - node at " + currentSourceNode + " but edge claims " + edgeInputSource);
//            }
//
//            currentFieldAcc.begin(edgeInputSource, context, skipOutput);
//            result = currentFieldAcc.transition(input, context);
//        }
//        return result;
//    }
//
//    @Override
//    public void end(AccContext context) throws IOException {
//        ensureBegun();
//
//        // Visit all remaining fields
//        for (int i = currentFieldIndex + 1; i < edgeAccs.length; ++i) {
//            AccJsonEdge edgeAcc = edgeAccs[i];
//            // Edge.begin receives the target of an edge - but there is none so we pass null
//
//            // With these calls we tell the fields that there is no value
//            edgeAcc.begin(null, context, skipOutput); // TODO Passing 'null' as the start node to indicate absent values is perhaps not the best API contract
//            edgeAcc.end(context);
//        }
//
//        if (!skipOutput) {
//            if (context.isMaterialize() && parent != null) {
//                parent.acceptContribution(value, context);
//            }
//
//            if (context.isSerialize()) {
//                context.getJsonWriter().endObject();
//            }
//        }
//
//        currentFieldIndex = -1;
//        currentFieldAcc = null;
//        super.end(context);
//    }
//
//    @Override
//    public String toString() {
//        return "AccJsonNodeObject (source: " + currentSourceNode + ", field: " + currentFieldAcc + ")";
//    }
//}
