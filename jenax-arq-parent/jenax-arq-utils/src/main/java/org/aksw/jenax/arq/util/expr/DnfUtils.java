package org.aksw.jenax.arq.util.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Coalesce;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

public class DnfUtils {

    /**
     * Similar to OpVars.fixedVars - but analyzes a boolean expression.
     * Returns the set of variables that must be bound for the expression to be satisfiable.
     * Internally converts the expression to a CNF and checks for which variables are required in all clauses.
     * A variable is not required if it is used in
     * - !bound(?x)
     * - coalesce(?x)
     * - does not appear in a clause at all.
     */
    public static Set<Var> fixedVars(Expr expr) {
        Set<Set<Expr>> clauses = DnfUtils.toSetDnf(expr);
        Set<Var> fixedVars = null;
        for (Set<Expr> clause : clauses) {
            Set<Var> fixedVarsOfClause = fixedVarsClause(clause);
            if (fixedVars == null) {
                fixedVars = fixedVarsOfClause;
            } else {
                fixedVars.retainAll(fixedVarsOfClause);
            }
        }
        if (fixedVars == null) {
            fixedVars = Collections.emptySet();
        }
        return fixedVars;
    }

    /**
     * Return a set of fixed variables in the given clause.
     * Best effort approach: If any expression in the clause contains !bound or coalesce then all mentioned variables
     * are considered non-fixed.
     * Any variable x appearing in coalesce(x) or !bound(x) is <b>not</b> considered fixed.
     * Assumes the input is a clause of a DNF.
     */
    public static Set<Var> fixedVarsClause(Iterable<? extends Expr> clause) {
        Set<Var> fixedVars = new LinkedHashSet<>();
        Set<Var> nonFixedVars = new LinkedHashSet<>();
        for (Expr expr : clause) {
            Set<Var> varsMentioned = expr.getVarsMentioned();
            boolean containsCoalesceOrNotBound =
                Streams.stream(Traverser.forTree(ExprUtils::getSubExprs).depthFirstPreOrder(expr))
                    .anyMatch(x -> x instanceof E_Coalesce || ExprUtils.getIsNotBoundArg(x) != null);

            if (containsCoalesceOrNotBound) {
                nonFixedVars.addAll(varsMentioned);
            } else {
                fixedVars.addAll(varsMentioned);
            }
        }
        fixedVars.removeAll(nonFixedVars);
        return fixedVars;
    }


    public static Expr toExpr(Iterable<? extends Iterable<Expr>> ors) {
        List<Expr> tmpOrs = new ArrayList<Expr>();
        for(Iterable<Expr> ands : ors) {
            Expr and = ExprUtils.andifyBalanced(ands);
            if(and == null) {
                and = NodeValue.TRUE;
            }

            tmpOrs.add(and);
        }

        if(Iterables.isEmpty(tmpOrs)) {
            return NodeValue.FALSE;
        }

        Expr result = ExprUtils.orifyBalanced(tmpOrs);

        return result;
    }

    public static void addConstantConstraint(Map<Var, NodeValue> map, Entry<Var, NodeValue> constraint) {
        if(constraint == null) {
            return;
        }

        addConstantConstraint(map, constraint.getKey(), constraint.getValue());
    }

    public static void addConstantConstraint(Map<Var, NodeValue> map, Var var, NodeValue nodeValue)
    {
        if(map.containsKey(var)) {
            NodeValue oldValue = map.get(var);
            if(oldValue != null && !oldValue.equals(nodeValue)) {
                map.put(var, null);
            }
        }
        else {
            map.put(var, nodeValue);
        }
    }

    public static Set<Map<Var, NodeValue>> extractConstantConstraints(Collection<? extends Collection<? extends Expr>> dnf) {
        dnf = dnf == null ? Collections.emptySet() : dnf;
        Set<Map<Var, NodeValue>> result = new HashSet<>(dnf.size());
        for(Collection<? extends Expr> clause : dnf) {
            Map<Var, NodeValue> map = ClauseUtils.extractConstantConstraints(clause);
            result.add(map);
        }
        return result;
    }

