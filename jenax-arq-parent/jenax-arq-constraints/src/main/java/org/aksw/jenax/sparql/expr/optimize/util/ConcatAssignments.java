package org.aksw.jenax.sparql.expr.optimize.util;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;

import org.aksw.jenax.arq.expr.E_StrConcatPermissive;
import org.aksw.jenax.arq.rdfterm.E_RdfTerm;
import org.aksw.jenax.arq.util.expr.DnfUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.FunctionLabel;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Iterator for iterating all of a list's subLists with a given offset (default 0)
 * and increasing size.
 *
 * @author raven
 *
 * @param <T>
 */

public class ConcatAssignments {

    public static final Logger logger = LoggerFactory.getLogger(ConcatAssignments.class);


    public static final NodeValue TYPE_BLANK = NodeValue.makeInteger(0);
    public static final NodeValue TYPE_URI = NodeValue.makeInteger(1);
    public static final NodeValue TYPE_PLAIN_LITERAL = NodeValue.makeInteger(2);
    public static final NodeValue TYPE_TYPED_LITERAL = NodeValue.makeInteger(3);



    public static Expr getTypeOrExpr(Expr expr) {
        Expr result = null;

        if(expr.isConstant()) {
            Node node = expr.getConstant().getNode();

            if(node.isBlank()) {
                result = TYPE_BLANK;
            } else if(node.isURI()) {
                result = TYPE_URI;
            } else if(node.isLiteral()) {

                String datatype = node.getLiteral().getDatatypeURI();

                if(datatype == null || datatype.trim().isEmpty()) {
                    result = TYPE_PLAIN_LITERAL;
                } else {
                    result = TYPE_TYPED_LITERAL;
                }

            } else {

                throw new RuntimeException("Unkown node type: " + expr);

            }

        } else if(expr.isFunction()) {

            E_RdfTerm rdfTerm = expandRdfTerm(expr.getFunction());
            if(rdfTerm != null) {
                result = rdfTerm.getType();
            }

        } else {

            //throw new RuntimeException("Could not transform 'lang' function for: " + expr);
            result = null;

        }

        return result;
    }


    /**
     * TODO: If we really want the ___lexical value___, we
     * need to apply a transformation to value field based
     * on its type and on the RDF datatype.
     *
     * @param expr
     * @return
     */
    public static Expr getLexicalValueOrExpr(Expr expr) {

        Expr result;

        if(expr.isFunction()) {

            E_RdfTerm rdfTerm = expandRdfTerm(expr.getFunction());
            if(rdfTerm != null) {
                result = rdfTerm.getLexicalValue();
            } else {
                result = expr;
            }

        } else {

            result = expr;

        }

        //throw new RuntimeException("Not implemented yet");
        /*
        E_RdfTerm term = asRdfTerm(expr);

        Expr result = (term != null) ? term.getLexicalValue() : expr;
        */
        return result;
    }

    public static Expr getDatatypeOrExpr(Expr expr) {
        Expr result = null;

        if(expr.isConstant()) {
            Node node = expr.getConstant().getNode();
            if(node.isLiteral()) {
                String datatype = node.getLiteralDatatypeURI();

                // Prevent null values
                datatype = datatype == null ? "" : datatype;

                result = NodeValue.makeString(datatype);

            } else {
                throw new RuntimeException("Should not happen");
                //result = NodeValue.nvNothing;
            }
        } else if(expr.isFunction()) {

            E_RdfTerm rdfTerm = expandRdfTerm(expr.getFunction());
            if(rdfTerm != null) {
                result = rdfTerm.getDatatype();
            }

        } else {

            //throw new RuntimeException("Could not transform 'lang' function for: " + expr);
            result = null;

        }

        return result;
    }

    public static Expr extractLanguageTag(Expr expr) {

        Expr result = null;

        if(expr.isConstant()) {
            Node node = expr.getConstant().getNode();
            if(node.isLiteral()) {
                String lang = node.getLiteralLanguage();

                // Prevent null values
                lang = lang == null ? "" : lang;

                result = NodeValue.makeString(lang);

            } else {
                result = Expr.NONE; //NodeValue.nvNothing;
            }
        } else if(expr.isFunction()) {

            E_RdfTerm rdfTerm = expandRdfTerm(expr.getFunction());
            if(rdfTerm != null) {
                result = rdfTerm.getLanguageTag();
            }

        } else {

            //throw new RuntimeException("Could not transform 'lang' function for: " + expr);
            result = null;

        }

        return result;
    }


