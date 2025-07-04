package org.aksw.jenax.sparql.fragment.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorBlacklist;
import org.aksw.jenax.arq.util.expr.ExprListUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.syntax.VarExprListUtils;
import org.aksw.jenax.arq.util.triple.Triples;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.vocabulary.RDF;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;

public class ConceptUtils {
    public static final Fragment1 subjectConcept = createSubjectConcept();

    public static Fragment1 listDeclaredProperties = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> || ?t = <http://www.w3.org/2002/07/owl#ObjectProperty> || ?t = <http://www.w3.org/2002/07/owl#DataTypeProperty>)", "s");
    public static Fragment1 listDeclaredClasses = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/2000/01/rdf-schema#Class> || ?t = <http://www.w3.org/2002/07/owl#Class>)", "s");
    public static Fragment1 listUsedClasses = Concept.create("?s a ?t", "t");

    public static Fragment1 listAllPredicates = Concept.create("?s ?p ?o", "p");
    public static Fragment1 listAllGraphsLegacy = Concept.create("Graph ?g { ?s ?p ?o }", "g");
    public static Fragment1 listAllGraphs = Concept.create("Graph ?g { }", "g");


    /**
     * Takes a concept and returns a new one that matches the original one's outgoing predicates
     *
     * @return
     */
    public static Fragment1 createPredicateQuery(Fragment1 concept) {
        Collection<Var> vars = PatternVars.vars(concept.getElement());
        //List<String> varNames = VarUtils.getVarNames(vars);

        Var s = concept.getVar();

        Generator<Var> gen = GeneratorBlacklist.create(VarGeneratorImpl2.create("v"), vars);
        Var p = Var.alloc(gen.next());
        Var o = Var.alloc(gen.next());


        Triple triple = Triple.create(s, p, o);

        BasicPattern bp = new BasicPattern();
        bp.add(triple);

        List<Element> elements;
        if(concept.isSubjectConcept()) {
            elements = new ArrayList<Element>();
        } else {
            elements = concept.getElements();
        }
        elements.add(new ElementTriplesBlock(bp));

        Fragment1 result = new Concept(elements, p);

        return result;
    }
    /**
     * True if the concept is isomorph to { ?s ?p ?o }, ?s
     *
     * @return
     */
    public static boolean isSubjectConcept(Fragment1 r) {
        Element element = r.getElement();
        Var var = r.getVar();

        if(element instanceof ElementTriplesBlock) {
            List<Triple> triples = ((ElementTriplesBlock)element).getPattern().getList();

            if(triples.size() == 1) {

                Triple triple = triples.get(0);

                // TODO Refactor into e.g. ElementUtils.isVarsOnly(element)
                boolean condition =
                        triple.getSubject().isVariable() &&
                        triple.getSubject().equals(var) &&
                        triple.getPredicate().isVariable() &&
                        triple.getObject().isVariable();

                if(condition) {
                    return true;
                }
            }
        }

        return false;
    }



    /**
     * Create a new concept that has no variables with the given one in common
     *
     *
     *
     * @param that
     * @return
     */
    public static Fragment1 makeDistinctFrom(Fragment1 a, Fragment1 that) {

        Set<String> thisVarNames = new HashSet<String>(VarUtils.getVarNames(PatternVars.vars(a.getElement())));
        Set<String> thatVarNames = new HashSet<String>(VarUtils.getVarNames(PatternVars.vars(that.getElement())));

        Set<String> commonVarNames = Sets.intersection(thisVarNames, thatVarNames);
        Set<String> combinedVarNames = Sets.union(thisVarNames, thatVarNames);

        Generator<Var> generator = GeneratorBlacklist.create(VarGeneratorImpl2.create("v"), combinedVarNames);

        BindingBuilder builder = BindingBuilder.create();
        for(String varName : commonVarNames) {
            Var oldVar = Var.alloc(varName);
            Var newVar = Var.alloc(generator.next());

            builder.add(oldVar, newVar);
        }
        Binding binding = builder.build();


        Op op = Algebra.compile(a.getElement());
        Op substOp = Substitute.substitute(op, binding);

        Element newElement;
        if(substOp instanceof OpBGP) {
            BasicPattern bp = ((OpBGP)substOp).getPattern();
            newElement = new ElementTriplesBlock(bp);
        } else {
            Query tmp = OpAsQuery.asQuery(substOp);
            newElement = tmp.getQueryPattern();
        }
        //ElementGroup newElement = new ElementGroup();
        //newElement.addElement(tmp.getQueryPattern());

        /*
        if(newElement instanceof ElementGroup) {


            ElementPathBlock) {
        }
            List<TriplePath> triplePaths = ((ElementPathBlock)newElement).getPattern().getList();

            ElementTriplesBlock block = new ElementTriplesBlock();
            for(TriplePath triplePath : triplePaths) {
                block.addTriple(triplePath.asTriple());
            }

            newElement = block;
            //newElement = new ElementTriplesBlock(pattern);
        }
        */

        Var tmpVar = (Var)binding.get(a.getVar());

        Var newVar = tmpVar != null ? tmpVar : a.getVar();

        Fragment1 result = new Concept(newElement, newVar);
        return result;
    }

