package org.aksw.jenax.arq.util.graph;

import static org.apache.jena.sparql.engine.main.solver.SolverLib.nodeTopLevel;

import java.util.List;
import java.util.function.Predicate;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterFilterExpr;
import org.apache.jena.sparql.engine.iterator.QueryIterPeek;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;
import org.apache.jena.sparql.engine.main.StageGeneratorGeneric;
import org.apache.jena.sparql.engine.main.solver.SolverRX3;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderProc;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.mgt.Explain;

/** Variant of stage generator for aborting find() calls on graph implementations
 *  that may require a long time until the next binding can be produced.
 *  The filtering of candidates is moved to the QueryIter level.
 */
public class StageGeneratorGraphFindRaw
    extends StageGeneratorGeneric
{
    @Override
    protected QueryIterator execute(BasicPattern pattern, ReorderTransformation reorder,
                                    QueryIterator input, ExecutionContext execCxt) {
        Explain.explain(pattern, execCxt.getContext()) ;

        if ( ! input.hasNext() )
            return input ;

        if ( reorder != null && pattern.size() >= 2 ) {
            // If pattern size is 0 or 1, nothing to do.
            BasicPattern bgp2 = pattern ;

            // Try to ground the pattern
            if ( ! input.isJoinIdentity() ) {
                QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
                // And now use this one
                input = peek ;
                Binding b = peek.peek() ;
                bgp2 = Substitute.substitute(pattern, b) ;
            }
            ReorderProc reorderProc = reorder.reorderIndexes(bgp2) ;
            pattern = reorderProc.reorder(pattern) ;
        }
        Explain.explain("Reorder/generic", pattern, execCxt.getContext()) ;
        // return PatternMatchData.execute(execCxt.getActiveGraph(), pattern, input, null, execCxt);
        return execute(execCxt.getActiveGraph(), pattern, input, null, execCxt);
    }

    /**
     * Non-reordering execution of a triple pattern (basic graph pattern),
     * given an iterator of bindings as input.
     */
    public static QueryIterator execute(Graph graph, BasicPattern pattern,
                                        QueryIterator input, Predicate<Triple> filter,
                                        ExecutionContext execCxt)
    {
        List<Triple> triples = pattern.getList();
        QueryIterator chain = input;
        for ( Triple triple : triples ) {
            chain = rdfStarTriple(chain, graph, triple, execCxt);
        }
        return chain;
    }

    public static QueryIterator rdfStarTriple(QueryIterator chain, Graph graph, Triple tPattern, ExecutionContext execCxt) {
        return rdfStarTripleSub(chain, graph, tPattern, execCxt);
    }

    private static QueryIterator rdfStarTripleSub(QueryIterator input, Graph graph, Triple tPattern, ExecutionContext execCxt) {
        return new QueryIterRepeatApply(input, execCxt) {
            @Override
            protected QueryIterator nextStage(Binding binding) {
                return rdfStarTripleSub(binding, graph, tPattern, execCxt);
            }
        };
    }


    /** Creates an abortable QueryIter - matching is handled on the QueryIterator level. */
    public static QueryIterator rdfStarTripleSub(Binding input, Graph graph, Triple xPattern, ExecutionContext execCxt) {
        GraphFindRaw gfr = (GraphFindRaw)graph;

        Triple tPattern = Substitute.substitute(xPattern, input);
        Node s = nodeTopLevel(tPattern.getSubject());
        Node p = nodeTopLevel(tPattern.getPredicate());
        Node o = nodeTopLevel(tPattern.getObject());

        Triple lookup = Triple.create(s, p, o);

        Var dummyVar = Var.alloc(ARQConstants.allocVarMarker + "binSearch_noMatchVar");
        Binding NO_MATCH = BindingFactory.binding(dummyVar, NodeValue.TRUE.asNode());

        QueryIterator it = QueryIterPlainWrapper.create(gfr.findRaw(lookup)
            .mapWith(t -> {
                Binding r = SolverRX3.matchTriple(input, t, tPattern);
                if (r == null) {
                    // Create a dummy binding which we filter away
                    r = NO_MATCH;
                }
                return r;
            }));

        it = new QueryIterFilterExpr(it, new E_LogicalNot(new E_Bound(new ExprVar(dummyVar))), execCxt);
        return it;
    }
}