    /**
     * Expands both constants and functions to RDF terms
     *
     * I don't want to touch the legacy function - thats why this function has this name
     *
     * @param expr
     * @return
     */
    public static E_RdfTerm expandAnyToTerm(Expr expr) {
        E_RdfTerm result;

        if(expr.isConstant()) {
            result = expandConstant(expr);
        } else {
            result = expandRdfTerm(expr);
        }

        return result;
    }

    public static E_RdfTerm expandRdfTerm(Expr expr) {

        E_RdfTerm result = null;

        if(expr.isFunction()) {
            result = expandRdfTerm(expr.getFunction());
        }
        /*
        else if(expr.isConstant()) {
            result = expandConstant(expr);
        }*/

        return result;
    }



    public static E_RdfTerm expandConstant(Expr expr) {

        E_RdfTerm result = null;

        if(expr.isConstant()) {

            result = expandConstant(expr.getConstant().asNode());

        }

        return result;
    }

    public static E_RdfTerm expandConstant(Node node) {
        int type;

        // FIXME Expansion should make use of the lexical value - not the java object!
        Object val = "";
        String lang = null;
        String dt = null;

        if(node.isBlank()) {
            type = 0;
            val = node.getBlankNodeId().getLabelString();
        } else if(node.isURI()) {
            type = 1;
            val = node.getURI();
        } else if(node.isLiteral()) {

            val = node.getLiteral().getValue();

            //lex = node.getLiteralLexicalForm();

            String datatype = node.getLiteralDatatypeURI();
            if(datatype == null || datatype.isEmpty() || datatype.equals(RDFLangString.rdfLangString.getURI())) {
                //System.err.println("Treating plain literals as typed ones");
                logger.warn("Treating plain literals as typed ones");
                type = 2;
                lang = node.getLiteralLanguage();
            } else {
                type = 3;
                dt = node.getLiteralDatatypeURI();
            }
        } else {
            throw new RuntimeException("Should not happen");
        }

        String dtStr = dt == null ? "" : dt;
        String langStr = lang == null ? "" : lang;

        return new E_RdfTerm(
                NodeValue.makeDecimal(type), NodeValue.makeNode(val.toString(), null, dt),
                NodeValue.makeString(langStr), NodeValue.makeString(dtStr));

        /*
        return new E_Function(SparqlifyConstants.rdfTermLabel, SparqlSubstitute.makeExprList(
                NodeValue.makeDecimal(type), NodeValue.makeNode(lex.toString(), lang, dt),
                NodeValue.makeString(lang), NodeValue.makeString(dt)));
        */

    }


    /**
     * Converts op(f(argsA), f(argsB)) -> op1(op2(argsA[1], argsB[2]), ...,
     * op2(...)) This is mainly used for translating Equals(rdfTerm(argsA),
     * rdfTerm(argsB)) to And(Equals(argsA[0], argsB[0]), Equals(...), ...)
     *
     * Example: f(argsA) = f(argsB) -> argsA = argsB -> argsA.1 = argsB.1 &&
     * argsA.2 = argsB.2 && ...
     *
     * TODO: How to account for extra information that might be available on the
     * constraints on the variables, such as argX.y is a constant?
     *
     * Note: This does not work it many cases for e.g. concat : concat("ab",
     * "c") = concat("a", "bc")
     *
     *
     *
     * @param expr
     * @return
     */
    public static Expr optimizeRdfTerm(E_Equals expr) {
        Expr result = expr;

        // TODO Check if the arguments are simple or functions
        // An expression such as f(vector1) = f(vector2) can be translated into
        // vector1 = vector2 iff
        // . f is stateless/deterministic.
        // . f is not overloaded (we do not take that case into account)

        Expr a = expr.getArg1();
        Expr b = expr.getArg2();

        if (a.isFunction() && b.isFunction()) {
            ExprFunction fa = a.getFunction();
            ExprFunction fb = b.getFunction();

            FunctionLabel la = fa.getFunctionSymbol();
            FunctionLabel lb = fb.getFunctionSymbol();

            // TODO Vector label
            if (fa.getArgs().size() == fb.getArgs().size() && la.equals(lb)) {

                List<Expr> exprs = new ArrayList<Expr>();
                for (int i = 0; i < fa.getArgs().size(); ++i) {
                    Expr ea = fa.getArgs().get(i);
                    Expr eb = fb.getArgs().get(i);

                    exprs.add(new E_Equals(ea, eb));
                }

                result = ExprUtils.andifyBalanced(exprs);
            }
        }

        return result;
    }


