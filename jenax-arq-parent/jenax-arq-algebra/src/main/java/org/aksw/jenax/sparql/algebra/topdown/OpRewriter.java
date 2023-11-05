package org.aksw.jenax.sparql.algebra.topdown;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
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

public interface OpRewriter<T> {
    default T fallback(Op op)                  { return null; }

    default T rewriteOp(Op op) {
        OpVisitorBridge<T> visitor = new OpVisitorBridge<>(this);
        op.visit(visitor);
        return visitor.getResult();
    }

    default T rewrite(OpTable op)              { return fallback(op); }
    default T rewrite(OpBGP op)                { return fallback(op); }
    default T rewrite(OpTriple op)             { return fallback(op); }
    default T rewrite(OpQuad op)               { return fallback(op); }
    default T rewrite(OpPath op)               { return fallback(op); }
    default T rewrite(OpDatasetNames op)       { return fallback(op); }
    default T rewrite(OpQuadPattern op)        { return fallback(op); }
    default T rewrite(OpQuadBlock op)          { return fallback(op); }
    default T rewrite(OpNull op)               { return fallback(op); }
    default T rewrite(OpFilter op)             { return fallback(op); }
    default T rewrite(OpGraph op)              { return fallback(op); }
    default T rewrite(OpService op)            { return fallback(op); }
    default T rewrite(OpProcedure op)          { return fallback(op); }
    default T rewrite(OpPropFunc op)           { return fallback(op); }
    default T rewrite(OpLabel op)              { return fallback(op); }
    default T rewrite(OpAssign op)             { return fallback(op); }
    default T rewrite(OpExtend op)             { return fallback(op); }
    default T rewrite(OpJoin op)               { return fallback(op); }
    default T rewrite(OpLeftJoin op)           { return fallback(op); }
    default T rewrite(OpDiff op)               { return fallback(op); }
    default T rewrite(OpMinus op)              { return fallback(op); }
    default T rewrite(OpUnion op)              { return fallback(op); }
    default T rewrite(OpLateral op)            { return fallback(op); }
    default T rewrite(OpConditional op)        { return fallback(op); }
    default T rewrite(OpSequence op)           { return fallback(op); }
    default T rewrite(OpDisjunction op)        { return fallback(op); }
    default T rewrite(OpExt op)                { return fallback(op); }
    default T rewrite(OpList op)               { return fallback(op); }
    default T rewrite(OpOrder op)              { return fallback(op); }
    default T rewrite(OpTopN op)               { return fallback(op); }
    default T rewrite(OpProject op)            { return fallback(op); }
    default T rewrite(OpDistinct op)           { return fallback(op); }
    default T rewrite(OpReduced op)            { return fallback(op); }
    default T rewrite(OpSlice op)              { return fallback(op); }
    default T rewrite(OpGroup op)              { return fallback(op); }


    public static class OpVisitorBridge<T>
        implements OpVisitor
    {
        protected OpRewriter<T> rewriter;
        protected T result;

        protected OpVisitorBridge(OpRewriter<T> rewriter) {
            super();
            this.rewriter = rewriter;
            this.result = null;
        }

        public static <T> OpVisitorBridge<T> of(OpRewriter<T> rewriter) {
            return new OpVisitorBridge<>(rewriter);
        }

        public T getResult() { return result; }

        @Override public void visit(OpTable op)                  { result = rewriter.rewrite(op); }
        @Override public void visit(OpBGP op)                    { result = rewriter.rewrite(op); }
        @Override public void visit(OpQuadPattern op)            { result = rewriter.rewrite(op); }
        @Override public void visit(OpQuadBlock op)              { result = rewriter.rewrite(op); }
        @Override public void visit(OpTriple op)                 { result = rewriter.rewrite(op); }
        @Override public void visit(OpQuad op)                   { result = rewriter.rewrite(op); }
        @Override public void visit(OpPath op)                   { result = rewriter.rewrite(op); }
        @Override public void visit(OpDatasetNames op)           { result = rewriter.rewrite(op); }
        @Override public void visit(OpNull op)                   { result = rewriter.rewrite(op); }
        @Override public void visit(OpFilter op)                 { result = rewriter.rewrite(op); }
        @Override public void visit(OpGraph op)                  { result = rewriter.rewrite(op); }
        @Override public void visit(OpService op)                { result = rewriter.rewrite(op); }
        @Override public void visit(OpProcedure op)              { result = rewriter.rewrite(op); }
        @Override public void visit(OpPropFunc op)               { result = rewriter.rewrite(op); }
        @Override public void visit(OpLabel op)                  { result = rewriter.rewrite(op); }
        @Override public void visit(OpAssign op)                 { result = rewriter.rewrite(op); }
        @Override public void visit(OpExtend op)                 { result = rewriter.rewrite(op); }
        @Override public void visit(OpJoin op)                   { result = rewriter.rewrite(op); }
        @Override public void visit(OpLeftJoin op)               { result = rewriter.rewrite(op); }
        @Override public void visit(OpDiff op)                   { result = rewriter.rewrite(op); }
        @Override public void visit(OpMinus op)                  { result = rewriter.rewrite(op); }
        @Override public void visit(OpUnion op)                  { result = rewriter.rewrite(op); }
        @Override public void visit(OpLateral op)                { result = rewriter.rewrite(op); }
        @Override public void visit(OpConditional op)            { result = rewriter.rewrite(op); }
        @Override public void visit(OpSequence op)               { result = rewriter.rewrite(op); }
        @Override public void visit(OpDisjunction op)            { result = rewriter.rewrite(op); }
        @Override public void visit(OpExt op)                    { result = rewriter.rewrite(op); }
        @Override public void visit(OpList op)                   { result = rewriter.rewrite(op); }
        @Override public void visit(OpOrder op)                  { result = rewriter.rewrite(op); }
        @Override public void visit(OpTopN op)                   { result = rewriter.rewrite(op); }
        @Override public void visit(OpProject op)                { result = rewriter.rewrite(op); }
        @Override public void visit(OpDistinct op)               { result = rewriter.rewrite(op); }
        @Override public void visit(OpReduced op)                { result = rewriter.rewrite(op); }
        @Override public void visit(OpSlice op)                  { result = rewriter.rewrite(op); }
        @Override public void visit(OpGroup op)                  { result = rewriter.rewrite(op); }
    }

}

//class OpRewriterBridge<T>
//implements OpRewriter<T>
//{
//protected OpVisitorBridge<T> bridge;
//
//@Override public T rewrite(OpTable op)              { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpBGP op)                { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpTriple op)             { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpQuad op)               { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpPath op)               { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpDatasetNames op)       { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpQuadPattern op)        { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpQuadBlock op)          { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpNull op)               { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpFilter op)             { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpGraph op)              { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpService op)            { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpProcedure op)          { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpPropFunc op)           { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpLabel op)              { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpAssign op)             { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpExtend op)             { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpJoin op)               { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpLeftJoin op)           { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpDiff op)               { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpMinus op)              { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpUnion op)              { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpLateral op)            { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpConditional op)        { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpSequence op)           { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpDisjunction op)        { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpExt op)                { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpList op)               { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpOrder op)              { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpTopN op)               { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpProject op)            { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpDistinct op)           { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpReduced op)            { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpSlice op)              { op.visit(bridge); return bridge.getResult(); }
//@Override public T rewrite(OpGroup op)              { op.visit(bridge); return bridge.getResult(); }
//}

