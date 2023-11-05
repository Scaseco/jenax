package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprVar;

/**
 * Transforms
 * <pre>GRAPH ?g {}</pre>
 * to
 * <pre>SELECT DISTINCT ?g { GRAPH ?g { ?s ?p ?o } }</pre>
 *
 * and transforms
 * <pre>GRAPH &lt;g&gt; {}</pre>
 * to
 * <pre>
 * FILTER EXISTS {
 *   SELECT DISTINCT ?g { GRAPH ?g { ?s ?p ?o } }
 *   FILTER(?g = &lt;g&gt;)
 * }
 * </pre>.
 *
 */
// TODO Maybe we need a static util method to ensure proper scope of the spo variables
public class TransformDatasetNamesViaSpo
    extends TransformCopy
{
    @Override
    public Op transform(OpGraph opGraph, Op subOp) {
        Op result = OpUtils.isUnitTable(subOp)
            ? createGraphNamesOp(opGraph.getNode())
            : super.transform(opGraph, subOp);
        return result;
    }

    @Override
    public Op transform(OpDatasetNames opDatasetNames) {
        Node g = opDatasetNames.getGraphNode();
        Op result = createGraphNamesOp(g);
        return result;
    }

    public static Op createGraphNamesOp(Node graphNode) {
        // Var gv = (Var)graphNode;
        Var gv = graphNode.isVariable()
                ? (Var)graphNode
                : Vars.g;

        Var s = Vars.s;
        Var p = Vars.p;
        Var o = Vars.o;

        Op result = new OpDistinct(
                new OpProject(new OpGraph(gv,
                    new OpBGP(BasicPattern.wrap(List.of(Triple.create(s, p, o))))),
                List.of(gv)));

        if (!graphNode.isVariable()) {
            Op condition = OpFilter.filter(new E_Equals(new ExprVar(gv), ExprLib.nodeToExpr(graphNode)), result);
            result = OpFilter.filter(new E_Exists(condition), OpTable.unit());
        }

        return result;
    }

    public static void main(String[] args) {
        // String queryStr = "SELECT ?g { GRAPH <urn:foo> { } }";
        String queryStr = "SELECT * { GRAPH ?g { } }";
        System.out.println(QueryUtils.applyOpTransform(QueryFactory.create(queryStr),
                op -> Transformer.transform(new TransformDatasetNamesViaSpo(), op)));
    }
}
