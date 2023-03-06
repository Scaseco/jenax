package org.aksw.jenax.sparql.algebra.transform2;

import java.util.List;

import org.apache.jena.sparql.algebra.Transform;
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

/**
 * A generalization of {@link Transform}
 */
 interface Evaluation<T> {
     T eval(OpTable op);
     T eval(OpBGP op);
     T eval(OpTriple op);
     T eval(OpQuad op);
     T eval(OpPath op);
     T eval(OpDatasetNames op);
     T eval(OpQuadPattern op);
     T eval(OpQuadBlock op);
     T eval(OpNull op);
     T eval(OpFilter op, T subOp);
     T eval(OpGraph op, T subOp);
     T eval(OpService op, T subOp);
     T eval(OpProcedure op, T subOp);
     T eval(OpPropFunc op, T subOp);
     T eval(OpLabel op, T subOp);
     T eval(OpAssign op, T subOp);
     T eval(OpExtend op, T subOp);
     T eval(OpJoin op, T left, T right);
     T eval(OpLeftJoin op, T left, T right);
     T eval(OpDiff op, T left, T right);
     T eval(OpMinus op, T left, T right);
     T eval(OpUnion op, T left, T right);
     T eval(OpLateral op, T left, T right);
     T eval(OpConditional op, T left, T right);
     T eval(OpSequence op, List<T> elts);
     T eval(OpDisjunction op, List<T> elts);
     T eval(OpExt op);
     T eval(OpList op, T subOp);
     T eval(OpOrder op, T subOp);
     T eval(OpTopN op, T subOp);
     T eval(OpProject op, T subOp);
     T eval(OpDistinct op, T subOp);
     T eval(OpReduced op, T subOp);
     T eval(OpSlice op, T subOp);
     T eval(OpGroup op, T subOp);
}