    public static Fragment1 createConcept(Node ... nodes) {
        Fragment1 result = createConcept(Arrays.asList(nodes));
        return result;
    }

    public static Fragment1 createConceptFromRdfNodes(Iterable<? extends RDFNode> rdfNodes) {
        Iterable<Node> nodes = Streams.stream(rdfNodes).map(RDFNode::asNode).collect(Collectors.toList());
        Fragment1 result = ConceptUtils.createConcept(nodes);
        return result;
    }

    public static Fragment1 createConcept(Iterable<? extends Node> nodes) {
        ElementData data = new ElementData();
        data.add(Vars.s);
        for(Node node : nodes) {
            Binding binding = BindingFactory.binding(Vars.s, node);
            data.add(binding);
        }

        Fragment1 result = new Concept(data, Vars.s);
        return result;

    }

    public static Fragment1 createFilterConcept(Node ... nodes) {
        Fragment1 result = createFilterConcept(Arrays.asList(nodes));
        return result;
    }


    public static Fragment1 createFilterConcept(Iterable<Node> nodes) {

        int size = Iterables.size(nodes);
        Element el;
        switch(size) {
        case 0:
            el = new ElementFilter(NodeValue.FALSE);
            break;
        case 1:
            Node node = nodes.iterator().next();
            el = new ElementFilter(new E_Equals(new ExprVar(Vars.s), NodeValue.makeNode(node)));
            break;
        default:
            el = new ElementFilter(new E_OneOf(new ExprVar(Vars.s), ExprListUtils.nodesToExprs(nodes)));
            break;
        }

        Fragment1 result = new Concept(el, Vars.s);
        return result;
    }

    public static Fragment1 createRelatedConcept(Collection<Node> nodes, Fragment2 relation) {
        Var sourceVar = relation.getSourceVar();
        Var targetVar = relation.getTargetVar();
        Element relationEl = relation.getElement();

        ExprVar ev = new ExprVar(sourceVar);
        ExprList el = ExprListUtils.nodesToExprs(nodes);
        ElementFilter filterEl = new ElementFilter(new E_OneOf(ev, el));

        Element resultEl = ElementUtils.mergeElements(relationEl, filterEl);

        Fragment1 result = new Concept(resultEl, targetVar);
        return result;
    }


    public static Fragment1 getRelatedConcept(Fragment1 source, Fragment2 relation) {
        Fragment1 renamedSource = createRenamedSourceConcept(source, relation);

        Element merged = ElementUtils.mergeElements(renamedSource.getElement(), relation.getElement());

        Var targetVar = relation.getTargetVar();

        Fragment1 result = new Concept(merged, targetVar);
        return result;
    }

    // FIMXE Consolidate with QueryGenerationUtils.createQuryCount
    public static Query createQueryCount(Fragment1 concept, Var outputVar, Long itemLimit, Long rowLimit) {
        Query subQuery = createQueryList(concept);

        if(rowLimit != null) {
            subQuery.setDistinct(false);
            subQuery.setLimit(rowLimit);

            subQuery = QueryGenerationUtils.wrapAsSubQuery(subQuery, concept.getVar());
            subQuery.setDistinct(true);
        }

        if(itemLimit != null) {
            subQuery.setLimit(itemLimit);
        }

        Element esq = new ElementSubQuery(subQuery);

        Query result = new Query();
        result.setQuerySelectType();

        Expr ea = result.allocAggregate(new AggCount());

        result.getProject().add(outputVar, ea);//new ExprAggregator(concept.getVar(), new AggCount()));
        result.setQueryPattern(esq);

        return result;
    }

