package org.aksw.jenax.sparql.fragment.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.expr.NodeValueUtils;
import org.aksw.jenax.arq.util.prologue.PrologueUtils;
import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.api.Fragment3;

import com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Conditional;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

import com.google.common.collect.Sets;

public class FragmentUtils {
    public static final Fragment3 SPO = new Fragment3Impl(
            ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o),
            Vars.s, Vars.p, Vars.o);


    /**
     * Create a relation using the variables ?s ?p ?o and adding filters
     * as needed for any concrete node
     *
     * @return
     */
    public static Fragment3 createTernaryRelation(Node s, Node p, Node o) {
        List<Expr> exprs = new ArrayList<>(3);
        List<Node> nodes = Arrays.asList(s, p, o);

        for (int i = 0; i < 3; ++i) {
            Node n = nodes.get(i);
            if (n.isConcrete()) {
                Var v = Vars.spo.get(i);

                exprs.add(new E_Equals(new ExprVar(v), NodeValue.makeNode(n)));
            }
        }

        Element elt = ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o);

        if (!exprs.isEmpty()) {
            Expr expr = ExprUtils.andifyBalanced(exprs);
            elt = ElementUtils.createElementGroup(elt, new ElementFilter(expr));
        }

        return new Fragment3Impl(elt,
                Vars.s, Vars.p, Vars.o);
    }

    /**
     * Rename the variables of the relation to the given variables
     * In case of clashes, prior variables will be replaced with fresh ones.
     *
     * @param r
     * @param targetVars
     * @return
     */
    public static Fragment rename(Fragment r, List<Var> targetVars) {
        List<Var> rVars = r.getVars();
        Map<Var, Node> map = createRenameVarMap(r.getVarsMentioned(), rVars, targetVars);

        Fragment result = r.applyNodeTransform(new NodeTransformSubst(map));

        return result;
    }


    /**
     * Rename the vars of the relation to the given target variables.
     * Thereby take care of conflicts when the target variable also is also mentioned in the relation
     * The implementation uses Relation.join() which treats the variables of the left-hand side
     * of the join as fixed.
     *
     *
     * @param r
     * @param targetNodes A list of vars (TODO Change type to var)
     * @return
     */
    public static Element renameNodes(Fragment r, List<? extends Node> targetNodes) {
        List<Var> tgtVars = targetNodes.stream().map(v -> (Var)v).collect(Collectors.toList());

        // Create a relation with an empty pattern from the target nodes
        Fragment joined = new FragmentImpl(new ElementGroup(), tgtVars)
            .joinOn(tgtVars)
            .with(r);
        Element result = joined.getElement();

//		if(false) {
//			List<Var> rVars = r.getVars();
//			Element e = r.getElement();
//			Map<Var, Node> map = createRenameVarMap(r.getVarsMentioned(), rVars, targetNodes);
//
//			Element result = ElementUtils.applyNodeTransform(e, new NodeTransformSubst(map));
//		}
        return result;
    }

    public static Map<Var, Node> createRenameVarMap(Set<Var> mentionedVars, List<Var> rVars, List<? extends Node> targetNodes) {
        //Set<Var> rVars = ElementUtils.getMentionedVars(e);

        // Variables in the intersection of mentionedVars and targetNodes need to be renamed

        Set<Var> relationVars = new LinkedHashSet<>(rVars);
        Set<Node> vs = new LinkedHashSet<>(targetNodes);
        if(vs.size() != relationVars.size()) {
            throw new IllegalArgumentException("Number of distinct variables of the relation must match the number of distinct target variables");
        }

        Map<Var, Node> rename = Streams.zip(
            relationVars.stream(),
            vs.stream(),
            (a, b) -> new SimpleEntry<>(a, b))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));


        // Extend the map by renaming all remaining variables
        //Set<Var> mentionedVars = ElementUtils.getMentionedVars(e); //r.getVarsMentioned();
        Set<Var> remainingVars = Sets.difference(mentionedVars, relationVars);

        //Set<Var> forbiddenVars = Sets.union(vs, mentionedVars);
        Generator<Var> varGen = VarGeneratorBlacklist.create(remainingVars);

        Set<Var> targetVars = targetNodes.stream().filter(Node::isVariable).map(x -> (Var)x).collect(Collectors.toSet());
        // targetVars
        Map<Var, Var> map = VarUtils.createDistinctVarMap(targetVars, remainingVars, true, varGen);
        //map.putAll(rename);
        rename.putAll(map);

        return rename;
    }


    /**
     * Rename variables of all relations to the given list of variables
     * All relations and the list of given variables must have the same length
     *
     * @param relations
     * @return
     */
    public static Fragment align(Collection<? extends Fragment> relations, List<Var> vars) {
        List<Fragment> tmp = relations.stream()
                .map(r -> rename(r, vars))
                .collect(Collectors.toList());

        List<Element> es = tmp.stream()
                .map(Fragment::getElement)
                .collect(Collectors.toList());


        Element e = ElementUtils.unionIfNeeded(es);
        Fragment result = new FragmentImpl(e, vars);
        return result;
    }


    /**
     * Apply groupBy and count(Distinct ?var) to one of a relation's variables.
     *
     * @param r
     * @param aggVar
     * @param resultVar
     * @param includeAbsent if true, unbound values count too
     * @return
     */
    public static Fragment groupBy(Fragment r, Var aggVar, Var resultVar, boolean includeAbsent) {
        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryPattern(r.getElement());

        ExprVar ev = new ExprVar(aggVar);

        Expr e = includeAbsent
                ? new E_Conditional(new E_Bound(ev), ev, NodeValueUtils.NV_ABSENT)
                : ev;
        Expr tmp = query.allocAggregate(new AggCountVarDistinct(e));

        List<Var> vars = r.getVars();

        // Add all other vars as group vars
        List<Var> groupVars = vars.stream()
                .filter(v -> !aggVar.equals(v))
                .collect(Collectors.toList());

        query.addProjectVars(groupVars);
        query.getProject().add(resultVar, tmp);

        List<Var> newVars = new ArrayList<>(groupVars);
        newVars.add(resultVar);

        for(Var groupVar : groupVars) {
            query.addGroupBy(groupVar);
        }

        Fragment result = new FragmentImpl(new ElementSubQuery(query), newVars);
        return result;
    }