    /**
     * For each clause determine the constant constraints, and return those,
     * that are common to all clauses.
     *
     * @param dnf
     */
    public static Map<Var, NodeValue> extractCommonConstantConstraints(Set<Set<Expr>> dnf) {
        Map<Var, NodeValue> result = new HashMap<Var, NodeValue>();

        Iterator<Set<Expr>> clauseIt = dnf.iterator();
        if(!clauseIt.hasNext()) {
            return result;
        }

        Set<Expr> firstClause = clauseIt.next();
        for(Expr expr : firstClause) {
            Entry<Var, NodeValue> constraint = ExprUtils.extractConstantConstraint(expr);

            addConstantConstraint(result,  constraint);
        }

        Set<Var> seenVars = new HashSet<Var>();
        while(clauseIt.hasNext()) {

            if(result.isEmpty()) {
                return result;
            }

            Set<Expr> clause = clauseIt.next();
            for(Expr expr : clause) {
                Entry<Var, NodeValue> constraint = ExprUtils.extractConstantConstraint(expr);
                if(constraint == null || !result.containsKey(constraint.getKey())) {
                    continue;
                }

                addConstantConstraint(result,  constraint);
                seenVars.add(constraint.getKey());
            }

            result.keySet().retainAll(seenVars);
            seenVars.clear();
        }

        return result;
    }

    public static Set<Set<Expr>> toSetDnf(Expr expr) {
        Set<Set<Expr>> result = toSetDnf(expr, false);
        return result;
    }

    public static Set<Set<Expr>> toSetDnf(Expr expr, boolean allowEmptyClauseInsteadOfNull)
    {
        List<ExprList> clauses = DnfUtils.toClauses(expr);
        Set<Set<Expr>> dnf = FilterUtils.toSets(clauses);

        if(dnf == null && allowEmptyClauseInsteadOfNull) {
            dnf = Collections.singleton(Collections.emptySet());
        }

        return dnf;
    }

    public static Set<Set<Expr>> toSetDnf(ExprList exprs)
    {
        List<ExprList> clauses = DnfUtils.toClauses(exprs);
        Set<Set<Expr>> dnf = FilterUtils.toSets(clauses);

        return dnf;
    }


    public static List<List<Expr>> toListDnf(ExprList exprs)
    {
        List<List<Expr>> result = DnfUtils.toClauses(exprs).stream()
                .map(el -> new ArrayList<>(el.getList())) // Ensure mutable lists for the clauses
                .collect(Collectors.toCollection(ArrayList::new));

        return result;
    }

    public static boolean isSatisfiable(Set<Set<Expr>> dnf) {
        for(Set<Expr> clause : dnf) {
            if(ClauseUtils.isSatisfiable(clause)) {
                return true;
            }
        }

        return false;
    }


    // FIXME This method removes redundant TRUEs from clauses
    public static Expr dnfToExpr(Set<Set<Expr>> dnf, boolean skipUnsatisfiable) {
        Set<Expr> exprs = new HashSet<Expr>();

        for(Set<Expr> clause : dnf) {

            if(clause.size() > 1 && clause.contains(NodeValue.TRUE)) {
                clause.remove(NodeValue.TRUE);
            }

            if(skipUnsatisfiable) {
                if(!ClauseUtils.isSatisfiable(clause)) {
                    continue;
                }
            }

            exprs.add(ExprUtils.andifyBalanced(clause));
        }

        Expr result = ExprUtils.orifyBalanced(exprs);
        return result != null ? result : NodeValue.FALSE;
    }


    /**
     * Concatenates the sub exressions using Logical_And
     *
     * and(and(and(0, 1), 2, 3)
     *
     * @param exprs
     * @return
     */
    public static Expr andifyLeftSided(ExprList exprs) {
        Expr result = null;

        for(Expr expr : exprs.getList()) {
            result = (result == null) ? expr : new E_LogicalAnd(result, expr);
        }

        return result;
    }



    public static List<ExprList> toClauses(Expr expr)
    {
        Expr evaluated = eval(expr);
        return evaluated == null ? null : dnfToClauses(Collections.singleton(evaluated));
    }

