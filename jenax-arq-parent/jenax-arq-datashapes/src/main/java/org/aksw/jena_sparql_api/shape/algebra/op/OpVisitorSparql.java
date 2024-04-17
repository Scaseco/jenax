package org.aksw.jena_sparql_api.shape.algebra.op;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.ConceptOps;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.vocabulary.RDF;

public class OpVisitorSparql
    implements OpVisitor<Fragment1>
{
    protected PathExVisitorSparql pathVisitor;
    protected Generator<Var> generator;

    public OpVisitorSparql(Generator<Var> generator) {
        this.generator = generator;
    }

    @Override
    public Fragment1 visit(OpAssign op) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Fragment1 visit(OpAnd op) {
        Fragment1 a = op.getLeft().accept(this);
        Fragment1 b = op.getRight().accept(this);
        Fragment1 result = ConceptOps.intersect(a, b, generator);
        return result;
    }

    @Override
    public Fragment1 visit(OpUnion op) {
        Fragment1 a = op.getLeft().accept(this);
        Fragment1 b = op.getRight().accept(this);
        Fragment1 result = ConceptOps.union(a, b, generator);
        return result;
    }

    @Override
    public Fragment1 visit(OpExists op) {
        Fragment2 relation = op.getRole();
        Fragment1 filler = op.getSubOp().accept(this);
        Fragment1 result = ConceptOps.exists(relation, filler, generator);
        return result;
    }

    @Override
    public Fragment1 visit(OpForAll op) {
        Fragment2 relation = op.getRole();
        Fragment1 filler = op.getSubOp().accept(this);
        Fragment1 result = ConceptOps.forAllIfRolePresent(relation, filler, generator);
        return result;
    }

    @Override
    public Fragment1 visit(OpSparqlConcept op) {
        Fragment1 result = op.getConcept();
        return result;
    }

    @Override
    public Fragment1 visit(OpType op) {
        Node node = op.getType();
        Element e = ElementUtils.createElement(Triple.create(Vars.s, RDF.type.asNode(), node));
        Fragment1 result = new Concept(e, Vars.s);
        return result;
    }

    @Override
    public Fragment1 visit(OpTop op) {
        Fragment1 result = Concept.TOP;//ConceptUtils.createSubjectConcept();
        return result;
    }

    @Override
    public Fragment1 visit(OpConcept op) {
        Fragment1 result = op.getConcept();
        return result;
    }

    @Override
    public Fragment1 visit(OpFilter op) {
        Expr expr = op.getExpr();
        Fragment1 concept = op.getSubOp().accept(this);
        Var conceptVar = concept.getVar();
        Map<Var, Var> varMap = Collections.singletonMap(Vars.lodash, conceptVar);

        Expr newExpr = ExprUtils.applyNodeTransform(expr, varMap);
        Element newElement = ElementUtils.mergeElements(concept.getElement(), new ElementFilter(newExpr));

        Fragment1 result = new Concept(newElement, conceptVar);
        return result;
    }

    @Override
    public Fragment1 visit(OpFocus op) {
        Fragment1 concept = op.getSubOp().accept(this);
        Path path = op.getPath();
        Fragment2 relation = Fragment2Impl.create(path);
        Fragment1 result = ConceptUtils.getRelatedConcept(concept, relation);
        return result;
    }

    @Override
    public Fragment1 visit(OpEnumeration op) {
        List<Node> nodes = op.getNodes();

        Fragment1 result = ConceptUtils.createConcept(nodes);
        return result;
    }

}