    public static Set<Var> getVarsMentioned(Fragment1 concept) {
        Collection<Var> tmp = PatternVars.vars(concept.getElement());
        Set<Var> result = SetUtils.asSet(tmp);
        return result;
    }

    public static Fragment1 createSubjectConcept() {
        ElementTriplesBlock e = new ElementTriplesBlock();
        e.addTriple(Triples.spo);
        Fragment1 result = new Concept(e, Vars.s);
        return result;
    }

    /** Can be used for joining with empty patterns / substitution of variables */
    public static Fragment1 empty(Var var) {
        Fragment1 result = new Concept(new ElementGroup(), var);
        return result;
    }

    public static Fragment1 createForRdfType(String iriStr) {
        return createForRdfType(NodeFactory.createURI(iriStr));
    }

    public static Fragment1 createForRdfType(Node type) {
        Fragment1 result = new Concept(
                ElementUtils.createElementTriple(Vars.s, RDF.Nodes.type, type),
                Vars.s);
        return result;
    }

    public static Fragment1 createForSubjectsOfPredicate(String iriStr) {
        return createForSubjectsOfPredicate(NodeFactory.createURI(iriStr));
    }

    public static Fragment1 createForSubjectsOfPredicate(Node predicate) {
        Fragment1 result = new Concept(
                ElementUtils.createElementTriple(Vars.s, predicate, Vars.o),
                Vars.s);
        return result;
    }


    public static Map<Var, Var> createDistinctVarMap(Set<Var> workload, Set<Var> blacklist, Generator<Var> generator) {
        //Set<Var> varNames = new HashSet<String>(VarUtils.getVarNames(blacklist));
//        Generator<Var> gen = VarGeneratorBlacklist.create(generator, blacklist);

        Map<Var, Var> result = new HashMap<Var, Var>();
        for(Var var : workload) {
            boolean isBlacklisted = blacklist.contains(var);

            Var t;
            if(isBlacklisted) {
                t = generator.next();
                //t = Var.alloc(name);
            } else {
                t = var;
            }

            result.put(var, t);
        }

        return result;
    }

    /**
     * Creates a generator that does not yield variables part of the concept (at the time of creation)
     * @param concept
     * @return
     */
    public static Generator<Var> createGenerator(Fragment1 concept) {
        Collection<Var> tmp = PatternVars.vars(concept.getElement());
        //List<String> varNames = VarUtils.getVarNames(tmp);

        //Generator base = Gensym.create("v");
        Generator<Var> result = VarGeneratorBlacklist.create("v", tmp);

        return result;
    }

    // Create a fresh var that is not part of the concept
//    public static Var freshVar(Fragment1 concept) {
//        Generator gen = createGenerator(concept);
//        String varName = gen.next();
//        Var result = Var.alloc(varName);
//        return result;
//    }

    public static Fragment1 renameVar(Fragment1 concept, Var targetVar) {

        Fragment1 result;
        if(concept.getVar().equals(targetVar)) {
            // Nothing to do since we are renaming the variable to itself
            result = concept;
        } else {
            // We need to rename the concept's var, thereby we need to rename
            // any occurrences of targetVar
            Set<Var> conceptVars = getVarsMentioned(concept);
            Map<Var, Var> varMap = createDistinctVarMap(conceptVars, Collections.singleton(targetVar), VarGeneratorImpl2.create("v"));
            varMap.put(concept.getVar(), targetVar);
            Element replElement = ElementUtils.createRenamedElement(concept.getElement(), varMap);
            Var replVar = varMap.get(concept.getVar());
            result = new Concept(replElement, replVar);
        }

        return result;
    }

    /**
     * Select Distinct ?g { Graph ?g { ?s ?p ?o } }
     *
     * @return
     */
    /*
    public static Fragment1 listGraphs() {

        Triple triple = new Triple(Vars.s, Vars.p, Vars.o);
        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);


        ElementGroup group = new ElementGroup();
        group.addTriplePattern(triple);

        ElementNamedGraph eng = new ElementNamedGraph(Vars.g, group);

        Fragment1 result = new Concept(eng, Vars.g);
        return result;
    }
    */



