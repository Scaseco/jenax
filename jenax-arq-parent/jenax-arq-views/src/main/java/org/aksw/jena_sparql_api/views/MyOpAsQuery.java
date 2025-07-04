package org.aksw.jena_sparql_api.views;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.ARQNotImplemented;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.OpAntiJoin;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
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
import org.apache.jena.sparql.algebra.op.OpSemiJoin;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnfold;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.util.graph.GraphList;
import org.apache.jena.vocabulary.RDF;

// FIXME Can we remove this class? IIRC this class was copied because of some issue with LSQ.

/** Convert an Op expression in SPARQL syntax, that is, the reverse of algebra generation */
public class MyOpAsQuery
{
    public static Query asQuery(Op op)
    {
        return asQuery(op, Dialect.DEFAULT);
    }


    public static Query asQuery(Op op, Dialect dialect)
    {
        Query query = QueryFactory.make() ;

        Converter v = new Converter(query, dialect) ;
        //OpWalker.walk(op, v) ;
        op.visit(v) ;

        List<Var> vars = v.projectVars;
        query.setQueryResultStar(vars.isEmpty()); // SELECT * unless we are projecting
        Iterator<Var> iter = vars.iterator();
        for (; iter.hasNext();) {
            Var var = iter.next();
            if (v.varExpression.containsKey(var))
                query.addResultVar(var, v.varExpression.get(var));
            else
                query.addResultVar(var);
        }

        ElementGroup eg = v.currentGroup ;
        query.setQueryPattern(eg) ;
        query.setQuerySelectType() ;

        query.setResultVars() ;
        return query ;
    }

    public static class Converter implements OpVisitor
    {
        private Query query ;
        private Element element = null ;
        private ElementGroup currentGroup = null ;
        private Stack<ElementGroup> stack = new Stack<ElementGroup>() ;
        private List<Var> projectVars = Collections.emptyList() ;
        private Map<Var, Expr> varExpression = new HashMap<Var, Expr>() ;

        private Dialect dialect;

        public Converter(Query query, Dialect dialect)
        {
            this.dialect = dialect;

            this.query = query ;
            currentGroup = new ElementGroup() ;
        }

        Element asElement(Op op)
        {
            ElementGroup g = asElementGroup(op) ;
            if ( g.getElements().size() == 1 )
                return g.getElements().get(0) ;
            return g ;
        }

        ElementGroup asElementGroup(Op op)
        {
            startSubGroup() ;
            op.visit(this) ;
            return endSubGroup() ;
        }

        @Override
        public void visit(OpBGP opBGP)
        {
            currentGroup().addElement(process(opBGP.getPattern())) ;
        }

//        public void visit(OpPropFunc opPropFunc)
//        {
//            OpBGP opBGP = opPropFunc.getBGP() ;
//            currentGroup().addElement(process(opBGP.getPattern())) ;
//        }

        @Override
        public void visit(OpTriple opTriple)
        { currentGroup().addElement(process(opTriple.getTriple())) ; }

        @Override
        public void visit(OpQuad opQuad)
        { throw new ARQNotImplemented("OpQuad") ; }


        @Override
        public void visit(OpProcedure opProcedure)
        {
            throw new ARQNotImplemented("OpProcedure") ;
        }

        @Override
        public void visit(OpPropFunc opPropFunc)
        {
            Node s = processPropFuncArg(opPropFunc.getSubjectArgs()) ;
            Node o = processPropFuncArg(opPropFunc.getObjectArgs()) ;
            Triple t = Triple.create(s, opPropFunc.getProperty(), o) ;
            currentGroup().addElement(process(t)) ;
        }

        private Node processPropFuncArg(PropFuncArg args)
        {
            if ( args.isNode() )
                return args.getArg() ;

            // List ...
            List<Node> list = args.getArgList() ;
            if ( list.size() == 0 )
                return RDF.Nodes.nil ;
            BasicPattern bgp = new BasicPattern() ;
            Node head = GraphList.listToTriples(list, bgp) ;
            currentGroup().addElement(process(bgp)) ;
            return head ;
        }

