package org.aksw.jenax.sparql.algebra.evaluate;

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

    @Override public T apply(OpTable opTable)                   { return evaluator.apply(opTable) ; }
    @Override public T apply(OpBGP opBGP)                       { return evaluator.apply(opBGP) ; }
    @Override public T apply(OpTriple opTriple)                 { return evaluator.apply(opTriple) ; }
    @Override public T apply(OpQuad opQuad)                     { return evaluator.apply(opQuad) ; }
    @Override public T apply(OpPath opPath)                     { return evaluator.apply(opPath) ; }

    @Override public T apply(OpProcedure opProc, T subOp)       { return evaluator.apply(opProc, subOp) ; }
    @Override public T apply(OpPropFunc opPropFunc, T subOp)    { return evaluator.apply(opPropFunc, subOp) ; }

    @Override public T apply(OpDatasetNames dsNames)            { return evaluator.apply(dsNames) ; }
    @Override public T apply(OpQuadPattern quadPattern)         { return evaluator.apply(quadPattern) ; }
    @Override public T apply(OpQuadBlock quadBlock)             { return evaluator.apply(quadBlock) ; }

    @Override public T apply(OpFilter opFilter, T subOp)        { return evaluator.apply(opFilter, subOp) ; }
    @Override public T apply(OpGraph opGraph, T subOp)          { return evaluator.apply(opGraph, subOp) ; }
    @Override public T apply(OpService opService, T subOp)      { return evaluator.apply(opService, subOp) ; }

    @Override public T apply(OpAssign opAssign, T subOp)        { return evaluator.apply(opAssign, subOp) ; }
    @Override public T apply(OpExtend opExtend, T subOp)        { return evaluator.apply(opExtend, subOp) ; }

    @Override public T apply(OpJoin opJoin, T left, T right)           { return evaluator.apply(opJoin, left, right) ; }
    @Override public T apply(OpLeftJoin opLeftJoin, T left, T right)   { return evaluator.apply(opLeftJoin, left, right) ; }
    @Override public T apply(OpDiff opDiff, T left, T right)           { return evaluator.apply(opDiff, left, right) ; }
    @Override public T apply(OpMinus opMinus, T left, T right)         { return evaluator.apply(opMinus, left, right) ; }
    @Override public T apply(OpUnion opUnion, T left, T right)         { return evaluator.apply(opUnion, left, right) ; }
    @Override public T apply(OpLateral opLateral, T left, T right)     { return evaluator.apply(opLateral, left, right) ; }
    @Override public T apply(OpConditional opCond, T left, T right)    { return evaluator.apply(opCond, left, right) ; }

    @Override public T apply(OpSequence opSequence, List<T> elts)       { return evaluator.apply(opSequence, elts) ; }
    @Override public T apply(OpDisjunction opDisjunction, List<T> elts) { return evaluator.apply(opDisjunction, elts) ; }

    @Override public T apply(OpExt opExt)                        { return evaluator.apply(opExt) ; }
    @Override public T apply(OpNull opNull)                      { return evaluator.apply(opNull) ; }
    @Override public T apply(OpLabel opLabel, T subOp)           { return evaluator.apply(opLabel, subOp) ; }

    @Override public T apply(OpList opList, T subOp)             { return evaluator.apply(opList, subOp) ; }
    @Override public T apply(OpOrder opOrder, T subOp)           { return evaluator.apply(opOrder, subOp) ; }
    @Override public T apply(OpTopN opTop, T subOp)              { return evaluator.apply(opTop, subOp) ; }
    @Override public T apply(OpProject opProject, T subOp)       { return evaluator.apply(opProject, subOp) ; }
    @Override public T apply(OpDistinct opDistinct, T subOp)     { return evaluator.apply(opDistinct, subOp) ; }
    @Override public T apply(OpReduced opReduced, T subOp)       { return evaluator.apply(opReduced, subOp) ; }
    @Override public T apply(OpSlice opSlice, T subOp)           { return evaluator.apply(opSlice, subOp) ; }
    @Override public T apply(OpGroup opGroup, T subOp)           { return evaluator.apply(opGroup, subOp) ; }
}