    public static Map<Var, Var> createVarMap(Fragment1 attrConcept, Fragment1 filterConcept) {
        Element attrElement = attrConcept.getElement();
        Element filterElement = filterConcept.getElement();

        Collection<Var> attrVars = PatternVars.vars(attrElement);
        Collection<Var> filterVars = PatternVars.vars(filterElement);

        List<Var> attrJoinVars = Collections.singletonList(attrConcept.getVar());
        List<Var> filterJoinVars = Collections.singletonList(filterConcept.getVar());


        Map<Var, Var> result = VarUtils.createJoinVarMap(attrVars, filterVars, attrJoinVars, filterJoinVars, null); //, varNameGenerator);

        return result;
    }

    /**
     *
     * @param concept The concept subject to renaming such that it can act as a filter on the relation's source variable
     * @param relation The relation; variables will remain unchanged
     * @return
     */
    public static Fragment1 createRenamedSourceConcept(Fragment1 concept, Fragment2 relation) {
        Fragment1 attrConcept = new Concept(relation.getElement(), relation.getSourceVar());
        Fragment1 result = createRenamedConcept(attrConcept, concept);
        return result;
    }

    public static Fragment1 createRenamedConcept(Fragment1 concept, Map<Var, Var> varMap) {
        Var newVar = MapUtils.getOrElse(varMap, concept.getVar(), concept.getVar());
        Element newElement = ElementUtils.createRenamedElement(concept.getElement(), varMap);

        Fragment1 result = new Concept(newElement, newVar);

        return result;
    }


    /**
     *
     *
     * @param attrConcept The concept whose attributes will remained unchanged
     * @param filterConcept The concept whose variables will be renamed such that it filters the attrConcept
     * @return
     */
    public static Fragment1 createRenamedConcept(Fragment1 attrConcept, Fragment1 filterConcept) {

        Map<Var, Var> varMap = createVarMap(attrConcept, filterConcept);

        Var attrVar = attrConcept.getVar();
        Element filterElement = filterConcept.getElement();
        Element newFilterElement = ElementUtils.createRenamedElement(filterElement, varMap);

        Fragment1 result = new Concept(newFilterElement, attrVar);

        return result;
    }

    public static Fragment1 createCombinedConcept(Fragment1 attrConcept, Fragment1 filterConcept, boolean renameVars, boolean attrsOptional, boolean filterAsSubquery) {
        // TODO Is it ok to rename vars here? // TODO The variables of baseConcept and tmpConcept must match!!!
        // Right now we just assume that.
        Var attrVar = attrConcept.getVar();
        Var filterVar = filterConcept.getVar();

        if(!filterVar.equals(attrVar)) {
            Map<Var, Var> varMap = new HashMap<Var, Var>();
            varMap.put(filterVar, attrVar);

            // If the attrVar appears in the filterConcept, rename it to a new variable
            Var distinctAttrVar = Var.alloc("cc_" + attrVar.getName());
            varMap.put(attrVar, distinctAttrVar);

            // TODO Ensure uniqueness
            //filterConcept.getVarsMentioned();
            //attrConcept.getVarsMentioned();
            // VarUtils.freshVar('cv', );  //

            filterConcept = createRenamedConcept(filterConcept, varMap);
        }

        Fragment1 tmpConcept;
        if(renameVars) {
            tmpConcept = createRenamedConcept(attrConcept, filterConcept);
        } else {
            tmpConcept = filterConcept;
        }


        List<Element> tmpElements = tmpConcept.getElements();


        // Small workaround (hack) with constraints on empty paths:
        // In this case, the tmpConcept only provides filters but
        // no triples, so we have to include the base concept
        //var hasTriplesTmp = tmpConcept.hasTriples();
        //hasTriplesTmp &&
        Element attrElement = attrConcept.getElement();

        Element e;
        if(!tmpElements.isEmpty()) {

            if(tmpConcept.isSubjectConcept()) {
                e = attrConcept.getElement(); //tmpConcept.getElement();
            } else {

                List<Element> newElements = new ArrayList<Element>();

                if(attrsOptional) {
                    attrElement = new ElementOptional(attrConcept.getElement());
                }
                newElements.add(attrElement);

                if(filterAsSubquery) {
                    tmpElements = Collections.<Element>singletonList(new ElementSubQuery(tmpConcept.asQuery()));
                }


                newElements.addAll(tmpElements);
                //newElements.push.apply(newElements, attrElement);
                //newElements.push.apply(newElements, tmpElements);


                e = ElementUtils.createElementGroup(newElements);
                //xxx e = e.flatten();
            }
        } else {
            e = attrElement;
        }

        Fragment1 result = new Concept(e, attrVar);

        return result;
    }


