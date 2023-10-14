package org.aksw.jenax.sparql.fragment.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.collections.SetUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.stmt.parser.element.SparqlElementParser;
import org.aksw.jenax.stmt.parser.element.SparqlElementParserImpl;
import org.aksw.jenax.stmt.parser.prologue.SparqlPrologueParser;
import org.aksw.jenax.stmt.parser.prologue.SparqlPrologueParserImpl;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.PatternVars;

/**
 * This is a binary relation used to relate two concepts to each other
 *
 * @author raven
 *
 */
public class Fragment2Impl
    implements Fragment2 // TODO Actually we could just extend RelationImpl
{
    private Var sourceVar;
    private Var targetVar;
    private Element element;

    public Fragment2Impl(Element element, Var sourceVar, Var targetVar) {
        this.element = element;
        this.sourceVar = sourceVar;
        this.targetVar = targetVar;
    }

    public List<Var> getVars() {
        return Arrays.asList(sourceVar, targetVar);
    }

    public Var getSourceVar() {
        return sourceVar;
    }

    public Var getTargetVar() {
        return targetVar;
    }

    public Element getElement() {
        return element;
    }

    public static Fragment2 create(String elementStr, String sourceVarName,
            String targetVarName) {
        SparqlElementParser parser = SparqlElementParserImpl.create(Syntax.syntaxARQ, null);
        Fragment2 result = create(elementStr, sourceVarName, targetVarName, parser);
        return result;
    }

    public static Fragment2 create(String prologueStr, String elementStr, String sourceVarName,
            String targetVarName) {
        SparqlQueryParser queryParser = SparqlQueryParserImpl.create();
        SparqlPrologueParser prologueParser = new SparqlPrologueParserImpl(queryParser);

        Prologue prologue = prologueParser.apply(prologueStr);

        Fragment2 result = create(elementStr, sourceVarName, targetVarName, prologue);
        return result;
    }

    public static Fragment2 create(String elementStr, String sourceVarName,
            String targetVarName, Prologue prologue) {
        SparqlElementParser parser = SparqlElementParserImpl.create(Syntax.syntaxARQ, prologue);
        Fragment2 result = create(elementStr, sourceVarName, targetVarName, parser);
        return result;
    }

    public static Fragment2 create(String elementStr, String sourceVarName,
            String targetVarName, Function<String, ? extends Element> elementParser) {
        Var sourceVar = Var.alloc(sourceVarName);
        Var targetVar = Var.alloc(targetVarName);

        String tmp = elementStr.trim();
        boolean isEnclosed = tmp.startsWith("{") && tmp.endsWith("}");
        if (!isEnclosed) {
            tmp = "{" + tmp + "}";
        }

        Element element = elementParser.apply(tmp);//ParserSPARQL10.parseElement(tmp);

        // TODO Find a generic flatten routine
        if (element instanceof ElementGroup) {
            ElementGroup group = (ElementGroup) element;
            List<Element> elements = group.getElements();
            if (elements.size() == 1) {
                element = elements.get(0);
            }
        }

        Fragment2 result = new Fragment2Impl(element, sourceVar, targetVar);

        return result;
    }

    /**
     * Return all vars that are neither source nor target
     * @return
     */
    public Set<Var> getInnerVars() {
        Set<Var> result = SetUtils.asSet(PatternVars.vars(element));
        result.remove(sourceVar);
        result.remove(targetVar);
        return result;
    }

//    public Set<Var> getVarsMentioned() {
//        Set<Var> result = SetUtils.asSet(PatternVars.vars(element));
//        result.add(sourceVar);
//        result.add(targetVar);
//        return result;
//    }

    public Fragment2 applyNodeTransform(NodeTransform nodeTransform) {
        Var s = VarUtils.applyNodeTransform(sourceVar, nodeTransform);
        Var t = VarUtils.applyNodeTransform(targetVar, nodeTransform);
        Element e = ElementUtils.applyNodeTransform(element, nodeTransform);

        Fragment2 result = new Fragment2Impl(e, s, t);
        return result;
    }

    public static Fragment2 create(org.apache.jena.sparql.path.Path path) {
        Fragment2 result = new Fragment2Impl(ElementUtils.createElement(new TriplePath(Vars.s, path, Vars.o)), Vars.s, Vars.o);
        return result;
    }

    public static Fragment2 create(Node p) {
        Fragment2 result = new Fragment2Impl(ElementUtils.createElement(new Triple(Vars.s, p, Vars.o)), Vars.s, Vars.o);
        return result;
    }

    public static Fragment2 create(Resource p) {
        Fragment2 result = create(p.asNode());
        return result;
    }

    public static Fragment2 create(String p) {
        Fragment2 result = create(NodeFactory.createURI(p));
        return result;
    }

    /**
     * Create a relation ?s ?o | ?s p ?o
     *
     * @param s
     * @param p
     * @param o
     * @return
     */
    public static Fragment2 createFwd(Var s, Node p, Var o) {
        Fragment2 result = new Fragment2Impl(ElementUtils.createElementTriple(s, p, o), s, o);
        return result;
    }

    /**
     * Create a relation ?o ?s | ?s p ?o
     *
     * @param s
     * @param p
     * @param o
     * @return
     */
    public static Fragment2 createBwd(Var s, Node p, Var o) {
        Fragment2 result = new Fragment2Impl(ElementUtils.createElementTriple(s, p, o), o, s);
        return result;
    }

    /**
     *
     * @param s
     * @param p
     * @param o
     * @return
     */
    public static Fragment2 create(Var s, Node p, Var o, boolean isFwd) {
        Fragment2 result = isFwd
                ? createFwd(s, p, o)
                : createBwd(s, p, o);

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        result = prime * result
                + ((sourceVar == null) ? 0 : sourceVar.hashCode());
        result = prime * result
                + ((targetVar == null) ? 0 : targetVar.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Fragment2Impl other = (Fragment2Impl) obj;
        if (element == null) {
            if (other.element != null)
                return false;
        } else if (!element.equals(other.element))
            return false;
        if (sourceVar == null) {
            if (other.sourceVar != null)
                return false;
        } else if (!sourceVar.equals(other.sourceVar))
            return false;
        if (targetVar == null) {
            if (other.targetVar != null)
                return false;
        } else if (!targetVar.equals(other.targetVar))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String result = sourceVar + " " + targetVar + " | " + element;
        return result;
        /*
        return "Relation [sourceVar=" + sourceVar + ", targetVar=" + targetVar
                + ", element=" + element + "]";
         */
    }

    public static Fragment2 empty() {
        return new Fragment2Impl(new ElementGroup(), Vars.s, Vars.s);
    }

    public static Fragment2 empty(Var var) {
        return new Fragment2Impl(new ElementGroup(), var, var);
    }
}
