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

/** Base class that evaluates everything to <tt>null</tt>. */
public class EvaluationBase<T>
    implements Evaluation<T>
{
    @Override public T apply(OpTable opUnit) { return null; }
    @Override public T apply(OpBGP opBGP) { return null; }
    @Override public T apply(OpTriple opTriple) { return null; }
    @Override public T apply(OpQuad opQuad) { return null; }
    @Override public T apply(OpPath opPath) { return null; }
    @Override public T apply(OpDatasetNames dsNames) { return null; }
    @Override public T apply(OpQuadPattern quadPattern) { return null; }
    @Override public T apply(OpQuadBlock quadBlock) { return null; }
    @Override public T apply(OpNull opNull) { return null; }
    @Override public T apply(OpFilter opFilter, T subOp) { return null; }
    @Override public T apply(OpGraph opGraph, T subOp) { return null; }
    @Override public T apply(OpService opService, T subOp) { return null; }
    @Override public T apply(OpProcedure opProcedure, T subOp) { return null; }
    @Override public T apply(OpPropFunc opPropFunc, T subOp) { return null; }
    @Override public T apply(OpLabel opLabel, T subOp) { return null; }
    @Override public T apply(OpAssign opAssign, T subOp) { return null; }
    @Override public T apply(OpExtend opExtend, T subOp) { return null; }
    @Override public T apply(OpJoin opJoin, T left, T right) { return null; }
    @Override public T apply(OpLeftJoin opLeftJoin, T left, T right) { return null; }
    @Override public T apply(OpDiff opDiff, T left, T right) { return null; }
    @Override public T apply(OpMinus opMinus, T left, T right) { return null; }
    @Override public T apply(OpUnion opUnion, T left, T right) { return null; }
    @Override public T apply(OpLateral opLater, T left, T right) { return null; }
    @Override public T apply(OpConditional opCondition, T left, T right) { return null; }
    @Override public T apply(OpSequence opSequence, List<T> elts) { return null; }
    @Override public T apply(OpDisjunction opDisjunction, List<T> elts) { return null; }
    @Override public T apply(OpExt opExt) { return null; }
    @Override public T apply(OpList opList, T subOp) { return null; }
    @Override public T apply(OpOrder opOrder, T subOp) { return null; }
    @Override public T apply(OpTopN opTop, T subOp) { return null; }
    @Override public T apply(OpProject opProject, T subOp) { return null; }
    @Override public T apply(OpDistinct opDistinct, T subOp) { return null; }
    @Override public T apply(OpReduced opReduced, T subOp) { return null; }
    @Override public T apply(OpSlice opSlice, T subOp) { return null; }
    @Override public T apply(OpGroup opGroup, T subOp) { return null; }
}