    public static boolean isGroupedOnlyByVar(Query query, Var groupVar) {
        boolean result = false;

        boolean hasOneGroup = query.getGroupBy().size() == 1;
        if(hasOneGroup) {
            Expr expr = query.getGroupBy().getExprs().values().iterator().next();
            if(expr instanceof ExprVar) {
                Var v = expr.asVar();

                result = v.equals(groupVar);
            }
        }

        return result;
    }

    public static boolean isDistinctConceptVar(Query query, Var conceptVar) {
        boolean isDistinct = query.isDistinct();

        Collection<Var> projectVars = query.getProjectVars();

        boolean hasSingleVar = !query.isQueryResultStar() && projectVars != null && projectVars.size() == 1;
        boolean result = isDistinct && hasSingleVar && projectVars.iterator().next().equals(conceptVar);
        return result;
    }

    public static boolean isConceptQuery(Query query, Var conceptVar) {
        boolean isDistinctGroupByVar = isGroupedOnlyByVar(query, conceptVar);
        boolean isDistinctConceptVar = isDistinctConceptVar(query, conceptVar);

        boolean result = isDistinctGroupByVar || isDistinctConceptVar;
        return result;
    }


    public static Query createQueryList(Fragment1 concept) {
        Query result = createQueryList(concept, null, null);
        return result;
    }

    public static Query createQueryList(OrderedConcept orderedConcept, Range<Long> range) {
        Fragment1 concept = orderedConcept.getConcept();
        Query result = createQueryList(concept, range);

        for(SortCondition sc : orderedConcept.getOrderBy()) {
            result.addOrderBy(sc);
        }

        return result;
    }

    public static Query createQueryList(OrderedConcept orderedConcept, Long limit, Long offset) {
        Fragment1 concept = orderedConcept.getConcept();
        Query result = createQueryList(concept, limit, offset);

        for(SortCondition sc : orderedConcept.getOrderBy()) {
            result.addOrderBy(sc);
        }

        return result;
    }


    public static Query createQueryList(Fragment1 concept, Range<Long> range) {
        long offset = QueryUtils.rangeToOffset(range);
        long limit = QueryUtils.rangeToLimit(range);

        Query result = createQueryList(concept, limit, offset);
        return result;
    }

    public static Query createQueryList(Fragment1 concept, Long limit, Long offset) {
        Query result = new Query();
        result.setQuerySelectType();
        result.setDistinct(true);

        result.setLimit(limit == null ? Query.NOLIMIT : limit);
        result.setOffset(offset == null ? Query.NOLIMIT : offset);

        result.getProject().add(concept.getVar());
        Element e = concept.getElement();
        if(e instanceof ElementSubQuery) {
            e = ElementUtils.createElementGroup(e);
        }

        result.setQueryPattern(e);

//        String str = result.toString();
//        System.out.println(str);
        return result;
    }


