package org.aksw.jenax.sparql.algebra.transform2;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
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
import org.apache.jena.sparql.algebra.op.OpN;
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
import org.apache.jena.sparql.algebra.op.OpUnfold;
import org.apache.jena.sparql.algebra.op.OpUnion;

 public interface EvaluationCopy<T>
     extends Evaluation<T>
 {
     @Override default T eval(OpTable op) { return evalOp0(op); }
     @Override default T eval(OpBGP op) { return evalOp0(op); }
     @Override default T eval(OpTriple op) { return evalOp0(op); }
     @Override default T eval(OpQuad op) { return evalOp0(op); }
     @Override default T eval(OpPath op) { return evalOp0(op); }
     @Override default T eval(OpDatasetNames op) { return evalOp0(op); }
     @Override default T eval(OpQuadPattern op) { return evalOp0(op); }
     @Override default T eval(OpQuadBlock op) { return evalOp0(op); }
     @Override default T eval(OpNull op) { return evalOp0(op); }
     @Override default T eval(OpFilter op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpGraph op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpService op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpProcedure op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpPropFunc op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpLabel op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpAssign op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpExtend op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpJoin op, T left, T right) { return evalOp2(op, left, right); }
     @Override default T eval(OpLeftJoin op, T left, T right) { return evalOp2(op, left, right); }
     @Override default T eval(OpDiff op, T left, T right) { return evalOp2(op, left, right); }
     @Override default T eval(OpMinus op, T left, T right) { return evalOp2(op, left, right); }
     @Override default T eval(OpUnion op, T left, T right) { return evalOp2(op, left, right); }
     @Override default T eval(OpLateral op, T left, T right) { return evalOp2(op, left, right); }
     @Override default T eval(OpConditional op, T left, T right) { return evalOp2(op, left, right); }
     @Override default T eval(OpSequence op, List<T> elts) { return evalOpN(op, elts); }
     @Override default T eval(OpDisjunction op, List<T> elts) { return evalOpN(op, elts); }
     @Override default T eval(OpExt op) { return evalAny(op, Arrays.asList()); }
     @Override default T eval(OpList op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpOrder op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpTopN op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpProject op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpDistinct op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpReduced op, T subOp){ return evalOp1(op, subOp); }
     @Override default T eval(OpSlice op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpGroup op, T subOp) { return evalOp1(op, subOp); }
     @Override default T eval(OpUnfold op, T subOp) { return evalOp1(op, subOp); }

     default T evalOp0(Op0 op) {
         T result = evalAny(op, Arrays.asList());
         return result;
     }

     default T evalOp1(Op1 op, T arg) {
         T result = evalAny(op, Arrays.asList(arg));
         return result;
     }

     default T evalOp2(Op2 op, T arg1, T arg2) {
         T result = evalAny(op, Arrays.asList(arg1, arg2));
         return result;
     }

     default T evalOpN(OpN op, List<T> args) {
         T result = evalAny(op, args);
         return result;
     }

     T evalAny(Op op, List<T> args);
}