    public static List<ExprList> toClauses(ExprList exprs)
    {
        Expr evaluated = eval(ExprUtils.andifyBalanced(exprs));
        return evaluated == null ? null : dnfToClauses(Collections.singleton(evaluated));
    }


    /**
     * This method only words if the input expressions are in DNF,
     * otherwise you will likely get junk back.
     *
     * @param exprs
     * @return
     */
    public static List<ExprList> dnfToClauses(Iterable<Expr> exprs) {
        List<ExprList> result = new ArrayList<ExprList>();

        for(Expr expr : exprs) {
            collectOr(expr, result);
        }

        return result;
    }

    public static void collectAnd(Expr expr, ExprList list)
    {
        if(expr instanceof E_LogicalAnd) {
            E_LogicalAnd e = (E_LogicalAnd)expr;

            collectAnd(e.getArg1(), list);
            collectAnd(e.getArg2(), list);
        } else {
            list.add(expr);
        }
    }


    public static void collectOr(Expr expr, List<ExprList> list)
    {
        if(expr instanceof E_LogicalOr) {
            E_LogicalOr e = (E_LogicalOr)expr;

            collectOr(e.getArg1(), list);
            collectOr(e.getArg2(), list);
        }
        else if(expr instanceof E_LogicalAnd) {
            //List<Expr> ors = new ArrayList<Expr>();
            ExprList ors = new ExprList();
            collectAnd(expr, ors);

            list.add(ors);
        } else {
            ExprList tmp = new ExprList();
            // Turn true into empty clause
            if(!NodeValue.TRUE.equals(expr)) {
                tmp.add(expr);
            }

            list.add(tmp);
        }
    }


    public static ExprList eval(ExprList exprs) {
        //System.out.println("ExprList.size = " + exprs.size());

        ExprList result = new ExprList();
        for(Expr expr : exprs) {
            result.add(eval(expr));
        }

        return result;
    }

    public static Expr eval(Expr expr)
    {
        if(expr instanceof ExprFunction) {
            return handle((ExprFunction)expr);
        } else {
            return expr;
        }
    }


    public static boolean containsDirectFuncChild(Expr expr, Class<?> clazz)
    {
        if(!(expr instanceof ExprFunction)) {
            return false;
        }

        ExprFunction func = (ExprFunction)expr;

        for(Expr arg : func.getArgs()) {
            if(arg == null) {
                continue;
            }

            if(clazz.isAssignableFrom(arg.getClass())) {
                return true;
            }
        }

        return false;
    }
        //return new FuncExpr("and", children);