    public static Query createAttrQuery(Query attrQuery, Var attrVar, boolean isLeftJoin, Fragment1 filterConcept, Long limit, Long offset, boolean forceSubQuery) {

        //filterConcept.getElement()
        // TODO Deal with prefixes...

        Fragment1 attrConcept = new Concept(new ElementSubQuery(attrQuery), attrVar);

        Fragment1 renamedFilterConcept = ConceptUtils.createRenamedConcept(attrConcept, filterConcept);
        //console.log('attrConcept: ' + attrConcept);
        //console.log('filterConcept: ' + filterConcept);
        //console.log('renamedFilterConcept: ' + renamedFilterConcept);

        // Selet Distinct ?ori ?gin? alProj { Select (foo as ?ori ...) { originialElement} }

        // Whether each value for attrVar uniquely identifies a row in the result set
        // In this case, we just join the filterConcept into the original query
        boolean isAttrVarPrimaryKey = isConceptQuery(attrQuery, attrVar);
        //isAttrVarPrimaryKey = false;

        Query result;
        if(isAttrVarPrimaryKey) {
            // Case for e.g. Get the number of products offered by vendors in Europe
            // Select ?vendor Count(Distinct ?product) { ... }

            result = attrQuery.cloneQuery();

            Element se;
            if(forceSubQuery) {

                // Select ?s { attrElement(?s, ?x) filterElement(?s) }
                Query sq = new Query();
                sq.setQuerySelectType();
                sq.setDistinct(true);
                sq.getProject().add(attrConcept.getVar());
                sq.setQueryPattern(attrQuery.getQueryPattern());

                Element tmp = new ElementSubQuery(sq);

                Set<Var> refVars = VarExprListUtils.getRefVars(attrQuery.getProject());
                if(refVars.size() == 1 && attrVar.equals(refVars.iterator().next())) {
                    se = tmp;
                } else {
                    ElementGroup foo = new ElementGroup();
                    foo.addElement(attrQuery.getQueryPattern());
                    foo.addElement(tmp);
                    se = foo;
                }

            } else {
                se = attrQuery.getQueryPattern();
            }

            if(isLeftJoin) {
                se = new ElementOptional(se);
            }

            if(!renamedFilterConcept.isSubjectConcept()) {
                Element newElement = ElementUtils.createElementGroup(renamedFilterConcept.getElement(), se);
                //newElement = newElement.flatten();
                result.setQueryPattern(newElement);
            }

            result.setLimit(limit);
            result.setOffset(offset);
        } else {
            // Case for e.g. Get all products offered by some 10 vendors
            // Select ?vendor ?product { ... }

            // boolean requireSubQuery = limit != null || offset != null;
            boolean requireSubQuery = QueryUtils.hasLimit(limit) || QueryUtils.hasNonZeroOffset(offset);

            Element newFilterElement;
            if(requireSubQuery) {
                Fragment1 subConcept;
                if(isLeftJoin) {
                    subConcept = renamedFilterConcept;
                } else {
                    // If we do an inner join, we need to include the attrQuery's element in the sub query

                    Element subElement;
                    if(renamedFilterConcept.isSubjectConcept()) {
                        subElement = attrQuery.getQueryPattern();
                    } else {
                        subElement = ElementUtils.createElementGroup(attrQuery.getQueryPattern(), renamedFilterConcept.getElement());
                    }

                    subConcept = new Concept(subElement, attrVar);
                }

                Query subQuery = ConceptUtils.createQueryList(subConcept, limit, offset);
                newFilterElement = new ElementSubQuery(subQuery);
            }
            else {
                newFilterElement = renamedFilterConcept.getElement();
            }

//            var canOptimize = isAttrVarPrimaryKey && requireSubQuery && !isLeftJoin;
//
//            var result;
//
//            //console.log('Optimize: ', canOptimize, isAttrConceptQuery, requireSubQuery, isLeftJoin);
//            if(canOptimize) {
//                // Optimization: If we have a subQuery and the attrQuery's projection is only 'DISTINCT ?attrVar',
//                // then the subQuery is already the result
//                result = newFilterElement.getQuery();
//            } else {


            Query query = attrQuery.cloneQuery();

            Element attrElement = query.getQueryPattern();

            Element newAttrElement;
            if(!requireSubQuery && (filterConcept != null && filterConcept.isSubjectConcept())) {
                newAttrElement = attrElement;
            }
            else {
                if(isLeftJoin) {
                    newAttrElement = ElementUtils.createElementGroup(
                        newFilterElement,
                        new ElementOptional(attrElement)
                    );
                } else {
                    newAttrElement = ElementUtils.createElementGroup(
                        attrElement,
                        newFilterElement
                    );
                }
            }

            query.setQueryPattern(newAttrElement);
            result = query;
        }

        // console.log('Argh Query: ' + result, limit, offset);
        return result;
    }

    public static Var freshVar(Fragment1 concept) {
        Var result = freshVar(concept, null);
        return result;
    }

    public static Var freshVar(Fragment1 concept, String baseVarName) {
        baseVarName = baseVarName == null ? "c" : baseVarName;

        Set<Var> varsMentioned = concept.getVarsMentioned();

        Generator<Var> varGen = VarUtils.createVarGen(baseVarName, varsMentioned);
        Var result = varGen.next();

        return result;
    }

    public static Fragment1 createRenamedConcept(Fragment1 concept, Var attrVar) {
        Var newVar = freshVar(concept);
        Map<Var, Var> varMap = new HashMap<>();
        varMap.put(attrVar, newVar);
        varMap.put(concept.getVar(), attrVar);
        Fragment1 result = ConceptUtils.createRenamedConcept(concept, varMap);

//        Fragment1 tmp = createRenamedConcept(concept, Collections.singletonMap(attrVar, newVar));
//        Fragment1 result = ConceptUtils.createRenamedConcept(tmp, Collections.singletonMap(tmp.getVar(), attrVar));

        return result;
    }
}
