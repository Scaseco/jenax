/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aksw.jenax.graphql.sparql.v2.util.backport.syntaxtransform;

import java.util.ArrayDeque ;
import java.util.Deque ;
import java.util.List ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementAntiJoin;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementDataset;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementSemiJoin;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnfold;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.TransformElementLib;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps;

// TODO Part of this class is by now part of ApplyElementTransformVisitor ~ Claus 2019-06-06

/** A bottom-up application of a transformation of SPARQL syntax Elements.
 * {@linkplain QueryTransformOps#transform} provides the mechanism
 * to apply to a {@linkplain Query}.
 * @see ElementTransformCopyBase
 * @see UpdateTransformOps#transform
 */
public class ElementTransformer {
    private static ElementTransformer singleton = new ElementTransformer() ;

    /** Get the current transformer */
    public static ElementTransformer get() {
        return singleton ;
    }

    /** Set the current transformer - use with care */
    public static void set(ElementTransformer value) {
        ElementTransformer.singleton = value ;
    }

    /** Transform an algebra expression */
    public static Element transform(Element element, ElementTransform transform) {
        return transform(element, transform, null, null, null) ;
    }

    /** Transformation with specific ElementTransform and ExprTransform */
    public static Element transform(Element element, ElementTransform transform, ExprTransform exprTransform) {
        return get().transformation(element, transform, exprTransform, null, null) ;
    }

    public static Element transform(Element element, ElementTransform transform, ExprTransform exprTransform,
                                    ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        return get().transformation(element, transform, exprTransform, beforeVisitor, afterVisitor) ;
    }

    // To allow subclassing this class, we use a singleton pattern
    // and these protected methods.
    protected Element transformation(Element element, ElementTransform transform, ExprTransform exprTransform,
                                     ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        ApplyTransformVisitor v = new ApplyTransformVisitor(transform, exprTransform) ;
        return transformation(v, element, beforeVisitor, afterVisitor) ;
    }

    protected Element transformation(ApplyTransformVisitor transformApply, Element element,
                                     ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        if ( element == null ) {
            Log.warn(this, "Attempt to transform a null element - ignored") ;
            return element ;
        }
        return applyTransformation(transformApply, element, beforeVisitor, afterVisitor) ;
    }

    /** The primitive operation to apply a transformation to an Op */
    protected Element applyTransformation(ApplyTransformVisitor transformApply, Element element,
                                          ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
//        ElementWalker.walk(element, transformApply) ; // , beforeVisitor,
//                                                      // afterVisitor) ;
        beforeVisitor = beforeVisitor == null ? new ElementVisitorBase() : beforeVisitor;
        afterVisitor = afterVisitor == null ? new ElementVisitorBase() : afterVisitor;

        ElementWalker.walk(element, transformApply, beforeVisitor, afterVisitor);
        Element r = transformApply.result() ;
        return r ;
    }

    protected ElementTransformer() {}

    static class ApplyTransformVisitor implements ElementVisitor {
        protected final ElementTransform transform ;
        private final ExprTransform      exprTransform ;

        private final Deque<Element>     stack = new ArrayDeque<Element>() ;

        protected final Element pop() {
            return stack.pop() ;
        }

        protected final void push(Element elt) {
            stack.push(elt) ;
        }

        public ApplyTransformVisitor(ElementTransform transform, ExprTransform exprTransform) {
            if ( transform == null )
                transform = ElementTransformIdentity.get() ;

            if ( exprTransform == null )
                exprTransform = new ExprTransformCopy();

            this.transform = transform ;
            this.exprTransform = exprTransform ;
        }

        final Element result() {
            if ( stack.size() != 1 )
                Log.warn(this, "Stack is not aligned") ;
            return pop() ;
        }

        @Override
        public void visit(ElementTriplesBlock el) {
            Element el2 = transform.transform(el) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementPathBlock el) {
            Element el2 = transform.transform(el) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementFilter el) {
            Expr expr = el.getExpr() ;
            Expr expr2 = transformExpr(expr, exprTransform) ;
            Element el2 = transform.transform(el, expr2) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementAssign el) {
            Var v = el.getVar() ;
            Var v1 = TransformElementLib.applyVar(v, exprTransform) ;
            Expr expr = el.getExpr() ;
            Expr expr1 = ExprTransformer.transform(exprTransform, expr) ;
            Element el2 = transform.transform(el, v1, expr1 ) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementBind el) {
            Var v = el.getVar() ;
            Var v1 = TransformElementLib.applyVar(v, exprTransform) ;
            Expr expr = el.getExpr() ;
            Expr expr1 = ExprTransformer.transform(exprTransform, expr) ;
            Element el2 = transform.transform(el, v1, expr1 ) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementData el) {
            Element el2 = transform.transform(el) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementOptional el) {
            Element elSub = pop() ;
            Element el2 = transform.transform(el, elSub) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementGroup el) {
            ElementGroup newElt = new ElementGroup() ;
            boolean b = transformFromTo(el.getElements(), newElt.getElements()) ;
            Element el2 = transform.transform(el, newElt.getElements()) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementUnion el) {
            ElementUnion newElt = new ElementUnion() ;
            boolean b = transformFromTo(el.getElements(), newElt.getElements()) ;
            Element el2 = transform.transform(el, newElt.getElements()) ;
            push(el2) ;
        }