    public static Expr handle(ExprFunction expr)
    {
        //System.out.println("Converting to DNF: [" + expr.getClass() + "]: " + expr);

        // not(and(A, B)) -> or(not A, not B)
        // not(or(A, B)) -> or(not A, not B)


        if(expr instanceof E_LogicalNot) {

            Expr tmp = ((E_LogicalNot)expr).getArg();
            if (!(tmp instanceof ExprFunction)) {
                return expr;
            }

            ExprFunction child = (ExprFunction)tmp;

            Expr newExpr = expr;

            if (child instanceof E_LogicalAnd) {
                newExpr = new E_LogicalOr(eval(new E_LogicalNot(child.getArg(1))), eval(new E_LogicalNot(child.getArg(2))));
            }
            else if (child instanceof E_LogicalOr) {
                newExpr = new E_LogicalAnd(eval(new E_LogicalNot(child.getArg(1))), eval(new E_LogicalNot(child.getArg(2))));
            }
            else if (child instanceof E_LogicalNot) { // Remove double negation
                newExpr = eval(child.getArg(1));
            }
            else {
                return expr;
            }

            return eval(newExpr);
        }


        else if (expr instanceof E_LogicalOr) {
            //return expr;
            //return eval(expr);
            return new E_LogicalOr(eval(expr.getArg(1)), eval(expr.getArg(2)));
        }


        /* Given:
         * (A or B) AND (C x D) becomes:
         * (A and (C x D)) OR (B and (c x D))
         *
         *
         * (A or B) AND (C or D)
         *
         * Goal:
         * (A and C) OR (A and D) OR (B and C) OR (B and D)
         *
         * This method transforms any "or" children of an AND node.
         * other nodes are left untouched:
         * (A or B) AND (c x D) becomes:
         * (A and (c x D)) OR (B and (c x D))
         */
        else if (expr instanceof E_LogicalAnd) {

            Expr aa = eval(expr.getArg(1));
            Expr bb = eval(expr.getArg(2));

            E_LogicalOr a = null;
            Expr b = null;

            if (aa instanceof E_LogicalOr) {
                a = (E_LogicalOr)aa;
                b = bb;
            }
            else if(bb instanceof E_LogicalOr) {
                a = (E_LogicalOr)bb;
                b = aa;
            }

            if(a == null) {
                return new E_LogicalAnd(aa, bb);
            } else {
                return new E_LogicalOr(eval(new E_LogicalAnd(a.getArg(1), b)), eval(new E_LogicalAnd(a.getArg(2), b)));
            }
        }

        else if (expr instanceof E_NotEquals) { // Normalize (a != b) to !(a = b) --- this makes it easier to find "a and !a" cases
            return new E_LogicalNot(eval(new E_Equals(expr.getArg(1), expr.getArg(2))));
        }


        return expr;


        /*
        if(expr instanceof E_LogicalNot) {

            Expr tmp = ((E_LogicalNot)expr).getArg();
            if (!(tmp instanceof ExprFunction))
                return expr;

            ExprFunction child = (ExprFunction) tmp;

            String newFuncName = "";

            if (child.getName().equals("and"))
                newFuncName = "or";
            else if (child.getName().equals("or"))
                newFuncName = "and";
            else if (child.getName().equals("not")) // Remove double negation
                return eval(child.getChildren().get(0));
            else
                return expr;

            FuncExpr result = new FuncExpr(newFuncName, child.getArity());
            for (int i = 0; i < child.getArity(); ++i)
                result.setChild(i,
                        new FuncExpr("not", child.getChildren().get(i)));

            return eval(result);
        }

        if (expr.getName().equals("or")) {
            List<IExpr> children = new ArrayList<IExpr>();
            for (IExpr child : expr.getChildren())
                children.add(eval(child));

            if (!ExprUtil.containsDirectFuncChild(children, "or"))
                return new FuncExpr("or", children);

            // flatten or expressions
            // or(or(A, B), C) becomes or(A, B, C)
            List<IExpr> resultChildren = new ArrayList<IExpr>();
            for (IExpr child : children)
                if (ExprUtil.isFunc(child, "or"))
                    resultChildren.addAll(child.getChildren());
                else
                    resultChildren.add(child);

            return new FuncExpr("or", resultChildren);
        }

        if (expr.getName().equals("and")) {
            List<IExpr> children = new ArrayList<IExpr>();
            for (IExpr child : expr.getChildren())
                children.add(eval(child));

            // FIXME an and-node must have at least 2 children
            // but maybe validation should be done somewhere else
            // On the other hand it might be convenient to assume that
            // whenever a binary expression only contains a single child
            // it should be treated as if there was no operation at all.

            // No 'or'-expression => nothing todo
            if (!ExprUtil.containsDirectFuncChild(children, "or"))
                return new FuncExpr("and", children);

            // Collect all expressions
            List<List<IExpr>> tables = new ArrayList<List<IExpr>>();
            for (IExpr child : children) {

                if (ExprUtil.isFunc(child, "or"))
                    tables.add(child.getChildren());
                else
                    tables.add(Collections.singletonList(child));

            }

            Collection<List<IExpr>> joinedTable = new JoinIterable<IExpr>(
                    tables);
            FuncExpr result = new FuncExpr("or", joinedTable.size());

            // List<IExpr> resultChildren = new ArrayList<IExpr>();

            int i = 0;
            for (List<IExpr> row : joinedTable) {
                IExpr newChild = new FuncExpr("and", row);

                result.setChild(i, newChild);

                ++i;
            }

            return result;
        }

        // TODO Auto-generated method stub
        return expr;
        */



    }
}
