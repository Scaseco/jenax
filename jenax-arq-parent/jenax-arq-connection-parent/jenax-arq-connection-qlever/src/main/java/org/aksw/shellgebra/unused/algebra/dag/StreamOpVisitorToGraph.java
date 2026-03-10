package org.aksw.shellgebra.unused.algebra.dag;

import java.util.function.Supplier;

import org.aksw.shellgebra.algebra.common.OpSpecConcat;
import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpCommand;
import org.aksw.shellgebra.algebra.stream.op.StreamOpConcat;
import org.aksw.shellgebra.algebra.stream.op.StreamOpContentConvert;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVar;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVisitor;
import org.jgrapht.graph.DirectedAcyclicGraph;

public class StreamOpVisitorToGraph
    implements StreamOpVisitor<OpSpecNode>
{
    public static Supplier<StreamNode> vertexSupplier() {
        int[] nextId = {0};
        return () -> new StreamNode("" + (nextId[0]++));
    }

    public StreamOpVisitorToGraph() {
        this(new DirectedAcyclicGraph<>(OpSpecNode.vertexSupplier(), OpSpecEdge::new, false));
    }

    public StreamOpVisitorToGraph(DirectedAcyclicGraph<OpSpecNode, OpSpecEdge> dag) {
        this.dag = dag;
    }

    public DirectedAcyclicGraph<OpSpecNode, OpSpecEdge> getDag() {
        return dag;
    }

    private DirectedAcyclicGraph<OpSpecNode, OpSpecEdge> dag;

    @Override
    public OpSpecNode visit(StreamOpFile op) {
        return dag.addVertex().setOpSpec(op.opSpec());
    }

    @Override
    public OpSpecNode visit(StreamOpTranscode op) {
        OpSpecNode result = dag.addVertex().setOpSpec(op.getTranscoding());
        OpSpecNode child = op.getSubOp().accept(this);
        dag.addEdge(result, child);
        return result;
    }

    @Override
    public OpSpecNode visit(StreamOpContentConvert op) {
        OpSpecNode result = dag.addVertex().setOpSpec(op.getContentConvertSpec());
        OpSpecNode child = op.getSubOp().accept(this);
        dag.addEdge(result, child);
        return result;
    }

    @Override
    public OpSpecNode visit(StreamOpConcat op) {
        OpSpecNode result = dag.addVertex().setOpSpec(new OpSpecConcat());
        int i = 0;
        for (StreamOp subOp : op.getSubOps()) {
            OpSpecNode child = subOp.accept(this);
            dag.addEdge(result, child, new OpSpecEdge(i++));
        }
        return result;
    }

    @Override
    public OpSpecNode visit(StreamOpCommand op) {
        throw new RuntimeException("Command not supported yet.");
    }

    @Override
    public OpSpecNode visit(StreamOpVar op) {
        throw new RuntimeException("Variable not supported (should it?)");
    }
}