        @Override
        public void visit(OpSequence opSequence)
        {
            ElementGroup g = currentGroup() ;
            boolean nestGroup = ! g.isEmpty() ;
            if ( nestGroup )
            {
                startSubGroup() ;
                g = currentGroup() ;
            }

            Iterator<Op> iter = opSequence.iterator() ;

            for ( ; iter.hasNext() ; )
            {
                Op op = iter.next() ;
                Element e = asElement(op) ;
                g.addElement(e) ;
            }
            if ( nestGroup )
                endSubGroup() ;
            return ;
        }

        @Override
        public void visit(OpDisjunction opDisjunction)
        {
            if(opDisjunction.getElements().isEmpty()) {
                return;
            }

            Op a = null;
            for(Op b : opDisjunction.getElements()) {
                if(a == null) {
                    a = b;
                    continue;
                }

                a = new OpUnion(a, b);
            }


            a.visit(this);
            //throw new ARQNotImplemented("OpDisjunction") ;
        }

        private Element process(BasicPattern pattern)
        {
            // The different SPARQL versions use different internal structures for BGPs.
            if ( query.getSyntax() == Syntax.syntaxSPARQL_10 )
            {
                ElementTriplesBlock e = new ElementTriplesBlock() ;
                for (Triple t : pattern)
                    // Leave bNode variables as they are
                    // Query serialization will deal with them.
                    e.addTriple(t) ;
                return e ;
            }

            if ( query.getSyntax() == Syntax.syntaxSPARQL_11 ||
                 query.getSyntax() == Syntax.syntaxARQ )
            {
                ElementPathBlock e = new ElementPathBlock() ;
                for (Triple t : pattern)
                    // Leave bNode variables as they are
                    // Query serialization will deal with them.
                    e.addTriple(t) ;
                return e ;
            }

            throw new ARQInternalErrorException("Unrecognized syntax: "+query.getSyntax()) ;

        }

        private ElementTriplesBlock process(Triple triple)
        {
            // Unsubtle
            ElementTriplesBlock e = new ElementTriplesBlock() ;
            e.addTriple(triple) ;
            return e ;
        }

        @Override
        public void visit(OpQuadPattern quadPattern)
        {
            BasicPattern pattern = quadPattern.getBasicPattern();

            if(!Quad.isDefaultGraph(quadPattern.getGraphNode())) {
                OpGraph opGraph = new OpGraph(quadPattern.getGraphNode(), new OpBGP(pattern));
                visit(opGraph);
            } else {
                currentGroup().addElement(process(pattern)) ;
            }

            //throw new ARQNotImplemented("OpQuadPattern") ;
        }

        @Override
        public void visit(OpPath opPath)
        { throw new ARQNotImplemented("OpPath") ; }

        @Override
        public void visit(OpJoin opJoin)
        {
            // Keep things clearly separated.
            Element eLeft = asElement(opJoin.getLeft()) ;
            Element eRight = asElementGroup(opJoin.getRight()) ;

            ElementGroup g = currentGroup() ;
            g.addElement(eLeft) ;
            g.addElement(eRight) ;
            return ;
        }

        private static boolean emptyGroup(Element element)
        {
            if ( ! ( element instanceof ElementGroup ) )
                return false ;
            ElementGroup eg = (ElementGroup)element ;
            return eg.isEmpty() ;
        }

