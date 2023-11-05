package org.aksw.jena_sparql_api.data_query.util;

import java.util.Arrays;

import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrContains;
import org.apache.jena.sparql.expr.E_StrLowerCase;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.vocabulary.RDFS;

// TODO Maybe move to the jsa concept package
public class KeywordSearchUtils {
    /**
     * ?s ?p ?o // your relation
     * Filter(Regex(Str(?o), 'searchString'))
     *
     * if includeSubject is true, the output becomes:
     *
     * Optional {
     *     ?s ?p ?o // your relation
     *     Filter(Regex(Str(?o), 'searchString'))
     * }
     * Filter(Regex(Str(?s), 'searchString') || Bound(?o))
     *
     *
     *
     * @param relation
     * @returns
     */
    public static Concept createConceptRegex(Fragment2 relation, String searchString, boolean includeSubject) {
        Concept result = includeSubject
            ? createConceptRegexIncludeSubject(relation, searchString)
            : createConceptRegexLabelOnly(relation, searchString);

        return result;
    }

    public static Concept createConceptRegexLabelOnly(Fragment2 relation, String searchString) {

        Concept result;
        if(searchString != null) {
            Element element = ElementUtils.groupIfNeeded(Arrays.asList(
                    relation.getElement(),
                    new ElementFilter(
                        new E_Regex(new E_Str(new ExprVar(relation.getTargetVar())), NodeValue.makeString(searchString), NodeValue.makeString("i")))
            ));

            result = new Concept(element, relation.getSourceVar());
        } else {
            result = null;
        }

        return result;
    }



    public static Concept createConceptRegexIncludeSubject(Fragment2 relation, String searchString) {
        Concept result;

        if(searchString != null) {
            Element relEl = relation.getElement();
            Var s = relation.getSourceVar();
            Var o = relation.getTargetVar();

            // var nv = NodeValueUtils.makeString(searchString);

            ExprVar es = new ExprVar(s);
            ExprVar eo = new ExprVar(o);
            Expr ess = NodeValue.makeString(searchString);
            Expr flags = NodeValue.makeString("i");

            Expr innerExpr = new E_Regex(new E_Str(eo), ess, flags);

            Expr outerExpr = new E_LogicalOr(
                new E_Regex(new E_Str(es), ess, flags),
                new E_Bound(eo));


            Element element = ElementUtils.groupIfNeeded(Arrays.asList(
                new ElementOptional(
                    ElementUtils.groupIfNeeded(Arrays.asList(relEl, new ElementFilter(innerExpr)))),
                new ElementFilter(outerExpr)
            ));

            result = new Concept(element, s);
        } else {
            result = null;
        }

        return result;
    }



//    public static Expr createExprExistsRegexIncludeSubject(Var srcVar, BinaryRelation relation, String searchString) {
//        Expr expr = null;
//        if(searchString != null) {
//            Element relEl = relation.getElement();
//            Var s = relation.getSourceVar();
//            Var o = relation.getTargetVar();
//
//            ExprVar es = new ExprVar(s);
//            ExprVar eo = new ExprVar(o);
//            Expr ess = NodeValue.makeString(searchString);
//            Expr flags = NodeValue.makeString("i");
//
//            Expr innerExpr = new E_Regex(new E_Str(eo), ess, flags);
//
//            expr =
//                    new E_LogicalOr(
//                        new E_Regex(new E_Str(es), ess, flags),
//                        new E_Exists(
//                            ElementUtils.groupIfNeeded(Arrays.asList(relEl, new ElementFilter(innerExpr)))));
//
//        }
//        return expr;
//    }

    /**
     * Create a the pattern:
     *
     * ?s { FILTER (regex(str(?s), searchString, 'i') ||
     *   EXISTS { relation(?s ?o) FILTER(regex(str(?o), searchString, 'i')) }
     *
     * @param relation
     * @param searchString
     * @return
     */
    public static Concept createConceptExistsRegexIncludeSubject(Fragment2 relation, String searchString) {
        Concept result;

        if(searchString != null) {
            Element relEl = relation.getElement();
            Var s = relation.getSourceVar();
            Var o = relation.getTargetVar();

            ExprVar es = new ExprVar(s);
            ExprVar eo = new ExprVar(o);
            Expr ess = NodeValue.makeString(searchString);
            Expr flags = NodeValue.makeString("i");

            Expr innerExpr = new E_Regex(new E_Str(eo), ess, flags);

            Element element = new ElementFilter(
                    new E_LogicalOr(
                        new E_Regex(new E_Str(es), ess, flags),
                        new E_Exists(
                            ElementUtils.groupIfNeeded(Arrays.asList(relEl, new ElementFilter(innerExpr))))));

            result = new Concept(element, s);
        } else {
            result = null;
        }

        return result;
    }

    /**
     * ?s ?p ?o // relation
     * Filter(<bif:contains>(?o, 'searchString')
     */
    public static Concept createConceptBifContains(Fragment2 relation, String searchString) {
        Concept result;

        if(searchString != null) {
            Var o = relation.getTargetVar();

            ExprVar eo = new ExprVar(o);
            Expr nv = NodeValue.makeString(searchString);

            Element element =
                ElementUtils.groupIfNeeded(Arrays.asList(
                    relation.getElement(),
                    //new ElementFilter(new E_Equals(eo, eo))
                    new ElementFilter(new E_Function("bif:contains", new ExprList(Arrays.asList(eo, nv))))
                ));

            Var s = relation.getSourceVar();
            result = new Concept(element, s);
        } else {
            result = null;
        }

        return result;
    }


    /** Create a FILTER (EXISTS (...)) expression that filters a set of resources down to those matching the keyword */
    public static Concept createConceptExistsRegex(Fragment2 relation, String searchString, boolean includeSubject) {
        Concept result = includeSubject
            ? createConceptExistsRegexIncludeSubject(relation, searchString)
            : createConceptExistsRegexLabelOnly(relation, searchString);

        return result;
    }

    public static Concept createConceptExistsRegexLabelOnly(Fragment2 relation, String searchString) {
        Concept result;
        if(searchString != null) {
            Element element = new ElementFilter(new E_Exists(ElementUtils.groupIfNeeded(
                    relation.getElement(),
                    new ElementFilter(new E_Regex(
                            new E_Str(new ExprVar(relation.getTargetVar())),
                            NodeValue.makeString(searchString),
                            NodeValue.makeString("i"))))));

            result = new Concept(element, relation.getSourceVar());
        } else {
            result = null;
        }
        return result;
    }

    public static Concept createConceptExistsStrConstainsLabelOnly(Fragment2 relation, String searchString) {

        String str = searchString.toLowerCase();
        Concept result;
        if(searchString != null) {
            Element element = new ElementFilter(new E_Exists(ElementUtils.groupIfNeeded(
                    relation.getElement(),
                    new ElementFilter(new E_StrContains(
                            new E_StrLowerCase(new E_Str(new ExprVar(relation.getTargetVar()))),
                            NodeValue.makeString(str)
                            )))));

            result = new Concept(element, relation.getSourceVar());
        } else {
            result = null;
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println(KeywordSearchUtils.createConceptExistsRegexIncludeSubject(Fragment2Impl.create(RDFS.Nodes.label), "test"));
        System.out.println(KeywordSearchUtils.createConceptExistsRegexLabelOnly(Fragment2Impl.create(RDFS.Nodes.label), "test"));
    }

}