    /**
     * Merges consecutive string arguments.
     * Used for concat: concat("a", "b", "c") becomes concat("abc");
     *
     * @param concat
     * @return
     */
    public static ExprList mergeConsecutiveConstants(Iterable<Expr> exprs) {
        String prev = null;
        ExprList newExprs = new ExprList();

        for (Expr expr : exprs) {
            if (expr.isConstant()) {
                prev = (prev == null ? "" : prev)
                        + expr.getConstant().asString();
            } else {
                if (prev != null) {
                    newExprs.add(NodeValue.makeString(prev));
                    prev = null;
                }
                newExprs.add(expr);
            }
        }

        if (prev != null) {
            newExprs.add(NodeValue.makeString(prev));
        }

        return newExprs;
    }


    public static boolean isConcatExpr(Expr expr) {
        return expr instanceof E_StrConcat || expr instanceof E_StrConcatPermissive;
    }


    /**
     * Optimizes Equals(Concat(argsA), Concat(argsB)) FIXME: An combinations,
     * where there are constants - Equals(Concat(args), const)
     *
     * //Assumes optimized form (the whole prefix in a single arg)
     *
     * The following cases are being handled: concat(prefixA, restA) =
     * concat(prefixB, restB) If none of the prefixes is a substring of the
     * other, the whole expression evaluated to false. otherwise, if the
     * prefixes are equal, they will be removed. if only one further argument
     * remains, the concat will be removed.
     *
     * Also, if one of the arguments is a constant, then it is treated as
     * Concat(const), and theabove rules are applied
     *
     * @param expr
     * @return
     */
    /*
    public static Expr optimizeEqualsConcat(ExprFunction fn) {

        if(fn instanceof ExprFunction2) {
            ExprFunction2 tmp = (ExprFunction2)fn;

            Expr result = optimizeEqualsConcat(tmp);

            return result;
        }

        return fn;
    }
    */
    public static Expr optimizeOpConcat(ExprFunction fn) {

        if(fn instanceof ExprFunction2) {
            ExprFunction2 tmp = (ExprFunction2)fn;

            Expr result = optimizeOpConcat(tmp);

            return result;
        }

        return fn;
    }


    public static Expr optimizeOpConcat(ExprFunction2 fn) {
        Expr ta = fn.getArg1();
        Expr tb = fn.getArg2();

        Expr result;
        if(!isOpConcatExpr(ta, tb)) {
            result = fn;
        } else {
            String fnId = ExprUtils.getFunctionId(fn);

            if(fnId.equals(Tags.symEQ)) {
                result = optimizeEqualsConcat(ta, tb);
            } else {

                BinaryOperator<Expr> exprFactory = fn::copy; // ExprFactoryUtils.getFactory2(fnId);
                assert exprFactory != null : "No expr factory for " + fnId;

                result = optimizeOpConcat(ta, tb, exprFactory);
            }


            //result = optimizeOpConcat(ta, tb);
        }

        return result;
    }

    /*
    public static Expr optimizeEqualsConcat(ExprFunction2 fn) {
        Expr ta = fn.getArg1();
        Expr tb = fn.getArg2();

        Expr result;
        if(!isEqualsConcatExpr(ta, tb)) {
            result = fn;
        } else {
            result = optimizeEqualsConcat(ta, tb);
        }

        return result;
    }
    */

    public static Expr optimizeEqualsConcat(Expr ta, Expr tb) {

        List<List<Expr>> ors = splitEqualsConcat(ta, tb);

        Expr result = DnfUtils.toExpr(ors);

        return result;
    }

    public static Expr optimizeOpConcat(Expr ta, Expr tb, BinaryOperator<Expr> exprFactory) {

        List<Expr> ors = splitOpConcat(ta, tb, exprFactory);

        Expr result = ExprUtils.orifyBalanced(ors);

        return result;
    }


    public static boolean isOpConcatExpr(Expr ta, Expr tb) {

        if (isConcatExpr(ta) || isConcatExpr(tb)) {
            return true;
        }

        return false;
    }


