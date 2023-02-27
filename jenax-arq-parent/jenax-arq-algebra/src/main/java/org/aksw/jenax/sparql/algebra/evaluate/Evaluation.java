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
import org.apache.jena.sparql.engine.main.OpExecutor;

/**
 * A general interface for applyuating Ops to a result type T.
 *
 * This class can be seen as a generalization of e.g. {@link apply} and {@link OpExecutor}:
 * The former evaluates Ops into other Ops, whereas the latter applyuates them to QueryIters.
 */
 interface Evaluation<T> {
     T apply(OpTable opUnit);
     T apply(OpBGP opBGP);
     T apply(OpTriple opTriple);
     T apply(OpQuad opQuad);
     T apply(OpPath opPath);
     T apply(OpDatasetNames dsNames);
     T apply(OpQuadPattern quadPattern);
     T apply(OpQuadBlock quadBlock);
     T apply(OpNull opNull);
     T apply(OpFilter opFilter, T subOp);
     T apply(OpGraph opGraph, T subOp);
     T apply(OpService opService, T subOp);
     T apply(OpProcedure opProcedure, T subOp);
     T apply(OpPropFunc opPropFunc, T subOp);
     T apply(OpLabel opLabel, T subOp);
     T apply(OpAssign opAssign, T subOp);
     T apply(OpExtend opExtend, T subOp);
     T apply(OpJoin opJoin, T left, T right);
     T apply(OpLeftJoin opLeftJoin, T left, T right);
     T apply(OpDiff opDiff, T left, T right);
     T apply(OpMinus opMinus, T left, T right);
     T apply(OpUnion opUnion, T left, T right);
     T apply(OpLateral opLater, T left, T right);
     T apply(OpConditional opCondition, T left, T right);
     T apply(OpSequence opSequence, List<T> elts);
     T apply(OpDisjunction opDisjunction, List<T> elts);
     T apply(OpExt opExt);
     T apply(OpList opList, T subOp);
     T apply(OpOrder opOrder, T subOp);
     T apply(OpTopN opTop, T subOp);
     T apply(OpProject opProject, T subOp);
     T apply(OpDistinct opDistinct, T subOp);
     T apply(OpReduced opReduced, T subOp);
     T apply(OpSlice opSlice, T subOp);
     T apply(OpGroup opGroup, T subOp);
}