        private boolean transformFromTo(List<Element> elts, List<Element> elts2) {
            boolean changed = false ;
            for (Element elt : elts) {
                Element elt2 = pop() ;
                changed = (changed || (elt != elt2)) ;
                // Add reversed.
                elts2.add(0, elt2) ;
            }
            return changed ;
        }

        @Override
        public void visit(ElementDataset el) {
            Element sub = pop() ;
            Element el2 = transform.transform(el, sub) ;
            push(el) ;
        }

        @Override
        public void visit(ElementNamedGraph el) {
            Node n = el.getGraphNameNode() ;
            Node n1 = transformNode(n) ;
            Element elt1 = pop() ;
            Element el2 = transform.transform(el, n, elt1) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementExists el) {
            Element elt = el.getElement() ;
            Element elt1 = subElement(elt) ;
            Element el2 = transform.transform(el, elt1) ;
            push(el2) ;
        }

        @Override
        public void visit(ElementNotExists el) {
            Element elt = el.getElement() ;
            Element elt1 = subElement(elt) ;
            Element el2 = transform.transform(el, elt1) ;
            push(el2) ;
        }

        // When you need to force the walking of the tree ...
        // EXISTS / NOT EXISTS
        private Element subElement(Element elt) {
            ElementWalker.walk(elt, this) ;
            Element elt1 = pop() ;
            return elt1 ;
        }

        @Override
        public void visit(ElementMinus el) {
            Element elt = el.getMinusElement() ;
            Element elt1 = pop() ;
            if ( elt == elt1 )
                push(el) ;
            else
                push(new ElementMinus(elt1)) ;
        }

        @Override
        public void visit(ElementService el) {
            Node n = el.getServiceNode() ;
            Node n1 = transformNode(n) ;
            Element elt1 = pop() ;
            Element el2 = transform.transform(el, n1, elt1) ;
            push(el2) ;
//            boolean b = el.getSilent() ;
//            Node n = el.getServiceNode() ;
//            Node n1 = transformNode(n) ;
//            Element elt = el.getElement() ;
//            Element elt1 = pop() ;
//            Element el2 = transform.transform(el, n1, elt1) ;
//            push(el2) ;
        }

        @Override
        public void visit(ElementSubQuery el) {
            Query newQuery = QueryTransformOps.transform(el.getQuery(), transform, exprTransform) ;
            push(new ElementSubQuery(newQuery)) ;
        }

        @Override
        public void visit(ElementLateral el) {
            Element elSub = pop() ;
            Element el2 = transform.transform(el, elSub) ;
            push(el2) ;
        }

        private Node transformNode(Node n) {
            if ( exprTransform == null )
                return n ;
            return TransformElementLib.apply(n, exprTransform) ;
        }

        private ExprList transformExpr(ExprList exprList, ExprTransform exprTransform) {
            if ( exprList == null || exprTransform == null )
                return exprList ;
            return ExprTransformer.transform(exprTransform, exprList) ;
        }

        private Expr transformExpr(Expr expr, ExprTransform exprTransform) {
            if ( expr == null || exprTransform == null )
                return expr ;
            return ExprTransformer.transform(exprTransform, expr) ;
        }

        @Override
        public void visit(ElementUnfold el) {
            throw new UnsupportedOperationException();
//            Expr expr = el.getExpr() ;
//            Expr expr2 = transformExpr(expr, exprTransform) ;
//            Element el2 = transform.transform(el, expr2) ;
//            push(el2) ;
        }

        @Override
        public void visit(ElementSemiJoin el) {
            throw new UnsupportedOperationException();

        }

        @Override
        public void visit(ElementAntiJoin el) {
            throw new UnsupportedOperationException();
        }

//        @Override
//        public void visit(ElementFind el) {
//            Var v = el.getVar() ;
//            Var v1 = TransformElementLib.applyVar(v, exprTransform) ;
//            Triple t1 = transform.transform(el.getTriple());
//            Element el2 = transform.transform(el, v1, t1);
//            push(el2) ;
//        }
    }
}
