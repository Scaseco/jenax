package org.aksw.jenax.sparql.algebra.transform2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
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
import org.apache.jena.sparql.algebra.op.OpUnfold;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.walker.ApplyTransformVisitor;
import org.apache.jena.sparql.algebra.walker.Walker;
import org.apache.jena.sparql.algebra.walker.WalkerVisitor;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprFunction0;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprFunction3;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprNone;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformBase;
import org.apache.jena.sparql.expr.ExprTripleTerm;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Modeled after {@link ApplyTransformVisitor}.
 * Apply the {@link Evaluation}. Works in conjunction with {@link WalkerVisitor}.
 */
public class ApplyEvaluationVisitor<T> implements OpVisitor, ExprVisitor {
    protected final Evaluation<T>     evaluator;
    protected final Deque<T>          opStack   = new ArrayDeque<>() ;
    protected final Deque<Expr>       exprStack = new ArrayDeque<>() ;

    protected final ExprTransform     exprTransform =   new ExprTransformBase();

    public ApplyEvaluationVisitor(Evaluation<T> evaluator) {
        this.evaluator = evaluator;
    }

    public final T opResult() {
        return pop(opStack) ;
    }

//    private void dump(String label) {
//        System.out.println(label) ;
//        String x = opStack.toString().replace('\n', ' ').replaceAll("  +", " ") ;
//        System.out.println("    O:"+x);
//    }

    protected void push(Deque<T> stack, T value) {
        if ( value == null )
            Log.warn(ApplyEvaluationVisitor.class, "Pushing null onto the "+stackLabel(stack)+" stack") ;
        stack.push(value) ;
    }

    private <X> X pop(Deque<X> stack) {
        try {
            X v = stack.pop() ;
            if ( v ==  null )
                Log.warn(ApplyEvaluationVisitor.class, "Pop null from the "+stackLabel(stack)+" stack") ;
            return v ;
        }
        catch (NoSuchElementException ex) {
            // if ( true )
            throw new RuntimeException(ex) ;
            // Log.warn(ApplyEvaluationVisitor.class, "Empty "+stackLabel(stack)+" stack") ;
            // return null ;
        }
    }

    public T pop(Deque<T> stack, Op op) {
        T result = op != null
                ? pop(stack)
                : null;
        return result;
    }