        @Override
        public void visit(OpLeftJoin opLeftJoin)
        {
            Element eLeft = asElement(opLeftJoin.getLeft()) ;
            ElementGroup eRight = asElementGroup(opLeftJoin.getRight()) ;

            if ( opLeftJoin.getExprs() != null )
            {
                for ( Expr expr : opLeftJoin.getExprs() )
                {
                    ElementFilter f = new ElementFilter(expr) ;
                    eRight.addElement(f) ;
                }
            }
            ElementGroup g = currentGroup() ;
            if ( ! emptyGroup(eLeft) )
                g.addElement(eLeft) ;
            ElementOptional opt = new ElementOptional(eRight) ;
            g.addElement(opt) ;
        }

//        @Override
//        public void visit(OpDiff opDiff)
//        { throw new ARQNotImplemented("OpDiff") ; }

        @Override
        public void visit(OpSemiJoin op)
        { throw new ARQNotImplemented("OpSemiJoin") ; }

        @Override
        public void visit(OpAntiJoin opDiff)
        { throw new ARQNotImplemented("OpAntiJoin") ; }

        @Override
        public void visit(OpMinus opMinus)
        { throw new ARQNotImplemented("OpMinus") ; }

        @Override
        public void visit(OpUnion opUnion)
        {
            Element eLeft = asElementGroup(opUnion.getLeft()) ;
            Element eRight = asElementGroup(opUnion.getRight()) ;
            if ( eLeft instanceof ElementUnion )
            {
                ElementUnion elUnion = (ElementUnion)eLeft ;
                elUnion.addElement(eRight) ;
                return ;
            }

//            if ( eRight instanceof ElementUnion )
//            {
//                ElementUnion elUnion = (ElementUnion)eRight ;
//                elUnion.getElements().add(0, eLeft) ;
//                return ;
//            }

            ElementUnion elUnion = new ElementUnion() ;
            elUnion.addElement(eLeft) ;
            elUnion.addElement(eRight) ;
            currentGroup().addElement(elUnion) ;
        }

        @Override
        public void visit(OpConditional opCondition)
        { throw new ARQNotImplemented("OpCondition") ; }

        @Override
        public void visit(OpFilter opFilter)
        {
            // (filter .. (filter ( ... ))   (non-canonicalizing OpFilters)
            // Inner gets Grouped unnecessarily.
            Element e = asElement(opFilter.getSubOp()) ;
            if ( currentGroup() != e )
                currentGroup().addElement(e) ;
            element = currentGroup() ;      // Was cleared by asElement.

            ExprList exprs = opFilter.getExprs() ;
            for ( Expr expr : exprs )
            {
                ElementFilter f = new ElementFilter(expr) ;
                currentGroup().addElement(f) ;
            }
        }

        @Override
        public void visit(OpGraph opGraph)
        {
            startSubGroup() ;
            Element e = asElement(opGraph.getSubOp()) ;
            ElementGroup g = endSubGroup() ;

            Element graphElt = new ElementNamedGraph(opGraph.getNode(), e) ;
            currentGroup().addElement(graphElt) ;
        }

        @Override
        public void visit(OpService opService)
        {
            // Hmm - if the subnode has been optimized, we may fail.
            Op op = opService.getSubOp() ;
            Element x = asElement(opService.getSubOp()) ;
            Element elt = new ElementService(opService.getService(), x, opService.getSilent()) ;
            currentGroup().addElement(elt) ;
        }

        @Override
        public void visit(OpDatasetNames dsNames)
        { throw new ARQNotImplemented("OpDatasetNames") ; }

        @Override
        public void visit(OpTable opTable)
        {
            // This will go in a group so simply forget it.
            if ( opTable.isJoinIdentity() ) return ;
            throw new ARQNotImplemented("OpTable") ;
        }

        @Override
        public void visit(OpExt opExt)
        {
            Op op = opExt.effectiveOp() ;


            if(op == null) {
                throw new RuntimeException("Effective Op is null for: " + opExt.getClass());

            }

//            // This does not work in all cases.
            op.visit(this) ;
            //throw new ARQNotImplemented("OpExt") ;
        }

        @Override
        public void visit(OpNull opNull)
        { throw new ARQNotImplemented("OpNull") ; }

