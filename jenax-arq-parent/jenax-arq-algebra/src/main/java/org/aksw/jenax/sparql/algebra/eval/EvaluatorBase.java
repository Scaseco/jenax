package org.aksw.jenax.sparql.algebra.eval;

import org.apache.jena.sparql.algebra.Op;
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

/** Base class that evals everything to <tt>null</tt>. */
public class EvaluatorBase<T>
    implements Evaluator<T>
{
    /** Initialize with @code{this.dispatcher = new EvaluationDispatch(this);} in the implementing sub class. */
    protected EvaluationDispatch<T> dispatcher;

    protected int level = 0;

    protected EvaluatorBase() {
        super();
    }

    // Public interface
    @Override
    public T evalOp(Op op, T input) {
        return eval(op, input);
    }

    // ---- The recursive step.
    protected T eval(Op op, T input) {
        level++;
        T value = dispatcher.eval(op, input);
        // Intentionally not try/finally so exceptions leave some evidence
        // around.
        level--;
        return value;
    }

    @Override public T eval(OpTable opUnit, T input) { return null; }
    @Override public T eval(OpBGP opBGP, T input) { return null; }
    @Override public T eval(OpTriple opTriple, T input) { return null; }
    @Override public T eval(OpQuad opQuad, T input) { return null; }
    @Override public T eval(OpPath opPath, T input) { return null; }
    @Override public T eval(OpDatasetNames dsNames, T input) { return null; }
    @Override public T eval(OpQuadPattern quadPattern, T input) { return null; }
    @Override public T eval(OpQuadBlock quadBlock, T input) { return null; }
    @Override public T eval(OpNull opNull, T input) { return null; }
    @Override public T eval(OpFilter opFilter, T input) { return null; }
    @Override public T eval(OpGraph opGraph, T input) { return null; }
    @Override public T eval(OpService opService, T input) { return null; }
    @Override public T eval(OpProcedure opProcedure, T input) { return null; }
    @Override public T eval(OpPropFunc opPropFunc, T input) { return null; }
    @Override public T eval(OpLabel opLabel, T input) { return null; }
    @Override public T eval(OpAssign opAssign, T input) { return null; }
    @Override public T eval(OpExtend opExtend, T input) { return null; }
    @Override public T eval(OpJoin opJoin,  T input) { return null; }
    @Override public T eval(OpLeftJoin opLeftJoin,  T input) { return null; }
    @Override public T eval(OpDiff opDiff,  T input) { return null; }
    @Override public T eval(OpMinus opMinus,  T input) { return null; }
    @Override public T eval(OpUnion opUnion,  T input) { return null; }
    @Override public T eval(OpLateral opLater,  T input) { return null; }
    @Override public T eval(OpConditional opCondition,  T input) { return null; }
    @Override public T eval(OpSequence opSequence, T input) { return null; }
    @Override public T eval(OpDisjunction opDisjunction, T input) { return null; }
    @Override public T eval(OpExt opExt, T input) { return null; }
    @Override public T eval(OpList opList, T input) { return null; }
    @Override public T eval(OpOrder opOrder, T input) { return null; }
    @Override public T eval(OpTopN opTop, T input) { return null; }
    @Override public T eval(OpProject opProject, T input) { return null; }
    @Override public T eval(OpDistinct opDistinct, T input) { return null; }
    @Override public T eval(OpReduced opReduced, T input) { return null; }
    @Override public T eval(OpSlice opSlice, T input) { return null; }
    @Override public T eval(OpGroup opGroup, T input) { return null; }
}