    /**
     *
     * @param expr
     * @return
     */
    public static List<Expr> getOptimizedConcatArgs(Expr expr) {
        List<Expr> args = isConcatExpr(expr)
                ? expr.getFunction().getArgs()
                : Collections.singletonList(expr);

        List<Expr> result = mergeConsecutiveConstants(args).getList();

        return result;
    }

    /**
     *
     *
     * @param ta The first concat expression
     * @param tb The second concat expression
     * @return
     */
    public static List<List<Expr>> splitEqualsConcat(Expr ta, Expr tb) {

        // Create a list of concat-arguments (if not a concat, treat the expression
        // as an argument
        List<Expr> la = getOptimizedConcatArgs(ta);
        List<Expr> lb = getOptimizedConcatArgs(tb);

        List<List<Expr>> result = ConcatAssignments.splitEqualsConcat(la, lb); //optimizeEqualsConcatAlign(la, lb);

        return result;
    }

    public static List<Expr> splitOpConcat(Expr ta, Expr tb, BinaryOperator<Expr> exprFactory) {

        // Create a list of concat-arguments (if not a concat, treat the expression
        // as an argument
        List<Expr> la = getOptimizedConcatArgs(ta);
        List<Expr> lb = getOptimizedConcatArgs(tb);

        List<Expr> result = ConcatAssignments.splitOpConcat(la, lb, exprFactory); //optimizeEqualsConcatAlign(la, lb);

        return result;
    }


    /**
     * [(a1, b1)], [(a1, b1), (a2, b2)]
     *
     *
     * @param la
     * @param lb
     * @return
     */
    public static List<Expr> splitOpConcat(List<Expr> la, List<Expr> lb, BinaryOperator<Expr> exprFactory) {
        List<Alignment> cs = StringAlignments.align(la, lb);

        List<Expr> ors = new ArrayList<Expr>();
        for(Alignment c : cs) {


            if(c.isSameSize()) {

                Expr headExpr = null;

                for(int i = 0; i < c.getKey().size(); ++i) {
                    Expr ea = c.getKey().get(i);
                    Expr eb = c.getValue().get(i);

                    if(ea.isConstant() && ea.equals(eb)) {
                        continue;
                    }

                    // Create the inequality expression (e.g. a > b)
                    Expr tmpExpr = exprFactory.apply(ea, eb);

                    // Prepend all 'head' conditions (e.g. ?x = ?y)
                    Expr expr;
                    if(headExpr == null) {
                        expr = tmpExpr;
                    } else {
                        expr = new E_LogicalAnd(headExpr, tmpExpr);
                    }

                    ors.add(expr);

                    // Try to add an additional condition to the head expr
                    // TODO Detect unsatisfiability early
                    E_Equals eq = new E_Equals(ea, eb);
                    headExpr = new E_LogicalAnd(headExpr, eq);
                }

            } else {
                ors.add(
                    new E_Equals(
                        new E_StrConcatPermissive(new ExprList(c.getKey())),
                        new E_StrConcatPermissive(new ExprList(c.getValue()))
                    ));
            }

            //ors.add(ands);
        }

        return ors;
    }

    /**
     * Returns a list of alternatives (ors), whereas each alternative
     * is a list of Equals expressions:
     * [ [Equals("x", "x"), Equals(?a, ?b)], [...], ... ]
     *
     * @param la
     * @param lb
     * @return
     */
    public static List<List<Expr>> splitEqualsConcat(List<Expr> la, List<Expr> lb) {
        List<Alignment> cs = StringAlignments.align(la, lb);

        List<List<Expr>> ors = new ArrayList<List<Expr>>();
        for(Alignment c : cs) {

            List<Expr> ands = new ArrayList<Expr>();

            if(c.isSameSize()) {

                for(int i = 0; i < c.getKey().size(); ++i) {
                    Expr ea = c.getKey().get(i);

                    /*
                    if(i >= c.getValue().size()) {
                        System.out.println("OOpps");
                    }
                    */

                    Expr eb = c.getValue().get(i);

                    if(ea.isConstant() && ea.equals(eb)) {
                        continue;
                    }

                    E_Equals eq = new E_Equals(ea, eb);
                    ands.add(eq);
                }
            } else {
                ands.add(
                    new E_Equals(
                        new E_StrConcatPermissive(new ExprList(c.getKey())),
                        new E_StrConcatPermissive(new ExprList(c.getValue()))
                    ));
            }

            ors.add(ands);
        }

        return ors;
    }


}
