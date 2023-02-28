package org.aksw.jenax.sparql.algebra.transform2;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLateral;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.walker.ApplyTransformVisitor;
import org.apache.jena.sparql.algebra.walker.WalkerVisitor;

/**
 * Modeled after {@link ApplyTransformVisitor}.
 * Apply the {@link Evaluation}. Works in conjunction with {@link WalkerVisitor}.
 */
public class ApplyEvaluationVisitor<T> implements OpVisitor {
    protected final Evaluation<T>     evaluator;
    protected final Stack<T>      opStack   = new Stack<>() ;

    public ApplyEvaluationVisitor(Evaluation<T> evaluator) {
        this.evaluator = evaluator;
    }

    public final T opResult() {
        return pop(opStack) ;
    }

//    private void dump(String label) {
//        System.out.println(label) ;
//        String x = opStack.toString().replace('\n', ' ').replaceAll("  +", " ") ;
//        System.out.println("    O:"+x);
//    }

    private void push(Stack<T> stack, T value) {
        if ( value == null )
            Log.warn(ApplyEvaluationVisitor.class, "Pushing null onto the "+stackLabel(stack)+" stack") ;
        stack.push(value) ;
    }

    private T pop(Stack<T> stack) {
        try {
            T v = stack.pop() ;
            if ( v ==  null )
                Log.warn(ApplyEvaluationVisitor.class, "Pop null from the "+stackLabel(stack)+" stack") ;
            return v ;
        }
        catch (NoSuchElementException ex) {
            // if ( true )
            throw new RuntimeException(ex) ;
            // Log.warn(ApplyEvaluationVisitor.class, "Empty "+stackLabel(stack)+" stack") ;
            // return null ;
        }
    }

    public T pop(Stack<T> stack, Op op) {
        T result = op != null
                ? pop(stack)
                : null;
        return result;
    }

    public List<T> pop(Stack<T> stack, List<Op> ops) {
        int n = ops.size();
        List<T> result = new ArrayList<>(n);
        ListIterator<Op> it = ops.listIterator(n);
        int i = n;
        while (it.hasPrevious()) {
            --i;
            Op op = it.previous();
            T value = pop(stack, op);
            result.set(i, value);
        }
        return result;
    }


    private String stackLabel(Stack<?> stack) {
        if ( stack == opStack ) return "Op" ;
        return "<other>" ;
    }

    @Override
    public void visit(OpBGP op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpQuadPattern op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpQuadBlock op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpTriple op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpQuad op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpPath op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpTable op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpNull op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpProcedure op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpPropFunc op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpGraph op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpDatasetNames op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpLabel op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpJoin op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpUnion op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpDiff op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpMinus op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpLateral op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpConditional op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpSequence op) {
        List<T> values = pop(opStack, op.getElements());
        T value = evaluator.eval(op, values) ;
        push(opStack, value) ;
    }

    @Override
    public void visit(OpDisjunction op) {
        List<T> values = pop(opStack, op.getElements());
        T value = evaluator.eval(op, values) ;
        push(opStack, value) ;
    }

    @Override
    public void visit(OpList op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpProject op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpReduced op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpDistinct op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpSlice op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpTopN op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpFilter op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpService op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value);
    }

    @Override
    public void visit(OpAssign op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpExtend op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpLeftJoin op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpOrder op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpGroup op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }
}
