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
import org.apache.jena.sparql.engine.main.OpExecutor;

/**
 * A generalization of the interface of {@link OpExecutor} where the result type is not restricted to QueryIter.
 */
 interface Evaluator<T> {
     T evalOp(Op op, T input);

     T eval(OpTable opUnit, T input);
     T eval(OpBGP opBGP, T input);
     T eval(OpTriple opTriple, T input);
     T eval(OpQuad opQuad, T input);
     T eval(OpPath opPath, T input);
     T eval(OpDatasetNames dsNames, T input);
     T eval(OpQuadPattern quadPattern, T input);
     T eval(OpQuadBlock quadBlock, T input);
     T eval(OpNull opNull, T input);
     T eval(OpFilter opFilter, T input);
     T eval(OpGraph opGraph, T input);
     T eval(OpService opService, T input);
     T eval(OpProcedure opProcedure, T input);
     T eval(OpPropFunc opPropFunc, T input);
     T eval(OpLabel opLabel, T input);
     T eval(OpAssign opAssign, T input);
     T eval(OpExtend opExtend, T input);
     T eval(OpJoin opJoin, T input);
     T eval(OpLeftJoin opLeftJoin, T input);
     T eval(OpDiff opDiff, T input);
     T eval(OpMinus opMinus, T input);
     T eval(OpUnion opUnion, T input);
     T eval(OpLateral opLater, T input);
     T eval(OpConditional opCondition, T input);
     T eval(OpSequence opSequence, T input);
     T eval(OpDisjunction opDisjunction, T input);
     T eval(OpExt opExt, T input);
     T eval(OpList opList, T input);
     T eval(OpOrder opOrder, T input);
     T eval(OpTopN opTop, T input);
     T eval(OpProject opProject, T input);
     T eval(OpDistinct opDistinct, T input);
     T eval(OpReduced opReduced, T input);
     T eval(OpSlice opSlice, T input);
     T eval(OpGroup opGroup, T input);
}
