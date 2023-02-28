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
 * A general interface for evaluating Ops to a result type T.
 *
 * This class can be seen as a generalization of e.g. {@link eval} and {@link OpExecutor}:
 * The former evaluates Ops into other Ops, whereas the latter evaluates them to QueryIters.
 */
 interface Evaluation<T> {
     T eval(OpTable opUnit);
     T eval(OpBGP opBGP);
     T eval(OpTriple opTriple);
     T eval(OpQuad opQuad);
     T eval(OpPath opPath);
     T eval(OpDatasetNames dsNames);
     T eval(OpQuadPattern quadPattern);
     T eval(OpQuadBlock quadBlock);
     T eval(OpNull opNull);
     T eval(OpFilter opFilter, T subOp);
     T eval(OpGraph opGraph, T subOp);
     T eval(OpService opService, T subOp);
     T eval(OpProcedure opProcedure, T subOp);
     T eval(OpPropFunc opPropFunc, T subOp);
     T eval(OpLabel opLabel, T subOp);
     T eval(OpAssign opAssign, T subOp);
     T eval(OpExtend opExtend, T subOp);
     T eval(OpJoin opJoin, T left, T right);
     T eval(OpLeftJoin opLeftJoin, T left, T right);
     T eval(OpDiff opDiff, T left, T right);
     T eval(OpMinus opMinus, T left, T right);
     T eval(OpUnion opUnion, T left, T right);
     T eval(OpLateral opLater, T left, T right);
     T eval(OpConditional opCondition, T left, T right);
     T eval(OpSequence opSequence, List<T> elts);
     T eval(OpDisjunction opDisjunction, List<T> elts);
     T eval(OpExt opExt);
     T eval(OpList opList, T subOp);
     T eval(OpOrder opOrder, T subOp);
     T eval(OpTopN opTop, T subOp);
     T eval(OpProject opProject, T subOp);
     T eval(OpDistinct opDistinct, T subOp);
     T eval(OpReduced opReduced, T subOp);
     T eval(OpSlice opSlice, T subOp);
     T eval(OpGroup opGroup, T subOp);
}
