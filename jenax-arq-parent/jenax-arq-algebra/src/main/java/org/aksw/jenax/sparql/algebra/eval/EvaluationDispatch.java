package org.aksw.jenax.sparql.algebra.eval;

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
import org.apache.jena.sparql.algebra.op.OpExt;
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

public class EvaluationDispatch<T>
    implements OpVisitor
{
    protected Stack<T> stack = new Stack<>();
    protected Evaluator<T> evaluator;

    public EvaluationDispatch(Evaluator<T> evaluator) {
        this.evaluator = evaluator;
    }

    public T eval(Op op, T input) {
        push(input);
        int x = stack.size();
        op.visit(this);
        int y = stack.size();
        if ( x != y )
            Log.warn(this, "Possible stack misalignment");
        T value = pop();
        return value;
    }

    @Override
    public void visit(OpBGP opBGP) {
        T input = pop();
        T value = evaluator.eval(opBGP, input);
        push(value);
    }

    @Override
    public void visit(OpQuadPattern quadPattern) {
        T input = pop();
        T value = evaluator.eval(quadPattern, input);
        push(value);
    }

    @Override
    public void visit(OpQuadBlock quadBlock) {
        T input = pop();
        T value = evaluator.eval(quadBlock, input);
        push(value);
    }

    @Override
    public void visit(OpTriple opTriple) {
        T input = pop();
        T value = evaluator.eval(opTriple, input);
        push(value);
    }

    @Override
    public void visit(OpQuad opQuad) {
        T input = pop();
        T value = evaluator.eval(opQuad, input);
        push(value);
    }

    @Override
    public void visit(OpPath opPath) {
        T input = pop();
        T value = evaluator.eval(opPath, input);
        push(value);
    }

    @Override
    public void visit(OpProcedure opProc) {
        T input = pop();
        T value = evaluator.eval(opProc, input);
        push(value);
    }

    @Override
    public void visit(OpPropFunc opPropFunc) {
        T input = pop();
        T value = evaluator.eval(opPropFunc, input);
        push(value);
    }

    @Override
    public void visit(OpJoin opJoin) {
        T input = pop();
        T value = evaluator.eval(opJoin, input);
        push(value);
    }

    @Override
    public void visit(OpSequence opSequence) {
        T input = pop();
        T value = evaluator.eval(opSequence, input);
        push(value);
    }

    @Override
    public void visit(OpDisjunction opDisjunction) {
        T input = pop();
        T value = evaluator.eval(opDisjunction, input);
        push(value);
    }

    @Override
    public void visit(OpLeftJoin opLeftJoin) {
        T input = pop();
        T value = evaluator.eval(opLeftJoin, input);
        push(value);
    }

    @Override
    public void visit(OpDiff opDiff) {
        T input = pop();
        T value = evaluator.eval(opDiff, input);
        push(value);
    }

    @Override
    public void visit(OpMinus opMinus) {
        T input = pop();
        T value = evaluator.eval(opMinus, input);
        push(value);
    }

    @Override
    public void visit(OpUnion opUnion) {
        T input = pop();
        T value = evaluator.eval(opUnion, input);
        push(value);
    }

    @Override
    public void visit(OpLateral opLateral) {
        T input = pop();
        T value = evaluator.eval(opLateral, input);
        push(value);
    }

    @Override
    public void visit(OpConditional opCondition) {
        T input = pop();
        T value = evaluator.eval(opCondition, input);
        push(value);
    }

    @Override
    public void visit(OpFilter opFilter) {
        T input = pop();
        T value = evaluator.eval(opFilter, input);
        push(value);
    }

    @Override
    public void visit(OpGraph opGraph) {
        T input = pop();
        T value = evaluator.eval(opGraph, input);
        push(value);
    }

    @Override
    public void visit(OpService opService) {
        T input = pop();
        T value = evaluator.eval(opService, input);
        push(value);
    }

    @Override
    public void visit(OpDatasetNames dsNames) {
        T input = pop();
        T value = evaluator.eval(dsNames, input);
        push(value);
    }

    @Override
    public void visit(OpTable opTable) {
        T input = pop();
        T value = evaluator.eval(opTable, input);
        push(value);
    }

    @Override
    public void visit(OpExt opExt) {
        T input = pop();
        T value = evaluator.eval(opExt, input);
        push(value);
    }

    @Override
    public void visit(OpNull opNull) {
        T input = pop();
        T value = evaluator.eval(opNull, input);
        push(value);
    }

    @Override
    public void visit(OpLabel opLabel) {
        T input = pop();
        T value = evaluator.eval(opLabel, input);
        push(value);
    }

    @Override
    public void visit(OpList opList) {
        T input = pop();
        T value = evaluator.eval(opList, input);
        push(value);
    }

    @Override
    public void visit(OpOrder opOrder) {
        T input = pop();
        T value = evaluator.eval(opOrder, input);
        push(value);
    }

    @Override
    public void visit(OpProject opProject) {
        T input = pop();
        T value = evaluator.eval(opProject, input);
        push(value);
    }

    @Override
    public void visit(OpDistinct opDistinct) {
        T input = pop();
        T value = evaluator.eval(opDistinct, input);
        push(value);
    }

    @Override
    public void visit(OpReduced opReduced) {
        T input = pop();
        T value = evaluator.eval(opReduced, input);
        push(value);
    }

    @Override
    public void visit(OpAssign opAssign) {
        T input = pop();
        T value = evaluator.eval(opAssign, input);
        push(value);
    }

    @Override
    public void visit(OpExtend opExtend) {
        T input = pop();
        T value = evaluator.eval(opExtend, input);
        push(value);
    }

    @Override
    public void visit(OpSlice opSlice) {
        T input = pop();
        T value = evaluator.eval(opSlice, input);
        push(value);
    }

    @Override
    public void visit(OpGroup opGroup) {
        T input = pop();
        T value = evaluator.eval(opGroup, input);
        push(value);
    }

    @Override
    public void visit(OpTopN opTop) {
        T input = pop();
        T value = evaluator.eval(opTop, input);
        push(value);
    }

    private void push(T value) {
        stack.push(value);
    }

    private T pop() {
        if ( stack.size() == 0 )
            Log.warn(this, "Warning: pop: empty stack");
        return stack.pop();
    }
}
