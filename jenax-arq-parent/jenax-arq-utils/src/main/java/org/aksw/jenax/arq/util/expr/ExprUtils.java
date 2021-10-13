package org.aksw.jenax.arq.util.expr;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.node.NodeTransformRenameMap;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.FunctionLabel;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformExpr;
import org.apache.jena.sparql.graph.NodeTransformLib;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 1/8/12
 *         Time: 6:18 PM
 */
public class ExprUtils {
    /**
     * A variable-aware version of NodeValue.makeNode(Node node)
     * Use ExprLib#nodeToExpr
     *
     * @param node
     * @return
     */
    // public static Expr makeNode(Node node) { }




    /**
     * Test whether a node and an expression represent the same value
     *
     * @param node
     * @param expr
     * @return
     */
    public static boolean isSame(Node node, Expr expr) {
        boolean result = node.isVariable()
                ? expr.isVariable() && node.equals(expr.asVar())
                : node.isConcrete()
                    ? expr.isConstant() && node.equals(expr.getConstant().asNode())
                    : false;

        return result;
    }

    /**
     * Node transform version that
     * (a) handles blank nodes correctly; in constrast to Expr.applyNodeTransform
     * [disabled (b) treats null mappings as identity mapping]
     *
     *
     *
     * @return
     */
    public static Expr applyNodeTransform(Expr expr, NodeTransform xform) {
        Expr result = ExprTransformer.transform(new NodeTransformExpr(node -> {
            Node r = xform.apply(node);
            //Node r = Optional.ofNullable(xform.apply(node)).orElse(node);
            return r;
        }), expr);
        return result;
    }

    public static E_OneOf oneOf(Node v, Iterable<Node> args) {
        ExprList el = new ExprList();
        el.addAll(ExprListUtils.nodesToExprs(args));

        Expr base = v.isVariable() ? new ExprVar(v) : NodeValue.makeNode(v);
        return new E_OneOf(base, el);
    }

    public static E_OneOf oneOfIris(String varName, String ... iris) {
        List<Node> nodes = Arrays.asList(iris).stream().map(NodeFactory::createURI).collect(Collectors.toList());
        return oneOf(Var.alloc(varName), nodes);
    }

    public static E_OneOf oneOf(String varName, Collection<Node> nodes) {
        return oneOf(Var.alloc(varName), nodes);
    }

    public static E_OneOf oneOf(Node v, Node ... args) {
        return oneOf(v, Arrays.asList(args));
    }


    public static E_NotOneOf notOneOf(Node v, Collection<Node> args) {
        ExprList el = new ExprList();
        el.addAll(ExprListUtils.nodesToExprs(args));

        Expr base = v.isVariable() ? new ExprVar(v) : NodeValue.makeNode(v);
        return new E_NotOneOf(base, el);
    }


    public static E_NotOneOf notOneOf(Node v, Node ... args) {
        return notOneOf(v, Arrays.asList(args));
    }

    public static Entry<Var, Node> tryGetVarConst(Expr a, Expr b) {
        Var v = a.isVariable()
                ? a.asVar()
                // Hack to unwrap variables from NodeValue
                : Optional.of(a).filter(Expr::isConstant)
                    .map(Expr::getConstant).map(NodeValue::asNode).filter(Node::isVariable).map(n -> (Var)n)
                    .orElse(null)
                ;

        Entry<Var, Node> result = v != null && b.isConstant()
                ? Maps.immutableEntry(v, b.getConstant().asNode())
                : null
                ;

        return result;
    }


    public static Entry<Var, Var> tryGetVarVar(Expr e) {
         Entry<Var, Var> result = null;

        if(e.isFunction()) {
            ExprFunction fn = e.getFunction();
            List<Expr> args = fn.getArgs();
            if(args.size() == 2) {
                Expr a = args.get(0);
                Expr b = args.get(1);
                result = tryGetVarVar(a, b);
                if(result == null) {
                    result = tryGetVarVar(b, a);
                }
            }
        }

        return result;
    }