    public List<T> pop(Deque<T> stack, List<Op> ops) {
        int n = ops.size();
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) Arrays.asList(new Object[n]); // new ArrayList<>(n);
        ListIterator<Op> it = ops.listIterator(n);
        int i = n;
        while (it.hasPrevious()) {
            --i;
            Op op = it.previous();
            T value = pop(stack, op);
            result.set(i, value);
        }
        return result;
    }

    private String stackLabel(Deque<?> stack) {
        return stack == opStack
            ? "Op"
            : stack == exprStack
                ? "Expr"
                : "<other>";
    }

    @Override
    public void visit(OpBGP op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpQuadPattern op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpQuadBlock op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpTriple op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpQuad op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpPath op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpTable op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpNull op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpProcedure op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpPropFunc op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpGraph op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpDatasetNames op) {
        T value = evaluator.eval(op);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpLabel op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpJoin op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpUnion op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpDiff op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpMinus op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpLateral op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpConditional op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpSequence op) {
        List<T> values = pop(opStack, op.getElements());
        T value = evaluator.eval(op, values) ;
        push(opStack, value) ;
    }

    @Override
    public void visit(OpDisjunction op) {
        List<T> values = pop(opStack, op.getElements());
        T value = evaluator.eval(op, values) ;
        push(opStack, value) ;
    }

    @Override
    public void visit(OpList op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpProject op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpReduced op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpDistinct op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpSlice op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpTopN op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpFilter op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpService op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value);
    }

    @Override
    public void visit(OpAssign op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpExtend op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpLeftJoin op) {
        T right = pop(opStack, op.getRight());
        T left = pop(opStack, op.getLeft());
        T value = evaluator.eval(op, left, right);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpOrder op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpGroup op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    @Override
    public void visit(OpUnfold op) {
        T sub = pop(opStack, op.getSubOp());
        T value = evaluator.eval(op, sub);
        push(opStack, value) ;
    }

    /*package*/ final Expr exprResult() {
        return popExpr(exprStack) ;
    }


    private <X> void pushExpr(Deque<X> stack, X value) {
        if ( value == null )
            Log.warn(ApplyTransformVisitor.class, "Pushing null onto the "+stackLabel(stack)+" stack") ;
        stack.push(value) ;
    }

    private <T> T popExpr(Deque<T> stack) {
        try {
            T v = stack.pop() ;
            if ( v ==  null )
                Log.warn(ApplyTransformVisitor.class, "Pop null from the "+stackLabel(stack)+" stack") ;
            return v ;
        }
        catch (NoSuchElementException ex) {
            if ( true ) throw new RuntimeException() ;
            Log.warn(ApplyTransformVisitor.class, "Empty "+stackLabel(stack)+" stack") ;
            return null ;
        }
    }

    // Special test cases for collectors.

    // Careful about order.
    private VarExprList collect(VarExprList varExprList) {
        if ( varExprList == null )
            return varExprList ;
      List<Var> vars = varExprList.getVars() ;
      VarExprList varExpr2 = new VarExprList() ;

      List<Expr> x = collect(vars.size()) ;

      boolean changed = false ;
      for ( int i = 0 ; i < vars.size() ; i++ ) {
          Var v = vars.get(i) ;
          Expr e2 = x.get(i) ;
          Expr e = varExpr2.getExpr(v) ;
          if ( e != e2 )
              changed = true ;
          if ( e2 == null )
              varExpr2.add(v) ;
          else {
              varExpr2.add(v, e2) ;
          }
      }
      return changed ? varExpr2 : varExprList ;
    }

    private ExprList collect(ExprList exprList) {
        if ( exprList == null )
            return null ;
        List<Expr> x = collect(exprList.size()) ;
        boolean changed = false ;
        for ( int i = 0 ; i < x.size() ; i++ ) {
            if ( x.get(i) != exprList.get(i) ) {
                changed = true ;
                break ;
            }
        }
        if ( ! changed )
            return exprList ;
        return new ExprList(x) ;
    }

    private ExprList collect(List<Expr> exprList) {
        if ( exprList == null )
            return null ;
        return new ExprList(collect(exprList.size())) ;
    }

    // collect and return in the original order (take account of stack reversal).
    private List<Expr> collect(int N) {
        // Check for "same"/unchanged
        List<Expr> x = new ArrayList<>(N) ;
        for ( int i = N-1 ; i >= 0 ; i-- ) {
            Expr e2 = popExpr(exprStack) ;
            if ( e2 == Expr.NONE )
                e2 = null ;
            x.add(0, e2) ;
        }
        return x ;
    }

    // These three could be calls within WalkerVisitor followed by "collect".
    protected Expr transform(Expr expr) {
        int x1 = opStack.size() ;
        int x2 = exprStack.size() ;
        try {
            OpVisitor beforeVisitor = null;
            OpVisitor afterVisitor = null;
            // FIXME
            // Expr result = Walker.transform(expr, new ExprTransformBase(), beforeVisitor, afterVisitor) ;
            Expr result = Walker.transform(expr, new ExprTransformBase()) ;
            return expr;
        } finally {
            int y1 = opStack.size() ;
            int y2 = exprStack.size() ;
            if ( x1 != y1 )
                Log.error(ApplyTransformVisitor.class, "Misaligned opStack") ;
            if ( x2 != y2 )
                Log.error(ApplyTransformVisitor.class, "Misaligned exprStack") ;
        }
    }

    protected ExprList transform(ExprList exprList) {
//        if ( exprList == null || exprTransform == null )
//            return exprList ;
        ExprList exprList2 = new ExprList() ;
        exprList.forEach( e->exprList2.add(transform(e)) );
        return exprList2 ;
    }

    protected List<SortCondition> transform(List<SortCondition> conditions) {
        List<SortCondition> conditions2 = new ArrayList<>() ;
        boolean changed = false ;

        for ( SortCondition sc : conditions ) {
            Expr e = sc.getExpression() ;
            Expr e2 = transform(e) ;
            conditions2.add(new SortCondition(e2, sc.getDirection())) ;
            if ( e != e2 )
                changed = true ;
        }
        if ( changed )
            return conditions2 ;
        else
            return conditions ;
    }

//    @Override
//    public void visitExpr(ExprList exprs) {
//        throw new InternalErrorException("Didn't expect as call to ApplyTransformVisit.visitExpr") ;
//    }
//
//    @Override
//    public void visitVarExpr(VarExprList exprVarExprList)  {
//        throw new InternalErrorException("Didn't expect as call to ApplyTransformVisit.visitVarExpr") ;
//    }

    @Override
    public void visit(ExprFunction0 func) {
        Expr e = func.apply(exprTransform) ;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunction1 func) {
        Expr e1 = pop(exprStack) ;
        Expr e = func.apply(exprTransform, e1) ;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunction2 func) {
        Expr e2 = pop(exprStack) ;
        Expr e1 = pop(exprStack) ;
        Expr e = func.apply(exprTransform, e1, e2) ;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunction3 func) {
        Expr e3 = pop(exprStack) ;
        Expr e2 = pop(exprStack) ;
        Expr e1 = pop(exprStack) ;
        Expr e = func.apply(exprTransform, e1, e2, e3) ;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunctionN func) {
        ExprList x = collect(func.getArgs()) ;
        Expr e = func.apply(exprTransform, x) ;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunctionOp funcOp) {
        ExprList x = null ;
        if ( funcOp.getArgs() != null )
            x = collect(funcOp.getArgs()) ;
        T value = pop(opStack) ;
        // FIXME We discard the computed result here for now.
        // Op op = valueToOp(value);
        // Expr e = funcOp.apply(exprTransform, x, op) ;
        Expr e = funcOp.apply(exprTransform, x, funcOp.getGraphPattern()) ;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(ExprTripleTerm tripleTerm) {
        //Expr e = tripleTerm.apply(exprTransform) ;
        Expr e = tripleTerm;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(NodeValue nv) {
        Expr e = nv.apply(exprTransform) ;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(ExprVar var) {
        Expr e = var.apply(exprTransform) ;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(ExprAggregator eAgg) {
        Expr e = eAgg.apply(exprTransform) ;
        pushExpr(exprStack, e) ;
    }

    @Override
    public void visit(ExprNone e) {
        pushExpr(exprStack, e) ;
    }
}