//    public static Relation createRelationRenamed(Relation prototype, Relation target) {
//        RelationUtils.create
//
//        Set<Var> allowed = new HashSet<Var>(Arrays.asList(sourceVar, targetVar));
//        Set<Var> bl = Sets.difference(blacklist, allowed);
//
//        Generator<Var> gen = VarGeneratorBlacklist.create("v", bl);
//
//        Set<Var> sourceVars = relation.getVarsMentioned();
//        Map<Var, Var> varMap = VarUtils.createJoinVarMap();
//
//    }

    public static Fragment fromQuery(String queryStr) {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefixes(PrefixMapping.Extended);
        return fromQuery(queryStr, pm);
    }

    public static Fragment fromQuery(String queryStr, PrefixMapping prefixMapping) {
        Query query = new Query();
        query.setPrefixMapping(prefixMapping);
        // TODO Make parser configurable
        // SPARQLParser parser = new ParserSPARQL11();
        QueryFactory.parse(query, queryStr, "http://www.example.org/base/", Syntax.syntaxARQ);

        // parser.parse(query, queryStr);

        Fragment result = fromQuery(query);
        return result;
    }

    public static Fragment fromQuery(Query query) {
        Fragment result;
        if(query.isSelectType()) {
            List<Var> vars = query.getProjectVars();

            boolean needsWrapping = QueryGenerationUtils.needsWrappingByFeatures(query);
            Element element = needsWrapping
                    ? new ElementSubQuery(query)
                    : query.getQueryPattern();

            result = new FragmentImpl(element, vars);
        } else if(query.isConstructType()) {
            Template template = query.getConstructTemplate();
            List<Var> vars = new ArrayList<>(QuadPatternUtils.getVarsMentioned(template.getQuads()));
            Element element = query.getQueryPattern();
            result = new FragmentImpl(element, vars);
        } else {

            throw new RuntimeException("SELECT or CONSTRUCT query form expected, instead got " + query);
        }

        return result;
    }



    public static Triple extractTriple(Fragment2 relation) {
        Element e = relation.getElement();
        Triple result = ElementUtils.extractTriple(e);
        return result;
    }

//    public static Relation union(Relation a, Relation b, boolean transformInPlaceIfApplicable) {
//    	Relation result = addUnionMember(a, b, false);
//    	return result;
//    }