    public static Entry<Var, Var> tryGetVarVar(Expr a, Expr b) {
        Entry<Var, Var> result = a.isVariable() && b.isVariable()
                ? Maps.immutableEntry(a.asVar(), b.asVar())
                : null;

        return result;
    }

    public static Entry<Var, Node> tryGetVarConst(Expr e) {
        Entry<Var, Node> result = null;

        if(e.isFunction()) {
            ExprFunction fn = e.getFunction();
            List<Expr> args = fn.getArgs();
            if(args.size() == 2) {
                Expr a = args.get(0);
                Expr b = args.get(1);
                result = tryGetVarConst(a, b);
                if(result == null) {
                    result = tryGetVarConst(b, a);
                }
            }
        }

        return result;
    }

    public static int classify(Expr e) {
        int result = e.isConstant() ? 0
                   : e.isVariable() ? 1
                   : e.isFunction() ? 2
                   : 3;
        return result;
    }

    public static int compare(Expr a, Expr b) {
        int ca = classify(a);
        int cb = classify(b);

        int r = cb - ca;

        if(r == 0) {
            switch(ca) {
            case 0: r = NodeValue.compare(a.getConstant(), b.getConstant()); break;
            case 1: r = a.getVarName().compareTo(b.getVarName()); break;
            case 2: r = a.getFunction().getFunctionIRI().compareTo(b.getFunction().getFunctionIRI()); break;
            default: throw new RuntimeException("should not come here");
            }
         }
        return r;
    }


