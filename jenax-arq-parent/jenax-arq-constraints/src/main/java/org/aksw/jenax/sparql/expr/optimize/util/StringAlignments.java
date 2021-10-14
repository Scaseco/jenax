package org.aksw.jenax.sparql.expr.optimize.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Methods for rewriting two sequences of expressions as they occurr in
 * equality of concat expressions, i.e.
 *
 * "CONCAT(a1...an) = CONCAT(b1...bm)" where ai and bi may be variables and (string) constants.
 *
 * The code tries to generate multiple matches on a best effort basis.
 *
 * @author raven
 *
 */
public class StringAlignments {

    public static List<Alignment> toAlignment(List<Expr> a, List<List<Expr>> bs) {
        List<Alignment> result = new ArrayList<Alignment>();

        for(List<Expr> b : bs) {
            result.add(new Alignment(a, b));
        }

        return result;
    }

    public static List<Alignment> align(List<Expr> a, List <Expr> b) {
        List<Alignment> result;
        List<List<Expr>> tmp = new ArrayList<List<Expr>>();

        if(a.size() > b.size()) {

            alignRec(a, 0, b, 0, tmp);
            result = toAlignment(a, tmp);

        } else if(a.size() < b.size()) {

            alignRec(b, 0, a, 0, tmp);
            result = toAlignment(b, tmp);
        } else {
            result = Collections.singletonList(new Alignment(a, b));
        }

        return result;
    }

    public static int indexOfFirstConstant(List<Expr> a, int offset) {
        for(int i = offset; i < a.size(); ++i) {
            Expr ea = a.get(i);

            if(ea.isConstant()) {
                return i;
            }
        }

        return -1;
    }

    /**
     *
     *
     * @param a
     * @param i index of a constant
     * @param j position within the string of the constant
     * @return
     */
    public static List<Expr> copyReplace(List<Expr> a, int itemIndex, String[] parts) {
        List<Expr> result = new ArrayList<Expr>(a.size() + parts.length - 1);

        for(int i = 0; i < itemIndex; ++i) {
            result.add(a.get(i));
        }

        for(int i = 0; i < parts.length; ++i) {
            result.add(NodeValue.makeString(parts[i]));
        }

        for(int i = itemIndex + 1; i < a.size(); ++i) {
            result.add(a.get(i));
        }

        return result;

    }


    public static String[] split(String str, int i, int l) {
        int n = 1;

        if(i > 0) {
            n += 1;
        }

        if(i + l < str.length()) {
            n += 1;
        }

        String[] result = new String[n];
        int j = 0;

        if(i > 0) {
            result[j++] = str.substring(0, i);
        }

        result[j++] = str.substring(i, i + l);

        if(i + l < str.length()) {
            result[j++] = str.substring(i + l, str.length());
        }

        return result;
    }


    /**
     *
     * @param a The longer array
     * @param b The shorter one
     */
    public static void alignRec(List<Expr> a, int oa, List<Expr> b, int ob, List<List<Expr>> result) {

        int i = indexOfFirstConstant(a, oa);
        if(i < 0) {
            result.add(b);
            return;
        }

        Expr ea = a.get(i);
        String sa = ea.getConstant().asUnquotedString();

        for(int j = ob; j < b.size(); ++j) {
            Expr eb = b.get(j);

            if(!eb.isConstant()) {
                continue;
            }

            String sb = eb.getConstant().asUnquotedString();

            int k = 0;
            while((k = sb.indexOf(sa, k)) >= 0) {
                // Constants must align at beginning and ending
                if(i == 0 && k != 0) {
                    continue;
                }

                if(i == a.size() - 1 && k + sa.length() != sb.length()) {
                    // TODO Not sure if break is correct (we may miss alignments and thus get incomplete results) - with continue I entered an endless loop on the GADM mapping
                    // continue
                    break;
                }

                String[] parts = split(sb, k, sa.length());
                List<Expr> subB = copyReplace(b, j, parts);

                alignRec(a, i + 1, subB, j + parts.length - 1, result);

                k += sa.length();
            }
        }
    }


    public static Expr optimizeEqualsConcat2(List<Expr> la, List<Expr> lb)
    {
        // Remove common prefixes
        for (;;) {
            Expr a = la.get(0);
            Expr b = lb.get(0);

            if(a.equals(b)) {
                la.remove(0);
                lb.remove(0);
            } else if(a.isConstant() && b.isConstant()) {
                String sa = a.getConstant().asUnquotedString();
                String sb = b.getConstant().asUnquotedString();

                if(sa.startsWith(sb)) {
                    String delta = sa.substring(sb.length());
                    if(delta.isEmpty()) {
                        la.remove(0);
                    } else {
                        la.set(0, NodeValue.makeString(delta));
                    }
                    lb.remove(0);
                } else if(sb.startsWith(sa)) {
                    String delta = sb.substring(sa.length());
                    if(delta.isEmpty()) {
                        lb.remove(0);
                    } else {
                        lb.set(0, NodeValue.makeString(delta));
                    }

                    la.remove(0);
                    lb.set(0, NodeValue.makeString(delta));
                }
            }

            break;
        }

        int n = Math.min(la.size(), lb.size());

        // Now that the common prefix was removed, we might be left with something like
        // [ "123/separator456" ]
        // [ ?y, "/separator", ?z ]
        // or
        // [ ?a, "/" , ?b ]
        // [ ?x, "/" , ?y ]
        // or
        // [ ?a, " " , ?b , "/", ?c]
        // [ ?x, "/" , ?y ]
        //
        // The task we are now facing is to find an alignment of the constants




        boolean sameLength = la.size() == lb.size();

        int c = 0;
        for (; c < n; ++c) {
            Expr a = la.get(c);
            Expr b = lb.get(c);

            if (a.equals(b)) {
                continue;
            }


            /*
             * if(a.isConstant() && b.isConstant()) {
             * if(a.getConstant().asString().equals(b.getConstant().asString()))
             * { continue; } } else if(a.isVariable() && b.isVariable()) {
             * if(a.equals(b)) { continue; } }
             */

            break;
        }

        if (sameLength) {
            if (c == n) {
                // Expressions have same length and all arguments were equal
                return NodeValue.TRUE;
            } else if (c + 1 == n) {
                // Except for one argument all others were equal
                return new E_Equals(la.get(c), lb.get(c));
            }
        }

        // Remove the common prefix
        if (c == 0) {

            // Zero length common prefix - if both are constants, then the expression
            // can only evaluate to false

            if(lb.size() == 0) {
                throw new IndexOutOfBoundsException();
            }

            Expr a = la.get(0);
            Expr b = lb.get(0);
            if(a.isConstant() && b.isConstant()) {
                //String sa = a.getConstant().asUnquotedString();
                //String sb = b.getConstant().asUnquotedString();
                return NodeValue.FALSE;
            }

            //return expr;
            return null;
        } else {
            ExprList na = new ExprList();
            ExprList nb = new ExprList();

            for (int i = c; i < la.size(); ++i) {
                na.add(la.get(i));
            }

            for (int i = c; i < lb.size(); ++i) {
                nb.add(lb.get(i));
            }

            return new E_Equals(new E_StrConcat(na), new E_StrConcat(nb));
        }
    }
}