//    public static Relation concat(Relation a, Relation b, boolean transformInPlaceIfApplicable) {
//
//    }

    public static Fragment2 and(Fragment2 a, Fragment2 b, boolean transformInPlaceIfApplicable) {
        Element ae = a.getElement();
        Element be = b.getElement();

        Collection<Var> vas = PatternVars.vars(ae);
        Collection<Var> vbs = PatternVars.vars(be);
        Map<Var, Var> varMap = VarUtils.createDistinctVarMap(vas, vbs, true, null);

        varMap.put(b.getSourceVar(), a.getTargetVar());
        Element ce = ElementUtils.createRenamedElement(be, varMap);

        ElementGroup eg;
        boolean isInPlace = ae instanceof ElementGroup && transformInPlaceIfApplicable;
        if(isInPlace) {
            eg = (ElementGroup)ae;
        } else {
            eg = new ElementGroup();
            eg.addElement(ae);
        }
        eg.addElement(ce);;

        Fragment2 result = new Fragment2Impl(eg, a.getSourceVar(), varMap.getOrDefault(b.getTargetVar(), a.getSourceVar()));

        return result;
    }


    /**
     *
     *
     * @param a
     * @param b
     * @param transformInPlaceIfApplicable Add 'b' to to 'a' if a's element already is a union
     * @return
     */
    public static Fragment2 union(Fragment2 a, Fragment2 b, boolean transformInPlaceIfApplicable) {
        Element ae = a.getElement();

        ElementUnion u;
        boolean isInPlace;
        if(transformInPlaceIfApplicable && a.getElement() instanceof ElementUnion) {
            u = (ElementUnion)ae;
            isInPlace = true;
        } else {
            u = new ElementUnion();
            u.addElement(a.getElement());
            isInPlace = false;
        }


        Map<Var, Var> varMap = new HashMap<>();

        Collection<Var> vas = PatternVars.vars(a.getElement());
        Collection<Var> vbs = PatternVars.vars(b.getElement());
        VarUtils.createDistinctVarMap(vbs, vas, true, null);

        varMap.put(b.getSourceVar(), a.getSourceVar());
        varMap.put(b.getTargetVar(), a.getTargetVar());
        Element c = ElementUtils.createRenamedElement(b.getElement(), varMap);
        u.addElement(c);

        Fragment2 result = isInPlace ? a : new Fragment2Impl(u, a.getSourceVar(), a.getTargetVar());
        return result;
    }

    public static Fragment2 createRelation(String propertyUri, boolean isInverse, PrefixMapping prefixMapping) {

        String p = prefixMapping == null ? propertyUri : prefixMapping.expandPrefix(propertyUri);
        Node node = NodeFactory.createURI(p);
        Fragment2 result = createRelation(node, isInverse);
        return result;
    }


    public static Fragment2 createRelation(Node property, boolean isInverse) {

        //Expr expr = new E_Equals(new ExprVar(Vars.p), ExprUtils.nodeToExpr(property));

        Triple t = isInverse
                ? Triple.create(Vars.o, property, Vars.s)
                : Triple.create(Vars.s, property, Vars.o);

        Element element = ElementUtils.createElement(t);
        //Element element = new ElementTriplesBlock(bgp);
        Fragment2 result = new Fragment2Impl(element, Vars.s, Vars.o);//createRelation(expr, isInverse);
        return result;
    }

    public static Fragment2 createRelation(Property property, boolean isInverse) {
        Fragment2 result = createRelation(property.asNode(), isInverse);
        return result;
    }


    public static Fragment2 createRelation(Expr expr, boolean isInverse) {
        Fragment2 result = new Fragment2Impl(new ElementFilter(expr), Vars.p, Vars.o);
        return result;
    }


//    public static Relation createRelation(StepRelation step) {
//        Relation result = nav(step.getRelation(), step.isInverse());
//        return result;
//    }


    public static Query createQuery(Fragment relation) {
        // If the element is already a query, just limit the projection
        Element e = relation.getElement();
        List<Var> vars = relation.getVars();

        Query result;
        if(e instanceof ElementSubQuery) {
            result = ((ElementSubQuery)e).getQuery().cloneQuery();

            // Update the projection
            Set<Var> removals = new HashSet<>(result.getProject().getVars());
            removals.removeAll(vars);

            VarExprList project = result.getProject();
            removals.forEach(project::remove);

        } else {
            Prologue prologue = PrologueUtils.newPrologueAsGiven();
            result = new Query(prologue);
            result.setQuerySelectType();

            result.setQueryPattern(e);

            VarExprList project = result.getProject();
            vars.forEach(project::add);
        }

        return result;
    }
}
