package org.aksw.jenax.sparql.algebra.transform2;

import java.util.List;

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

/** Wrap another transform and pass on the transform operation */
public class EvaluationWrapper<T>
    implements Evaluation<T>
{
    protected final Evaluation<T> evaluator ;

    public EvaluationWrapper(Evaluation<T> transform )
    {
        this.evaluator = transform ;
    }

    @Override public T eval(OpTable opTable)                   { return evaluator.eval(opTable) ; }
    @Override public T eval(OpBGP opBGP)                       { return evaluator.eval(opBGP) ; }
    @Override public T eval(OpTriple opTriple)                 { return evaluator.eval(opTriple) ; }
    @Override public T eval(OpQuad opQuad)                     { return evaluator.eval(opQuad) ; }
    @Override public T eval(OpPath opPath)                     { return evaluator.eval(opPath) ; }

    @Override public T eval(OpProcedure opProc, T subOp)       { return evaluator.eval(opProc, subOp) ; }
    @Override public T eval(OpPropFunc opPropFunc, T subOp)    { return evaluator.eval(opPropFunc, subOp) ; }

    @Override public T eval(OpDatasetNames dsNames)            { return evaluator.eval(dsNames) ; }
    @Override public T eval(OpQuadPattern quadPattern)         { return evaluator.eval(quadPattern) ; }
    @Override public T eval(OpQuadBlock quadBlock)             { return evaluator.eval(quadBlock) ; }

    @Override public T eval(OpFilter opFilter, T subOp)        { return evaluator.eval(opFilter, subOp) ; }
    @Override public T eval(OpGraph opGraph, T subOp)          { return evaluator.eval(opGraph, subOp) ; }
    @Override public T eval(OpService opService, T subOp)      { return evaluator.eval(opService, subOp) ; }

    @Override public T eval(OpAssign opAssign, T subOp)        { return evaluator.eval(opAssign, subOp) ; }
    @Override public T eval(OpExtend opExtend, T subOp)        { return evaluator.eval(opExtend, subOp) ; }

    @Override public T eval(OpJoin opJoin, T left, T right)           { return evaluator.eval(opJoin, left, right) ; }
    @Override public T eval(OpLeftJoin opLeftJoin, T left, T right)   { return evaluator.eval(opLeftJoin, left, right) ; }
    @Override public T eval(OpDiff opDiff, T left, T right)           { return evaluator.eval(opDiff, left, right) ; }
    @Override public T eval(OpMinus opMinus, T left, T right)         { return evaluator.eval(opMinus, left, right) ; }
    @Override public T eval(OpUnion opUnion, T left, T right)         { return evaluator.eval(opUnion, left, right) ; }
    @Override public T eval(OpLateral opLateral, T left, T right)     { return evaluator.eval(opLateral, left, right) ; }
    @Override public T eval(OpConditional opCond, T left, T right)    { return evaluator.eval(opCond, left, right) ; }

    @Override public T eval(OpSequence opSequence, List<T> elts)       { return evaluator.eval(opSequence, elts) ; }
    @Override public T eval(OpDisjunction opDisjunction, List<T> elts) { return evaluator.eval(opDisjunction, elts) ; }

    @Override public T eval(OpExt opExt)                        { return evaluator.eval(opExt) ; }
    @Override public T eval(OpNull opNull)                      { return evaluator.eval(opNull) ; }
    @Override public T eval(OpLabel opLabel, T subOp)           { return evaluator.eval(opLabel, subOp) ; }

    @Override public T eval(OpList opList, T subOp)             { return evaluator.eval(opList, subOp) ; }
    @Override public T eval(OpOrder opOrder, T subOp)           { return evaluator.eval(opOrder, subOp) ; }
    @Override public T eval(OpTopN opTop, T subOp)              { return evaluator.eval(opTop, subOp) ; }
    @Override public T eval(OpProject opProject, T subOp)       { return evaluator.eval(opProject, subOp) ; }
    @Override public T eval(OpDistinct opDistinct, T subOp)     { return evaluator.eval(opDistinct, subOp) ; }
    @Override public T eval(OpReduced opReduced, T subOp)       { return evaluator.eval(opReduced, subOp) ; }
    @Override public T eval(OpSlice opSlice, T subOp)           { return evaluator.eval(opSlice, subOp) ; }
    @Override public T eval(OpGroup opGroup, T subOp)           { return evaluator.eval(opGroup, subOp) ; }
}
