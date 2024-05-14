package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Arrays;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Exists;

/**
 * Replace {@code GRAPH X { }}. Useful for triple stores that do not support this operator.
 * Affects mostly Virtuoso Open Source (VOS) versions.
 *
 * This transform should work on both triple and quad algebra forms.
 *
 * Transforms
 * <pre>
 * GRAPH ?g { }
 * </pre>
 * to
 * <pre>
 * { SELECT DISTINCT ?g { GRAPH ?g { ?g_s ?g_p ?g_o } } }
 * </pre>
 *
 *
 * <pre>
 * GRAPH :g { }
 * </pre>
 * to
 * <pre>
 * { FILTER(EXISTS GRAPH :g { ?g_s ?g_p ?g_o } }
 * </pre>
 *
 */
public class TransformOpDatasetNamesToOpGraph
    extends TransformCopy
{
    @Override
    public Op transform(OpGraph op, Op subOp) {
        boolean isNoPattern = OpUtils.isNoPattern(subOp);

        Op result = isNoPattern
            ? transform(op.getNode())
            : super.transform(op, subOp);

        return result;
    }

    @Override
    public Op transform(OpDatasetNames op) {
        return transform(op.getGraphNode());
    }

    public static Op transform(Node node) {
        Op result;
        if (node.isVariable() || node.isBlank()) {
            Var var = (Var)node;
            String prefix = "__" + (node.isVariable() ? node.getName() : node.getBlankNodeLabel()) + "_";
            BasicPattern bgp = new BasicPattern();
            bgp.add(Triple.create(Var.alloc(prefix + "s"), Var.alloc(prefix + "p"), Var.alloc(prefix + "o")));
            result = new OpDistinct(new OpProject(new OpGraph(node, new OpBGP(bgp)), Arrays.asList(var)));
        } else {
            String prefix = "__g_";
            BasicPattern bgp = new BasicPattern();
            bgp.add(Triple.create(Var.alloc(prefix + "s"), Var.alloc(prefix + "p"), Var.alloc(prefix + "o")));
            result = OpFilter.filter(new E_Exists(new OpGraph(node, new OpBGP(bgp))), OpTable.unit());
        }
        return result;
    }
}