    public static boolean isConstantsOnly(Iterable<Expr> exprs) {
        for(Expr expr : exprs) {
            if(!expr.isConstant()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks wtherer all arguments of the given function are constants (non-recursive).
     *
     * @param fn The function to test
     * @return True if all arguments are constants, false otherwise.
     */
    public static boolean isConstantArgsOnly(ExprFunction fn) {

        if(fn == null) {
            throw new RuntimeException("Null argument should not happen here");
        }

        boolean result = isConstantsOnly(fn.getArgs());

        return result;
    }

    public static String getFunctionId(ExprFunction fn) {

        String result = null;

        result = fn.getOpName();
        if(result != null) {
            return result;
        }



        result = fn.getFunctionIRI();
        if(result != null) {
            return result;
        }


        FunctionLabel label = fn.getFunctionSymbol();
        result = label == null ? null : label.getSymbol();

        /*
        if(result != null) {
            return result;
        }*/

        return result;
    }

    public static Expr applyNodeTransform(Expr expr, Map<?, ? extends Node> nodeMap) {
        NodeTransform nodeTransform = NodeTransformRenameMap.create(nodeMap);
        Expr result = NodeTransformLib.transform(nodeTransform, expr);
        //Expr result = applyNodeTransform(expr, nodeTransform);
        return result;
    }

//    public static Expr applyNodeTransform(Expr expr, NodeTransform nodeTransform) {
//        ElementTransform elementTransform = new ElementTransformSubst2(nodeTransform);
//        ExprTransform exprTransform = new ExprTransformNodeElement(nodeTransform, elementTransform);
//
//        Expr result = ExprTransformer.transform(exprTransform, expr);
//        return result;
//    }



    public static Expr andifyBalanced(Expr ... exprs) {
        return andifyBalanced(Arrays.asList(exprs));
    }

    public static Expr orifyBalanced(Expr... exprs) {
        return orifyBalanced(Arrays.asList(exprs));
    }

    public static List<String> extractNames(Collection<Var> vars) {
        List<String> result = new ArrayList<String>();
        for(Var var : vars) {
            result.add(var.getName());
        }

        return result;
    }

    public static Expr andifyBalanced(Iterable<Expr> exprs) {
        Expr result = opifyBalanced(exprs, (a, b) -> new E_LogicalAnd(a, b));
        return result;
    }


    // todo: isn't that a ring structure?
    public static <T> T distribute(
            List<T> as,
            List<T> bs,
            BinaryOperator<T> innerJunctor,
            BinaryOperator<T> outerJunctor) {
        List<T> items = new ArrayList<>(bs.size());
        for(T a : as) {
            for(T b : bs) {
                T item = innerJunctor.apply(a, b);
                items.add(item);
            }
        }
        T result = opifyBalanced(items, outerJunctor);

        return result;
    }


    public static <T> Optional<T> opify(Iterable<T> exprs, BinaryOperator<T> exprFactory) {
        Optional<T> result;
        Iterator<T> it = exprs.iterator();
        if(!it.hasNext()) {
            result = Optional.empty();
        } else {
            T tmp = it.next();
            while(it.hasNext()) {
                T b = it.next();
                tmp = exprFactory.apply(tmp, b);
            }
            result = Optional.of(tmp);
        }
        return result;
    }

    /**
     * Concatenates the sub exressions using a binary operator
     *
     * This method is not jena dependent and could be moved to aksw-commons
     *
     * and(and(0, 1), and(2, 3))
     *
     * @param exprs
     * @return
     */
    public static <T> T opifyBalanced(Iterable<? extends T> exprs, BiFunction<? super T, ? super T, ? extends T> exprFactory) {//BinaryOperator<T> exprFactory) {
        if(exprs.iterator().hasNext() == false) { //isEmpty()) {
            return null;
        }

        List<T> current = Lists.newArrayList(exprs.iterator());

        while(current.size() > 1) {

            List<T> next = new ArrayList<T>();
            T left = null;
            for(T expr : current) {
                if(left == null) {
                    left = expr;
                } else {
                    T newExpr = exprFactory.apply(left, expr);
                    next.add(newExpr);
                    left = null;
                }
            }

            if(left != null) {
                next.add(left);
            }

            current.clear();

            List<T> tmp = current;
            current = next;
            next = tmp;
        }

        return current.get(0);
    }

    public static Expr orifyBalanced(Iterable<Expr> exprs) {
        Expr result = opifyBalanced(exprs, (a, b) -> new E_LogicalOr(a, b));
        return result;
    }



    public static Entry<Var, NodeValue> extractConstantConstraint(Expr expr) {
        if(expr instanceof E_Equals) {
            E_Equals e = (E_Equals)expr;
            return extractVarConstant(e.getArg1(), e.getArg2());
        }

        return null;
    }

    public static Entry<Var, NodeValue> extractVarConstant(Expr expr) {
        Entry<Var, NodeValue> result = null;

        if(expr instanceof ExprFunction) {
            ExprFunction f = expr.getFunction();
            if(f.numArgs() == 2) {
                Expr a = f.getArg(1);
                Expr b = f.getArg(2);

                result = extractVarConstant(a, b);
            }
        }

        return result;
    }

    public static Entry<Var, NodeValue> extractVarConstant(Expr a, Expr b) {
        Entry<Var, NodeValue> result = extractVarConstantDirected(a, b);
        if(result == null) {
            result = extractVarConstantDirected(b, a);
        }

        return result;
    }

    /*
    public static void extractConstantConstraints(Expr a, Expr b, EquiMap<Var, NodeValue> equiMap) {
        extractConstantConstraints(a, b, equiMap.getKeyToValue());
    }*/


    /**
     * If a is a variable and b is a constant, then a mapping of the variable to the
     * constant is put into the map, and true is returned.
     * Otherwise, nothing is changed, and false is returned.
     *
     * A mapping of a variable is set to null, if it is mapped to multiple constants
     *
     *
     * @param a
     * @param b
     * @return
     */
    public static Entry<Var, NodeValue> extractVarConstantDirected(Expr a, Expr b) {
        if(!(a.isVariable() && b.isConstant())) {
            return null;
        }

        Var var = a.getExprVar().asVar();
        NodeValue nodeValue = b.getConstant();

        return new SimpleEntry<>(var, nodeValue);
    }


    public static List<Expr> getSubExprs(Expr expr) {
        List<Expr> result = expr != null && expr.isFunction()
                ? expr.getFunction().getArgs()
                : Collections.emptyList()
                ;

        return result;
    }

}
