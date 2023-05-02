package org.aksw.jena_sparql_api.algebra.utils;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jena_sparql_api.algebra.transform.TransformJoinToSequence;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

public class OpServiceUtils {
    /**
     * Utility to extract "function" arguments represented using multiple SERVICE clauses:
     * <pre>
     * SERVICE &lt;my:function:&gt; {
     *   SERVICE &lt;arg1:&gt; { opA }
     *   SERVICE &lt;arg2:&gt; { opB }
     *   SERVICE &lt;arg2:&gt; { opC }
     *   opD # arguments outside of SERVICE not yet supported
     * }
     * </pre>
     * becomes a multimap
     * <pre>
     *   null -&gt; { opD }
     *   arg1 -&gt; { opA }
     *   arg2 -&gt; { opB, opC }
     * </pre>
     *
     * @param op
     * @return
     */
    public static Multimap<String, Op> extractServiceArgs(OpService rootOp) {
        OpSequence seq = OpSequence.create();
        TransformJoinToSequence.addRecursive(seq, rootOp.getSubOp());
        ListMultimap<String, Op> result = ArrayListMultimap.create();
        for (Op op : seq.getElements()) {
            OpService sop = ObjectUtils.castAsOrNull(OpService.class, op);
            if (sop != null) {
                String iri = NodeUtils.getIriOrNull(sop.getService());
                if (iri != null) {
                    result.put(iri,  sop.getSubOp());
                } else {
                    throw new IllegalArgumentException(rootOp + " service member without IRI not allowed");
                }
            }
        }
        return result;
    }

    public static String getIriOrNull(OpService opService) {
        return opService == null ? null : NodeUtils.getIriOrNull(opService.getService());
    }
}