        @Override
        public void visit(OpLabel opLabel)
        {
            if ( opLabel.hasSubOp() )
                opLabel.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpAssign opAssign)
        {
        opAssign.getSubOp().visit(this) ;

            // Go through each var and get the assigned expression
            for ( Var v : opAssign.getVarExprList().getVars() )
            {
                Expr e = opAssign.getVarExprList().getExpr(v);

                // Substitute group aggregate expressions in for generated vars
                SubExprForVar sefr = new SubExprForVar(varExpression);
                Expr tr = ExprTransformer.transform(sefr, e);

                // If in top level we defer assignment to SELECT section
                // This also covers the GROUP recombine
                // NOTE: this means we can't round trip top-level BINDs
                if (inTopLevel()) {
                    varExpression.put(v, tr);
                } else {
                    Element elt = new ElementAssign(v, e) ;
                    ElementGroup g = currentGroup() ;
                    g.addElement(elt) ;
                }
            }
        }

        @Override
        public void visit(OpExtend opExtend)
        {
            if(!inTopLevel() && dialect.equals(Dialect.VIRTUOSO)) {

                Set<Var> vars = new HashSet<Var>(OpVars.mentionedVars(opExtend));
                OpProject project = new OpProject(opExtend, new ArrayList<Var>(vars));

                Query query = MyOpAsQuery.asQuery(project, dialect);
                //System.out.println(query);
                ElementSubQuery elt = new ElementSubQuery(query);
                ElementGroup g = currentGroup() ;
                g.addElement(elt) ;
                return;
            }

            opExtend.getSubOp().visit(this) ;

            // Go through each var and get the assigned expression
            for ( Var v : opExtend.getVarExprList().getVars() )
            {
                Expr e = opExtend.getVarExprList().getExpr(v);

                // Substitute group aggregate expressions in for generated vars
                Expr tr = ExprTransformer.transform(new SubExprForVar(varExpression), e);

                // If in top level we defer assignment to SELECT section
                // This also covers the GROUP recombine
                // NOTE: this means we can't round trip top-level BINDs
                if (inTopLevel()) {
                    varExpression.put(v, tr);
                } else {
                    Element elt = new ElementBind(v, tr) ;
                    ElementGroup g = currentGroup() ;
                    g.addElement(elt) ;
                }
            }
        }


        @Override
        public void visit(OpList opList)
        { /* No action */ }

        @Override
        public void visit(OpOrder opOrder)
        {
            List<SortCondition> x = opOrder.getConditions() ;
            for ( SortCondition sc : x )
                query.addOrderBy(sc);
            opOrder.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpProject opProject)
        {
            /*
            if(dialect.equals(Dialect.VIRTUOSO)) {
                if(opProject.getSubOp() instanceof OpExtend) {

                    //Set<Var> vars = GetVarsMentioned.getVarsMentioned(opProject.getSubOp());
                    //OpProject project = new OpProject(opExtend, new ArrayList<Var>(vars));

                    Query query = MyOpAsQuery.asQuery(opProject);
                    //System.out.println(query);
                    ElementSubQuery elt = new ElementSubQuery(query);
                    ElementGroup g = currentGroup() ;
                    g.addElement(elt) ;
                    return;
                }
            }*/

            // Defer adding result vars until the end.
            // OpGroup generates dupes otherwise
            this.projectVars = opProject.getVars();
            opProject.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpReduced opReduced)
        {
            query.setReduced(true) ;
            opReduced.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpDistinct opDistinct)
        {
            query.setDistinct(true) ;
            opDistinct.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpSlice opSlice)
        {
            if ( opSlice.getStart() != Query.NOLIMIT )
                query.setOffset(opSlice.getStart()) ;
            if ( opSlice.getLength() != Query.NOLIMIT )
                query.setLimit(opSlice.getLength()) ;
            opSlice.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpGroup opGroup) {
            List<ExprAggregator> a = opGroup.getAggregators();

            // Aggregators are broken up in the algebra, split between a
            // group and an assignment (extend or assign) using a generated var.
            // We record them here and insert later.
            for (ExprAggregator ea : a) {
                // Substitute generated var for actual
                Var givenVar = ea.getAggVar().asVar();
                // Copy aggregator across (?)
                Expr myAggr = query.allocAggregate(ea.getAggregator());
                varExpression.put(givenVar, myAggr);
            }

            VarExprList b = opGroup.getGroupVars();
            for (Var v : b.getVars()) {
                Expr e = b.getExpr(v);

                if (e != null) {
                    query.addGroupBy(v, e);

                } else {
                    query.addGroupBy(v);

                }
            }
            opGroup.getSubOp().visit(this);
        }

