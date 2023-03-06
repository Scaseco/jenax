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

/** Base class that evals everything to <tt>null</tt>. */
public class EvaluationBase<T>
    implements Evaluation<T>
{
    @Override public T eval(OpTable opUnit) { return null; }
    @Override public T eval(OpBGP opBGP) { return null; }
    @Override public T eval(OpTriple opTriple) { return null; }
    @Override public T eval(OpQuad opQuad) { return null; }
    @Override public T eval(OpPath opPath) { return null; }
    @Override public T eval(OpDatasetNames dsNames) { return null; }
    @Override public T eval(OpQuadPattern quadPattern) { return null; }
    @Override public T eval(OpQuadBlock quadBlock) { return null; }
    @Override public T eval(OpNull opNull) { return null; }
    @Override public T eval(OpFilter opFilter, T subOp) { return null; }
    @Override public T eval(OpGraph opGraph, T subOp) { return null; }
    @Override public T eval(OpService opService, T subOp) { return null; }
    @Override public T eval(OpProcedure opProcedure, T subOp) { return null; }
    @Override public T eval(OpPropFunc opPropFunc, T subOp) { return null; }
    @Override public T eval(OpLabel opLabel, T subOp) { return null; }
    @Override public T eval(OpAssign opAssign, T subOp) { return null; }
    @Override public T eval(OpExtend opExtend, T subOp) { return null; }
    @Override public T eval(OpJoin opJoin, T left, T right) { return null; }
    @Override public T eval(OpLeftJoin opLeftJoin, T left, T right) { return null; }
    @Override public T eval(OpDiff opDiff, T left, T right) { return null; }
    @Override public T eval(OpMinus opMinus, T left, T right) { return null; }
    @Override public T eval(OpUnion opUnion, T left, T right) { return null; }
    @Override public T eval(OpLateral opLater, T left, T right) { return null; }
    @Override public T eval(OpConditional opCondition, T left, T right) { return null; }
    @Override public T eval(OpSequence opSequence, List<T> elts) { return null; }
    @Override public T eval(OpDisjunction opDisjunction, List<T> elts) { return null; }
    @Override public T eval(OpExt opExt) { return null; }
    @Override public T eval(OpList opList, T subOp) { return null; }
    @Override public T eval(OpOrder opOrder, T subOp) { return null; }
    @Override public T eval(OpTopN opTop, T subOp) { return null; }
    @Override public T eval(OpProject opProject, T subOp) { return null; }
    @Override public T eval(OpDistinct opDistinct, T subOp) { return null; }
    @Override public T eval(OpReduced opReduced, T subOp) { return null; }
    @Override public T eval(OpSlice opSlice, T subOp) { return null; }
    @Override public T eval(OpGroup opGroup, T subOp) { return null; }
}
