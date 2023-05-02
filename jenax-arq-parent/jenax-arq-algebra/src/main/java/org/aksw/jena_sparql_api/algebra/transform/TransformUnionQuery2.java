package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorBlacklist;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;

public class TransformUnionQuery2 extends TransformCopy
{
    // ** SEE AlgebraQuad : Pusher and Popper :share.
    protected Deque<Node> currentGraph;
    protected Generator<Var> varGen;

    public TransformUnionQuery2(Generator<Var> varGen) {
        super();
        this.currentGraph = new ArrayDeque<>();
        currentGraph.push(Quad.defaultGraphNodeGenerated) ;
        this.varGen = varGen;
    }

    public static Op transform(Op op)
    {
        Collection<Var> blacklist = OpVars.mentionedVars(op);
        // Deque<Node> currentGraph = new ArrayDeque<>();
        Generator<Var> varGen = GeneratorBlacklist.create(VarGeneratorImpl2.create(), blacklist);

        TransformUnionQuery2 t = new TransformUnionQuery2(varGen);
        Op op2 = Transformer.transform(t, op, new Pusher(t.currentGraph), new Popper(t.currentGraph));
        return op2 ;
    }

    @Override
    public Op transform(OpQuadPattern quadPattern)
    {
        Op result;
        if ( quadPattern.isDefaultGraph() || quadPattern.isUnionGraph() )
        {
            OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern()) ;
            result = union(opBGP) ;
        } else {
            result = super.transform(quadPattern);
        }

        // Leave alone.
        // if ( quadPattern.isExplicitDefaultGraph() ) {}
        return result;
    }

    @Override
    public Op transform(OpBGP opBGP)
    {
        Op result;
        Node current = currentGraph.peek();
        if (current == Quad.defaultGraphNodeGenerated || current == Quad.unionGraph) {
            result = union(opBGP);
        } else {
            result = super.transform(opBGP);
        }
        return result;
    }

    @Override
    public Op transform(OpGraph opGraph, Op x)
    {
        // Remove any Quad.unionGraph - OpBGPs will be rewritten.
        return super.transform(opGraph, x) ;
    }

    static class Pusher extends OpVisitorBase
    {
        private Deque<Node> stack ;
        Pusher(Deque<Node> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph)
        {
            stack.push(opGraph.getNode()) ;
        }
    }

    protected Op union(OpBGP opBGP)
    {
        // QuadPattern quads = new QuadPattern();
        List<Op> ops = opBGP.getPattern().getList().stream()
            .map(t -> {
                BasicPattern bp = new BasicPattern();
                bp.add(t);
                return (Op)new OpQuadPattern(varGen.next(), bp);
            })
            .collect(Collectors.toList());

        Op result = ops.size() == 1
                ? ops.get(0)
                : OpSequence.create().copy(ops);

        return result;
    }


    static class Popper extends OpVisitorBase
    {
        private Deque<Node> stack ;
        Popper(Deque<Node> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph)
        {
            Node n = stack.pop() ;
            if ( ! opGraph.getNode().equals(n))
                throw new ARQInternalErrorException() ;
        }
    }
}
