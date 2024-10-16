package org.aksw.jenax.arq.util.exec.query;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.main.solver.SolverLib;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sparql.mgt.Explain;
import org.apache.jena.system.G;

public class StageGeneratorStreamGeneric
    implements StageGeneratorStream
{
    public StageGeneratorStreamGeneric() {}
    private static final ReorderTransformation reorderFixed = ReorderLib.fixed() ;

    @Override
    public Stream<Binding> execute(BasicPattern pattern, Stream<Binding> input, ExecutionContext execCxt) {
        if ( input == null )
            Log.error(this, "Null input to " + Lib.classShortName(this.getClass())) ;

        // Choose reorder transformation and execution strategy.
        ReorderTransformation reorder = reorderFixed ;
        return execute(pattern, reorder, input, execCxt) ;
    }

    protected Stream<Binding> execute(BasicPattern pattern, ReorderTransformation reorder,
                                    Stream<Binding> input, ExecutionContext execCxt) {
        Explain.explain(pattern, execCxt.getContext()) ;
        List<Triple> triples = pattern.getList();
        Stream<Binding> chain = input;
        for (Triple triple : triples) {
            chain = chain.flatMap(b -> {
//                if (reorder != null && pattern.size() >= 2) {
//                    BasicPattern bgp2 = Substitute.substitute(pattern, b) ;
//                    ReorderProc reorderProc = reorder.reorderIndexes(bgp2) ;
//                    BasicPattern reorderedPattern = reorderProc.reorder(pattern) ;
//                }
                return execute(input, execCxt.getActiveGraph(), triple, null, execCxt);
            });
        }
        Explain.explain("Reorder/generic", pattern, execCxt.getContext()) ;
        return chain;


//        if ( reorder != null && pattern.size() >= 2 ) {
//            // If pattern size is 0 or 1, nothing to do.
//            BasicPattern bgp2 = pattern ;
//
//            // Try to ground the pattern
//            if ( ! input.isJoinIdentity() ) {
//                QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
//                // And now use this one
//                input = peek ;
//                Binding b = peek.peek() ;
//            }
//            ReorderProc reorderProc = reorder.reorderIndexes(bgp2) ;
//            pattern = reorderProc.reorder(pattern) ;
//        }
        // return PatternMatchData.execute(execCxt.getActiveGraph(), pattern, input, null, execCxt);
    }

    public Stream<Binding> execute(Stream<Binding> input, Graph graph, Triple pattern, Predicate<Triple> filter, ExecutionContext execCxt) {
        return accessTriple(input, graph, pattern, filter, execCxt);
    }

    public static Stream<Binding> accessTriple(Stream<Binding> input, Graph graph, Triple pattern,
                   Predicate<Triple> filter, ExecutionContext execCxt) {
        return input.flatMap(b -> accessTriple(b, graph, pattern, filter, execCxt));
    }

    private static Stream<Binding> accessTriple(Binding binding, Graph graph, Triple pattern, Predicate<Triple> filter, ExecutionContext execCxt) {
        Node s = substituteFlat(pattern.getSubject(), binding) ;
        Node p = substituteFlat(pattern.getPredicate(), binding) ;
        Node o = substituteFlat(pattern.getObject(), binding) ;
        // BindingBuilder resultsBuilder = Binding.builder(binding);
        Node s2 = NodeUtils.nullOrVarToAny(s) ;
        Node p2 = NodeUtils.nullOrVarToAny(p) ;
        Node o2 = NodeUtils.nullOrVarToAny(o) ;
        // ExtendedIterator<Triple> graphIter = graph.find(s2, p2, o2) ;
        // Language tags.
        Stream<Triple> graphIter = findByLang(graph, s2, p2, o2);
        Stream<Binding> iter = graphIter
                .map(r -> mapper(Binding.builder(binding), s, p, o, r))
                .filter(Objects::nonNull);
        return iter;
    }

    // Variable or not a variable. Not recursively inside a triple term <<?var>>
    private static Node substituteFlat(Node n, Binding binding) {
        return Var.lookup(binding::get, n);
    }

    /** Contains, and language tags match case-insentively */
    public static Stream<Triple> findByLang(Graph g, Node s, Node p, Node o) {
        // No specific value given.
        if ( G.isNullOrAny(o) )
            return g.stream(s, p, o);
        if ( ! G.hasLang(o) )
            // Not a language literal. find(s,p,o) is enough.
            return g.stream(s,p,o);
        // Filter by language value.
        Stream<Triple> iter = g.stream(s, p, Node.ANY)
                                         .filter(triple->G.sameTermMatch(o, triple.getObject()));
        return iter;
    }

    private static Binding mapper(BindingBuilder resultsBuilder, Node s, Node p, Node o, Triple r) {
        resultsBuilder.reset();
        if ( !insert(resultsBuilder, s, r.getSubject()) )
            return null;
        if ( !insert(resultsBuilder, p, r.getPredicate()) )
            return null;
        if ( !insert(resultsBuilder, o, r.getObject()) )
            return null;
        return resultsBuilder.build();
    }

    private static boolean insert(BindingBuilder results, Node patternNode, Node dataNode) {
        if ( !Var.isVar(patternNode) )
            return true;
        Var v = Var.alloc(patternNode);
        Node x = results.get(v);
        if ( x != null )
            return SolverLib.sameTermAs(dataNode, x);
        results.add(v, dataNode);
        return true;
    }
}