        @Override
        public void visit(OpTopN opTop)
        { throw new ARQNotImplemented("OpTopN") ; }

        private Element lastElement()
        {
            ElementGroup g = currentGroup ;
            if ( g == null || g.getElements().size() == 0 )
                return null ;
            int len = g.getElements().size() ;
            return g.getElements().get(len-1) ;
        }

        @Override
        public void visit(OpUnfold opUnfold) {
            throw new UnsupportedOperationException();
//            Element e = asElement(opUnfold.getSubOp()) ;
//            // If (unfold ... (table unit)), and first in group, don't add the empty group.
//            insertIntoGroup(currentGroup(), e) ;
//            Element elmtUnfold = new ElementUnfold( opUnfold.getExpr(), opUnfold.getVar1(), opUnfold.getVar2() ) ;
//            currentGroup().addElement(elmtUnfold) ;
        }

        private void startSubGroup()
        {
            push(currentGroup) ;
            ElementGroup g = new ElementGroup() ;
            currentGroup = g ;
        }

        private ElementGroup endSubGroup()
        {
            ElementGroup g = pop() ;
            ElementGroup r = currentGroup ;
            currentGroup = g ;
            return r ;
        }

//        private void endCurrentGroup()
//        {
//            currentGroup = null ;
//            element = null ; //??
//        }

        private ElementGroup currentGroup()
        {
//            if ( currentGroup == null )
//                startSubGroup() ;
            return currentGroup ;
        }

        private ElementGroup peek()
        {
            if ( stack.size() == 0 )
                return null ;
            return stack.peek();
        }
        private ElementGroup pop() { return stack.pop(); }
        private void push(ElementGroup el) { stack.push(el); }
        private boolean inTopLevel() { return stack.size() == 0; }

        @Override
        public void visit(OpQuadBlock op) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void visit(OpLateral opLateral) {
            Element eLeft = asElement(opLateral.getLeft()) ;
            ElementGroup eRight = asElementGroup(opLateral.getRight()) ;
            ElementGroup g = currentGroup() ;
            if ( !emptyGroup(eLeft) ) {
                if ( eLeft instanceof ElementGroup )
                    g.getElements().addAll(((ElementGroup)eLeft).getElements()) ;
                else
                    g.addElement(eLeft) ;
            }
            ElementLateral eltLateral = new ElementLateral(eRight) ;
            g.addElement(eltLateral) ;
        }

//        @Override
//        public void visit(OpFind opFind) {
//            throw new RuntimeException("Not implemented");
//        }
    }

    /**
     * This class is used to take substitute an expressions for variables
     * in another expression. It is used to stick grouping expressions back
     * together.
     */
    public static class SubExprForVar extends ExprTransformCopy {
        private final Map<Var, Expr> varExpr;
        private boolean subOccurred = false;
        public SubExprForVar(Map<Var, Expr> varExpr) {
            this.varExpr = varExpr;
        }

        public boolean didChange() { return subOccurred; }

        @Override
        public Expr transform(ExprVar var) {
            if (varExpr.containsKey(var.asVar())) {
                subOccurred = true;
                return varExpr.get(var.asVar()).deepCopy();
            }
            else return var.deepCopy();
        }
    }
}
