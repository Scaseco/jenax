package org.aksw.jena_sparql_api.shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptOps;
import org.aksw.jena_sparql_api.core.LookupServiceUtils;
import org.aksw.jenax.analytics.core.MappedConcept;
import org.aksw.jenax.arq.aggregation.Agg;
import org.aksw.jenax.arq.aggregation.AggDatasetGraph;
import org.aksw.jenax.arq.aggregation.AggGraph;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.arq.util.triple.Triples;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


/**
 * How to deal with inserts?
 *
 * For example, the shape is: get all buyers (of dvds) togther with their (immediate friends), thus:
 *
 * Nav(Out(hasBuyer), Out(hasFriend))
 *
 *
 * Whenever we add triples that were not part of the original working set,
 * we could just add them to the store (it does not matter whether they already existed there or not).
 *
 * Yet, it would be very useful to validate whether newly inserted triples are part of a working set or not.
 *
 *
 *
 *
 * Whenever we delete triples from the working set, well, its a delete
 *
 *
 * If we added (EvilDead hasBuyer Alice),
 * then we know that this triple is real addition, because
 * - the subject equals the source resource
 * - the triple matches one of the immediate relations of the shape
 * - the triple was not part of the shape's graph
 *
 * However, if we added that triple, we might want to fetch data for Alice according to the shape.
 *
 *
 *
 */

//class Target {
//    private Concept concept;
//}

/**
 * A graph expression is a SPARQL expression which only makes use of the variables ?s ?p and ?o
 * The evaluation of a graph expression te over an RDF dataset D yields a graph (a set of triples)
 *
 * [[te]]_D
 *
 *
 * Maybe we should take a set of triples, derive a set of related resources,
 * and then for this related set of resources specify which triples we are interested in again.
 *
 * In this case, the model is Map<Expr, TripleTree>
 * i.e. the expression evaluates to a resource for each triple, and these resources are used for further lookups.
 * However, how would we navigate in inverse direction?
 *
 * Each resource r is associated with a set of ingoing and outgoing triples:
 *
 * outgoing: { t | t in G and t.s = r}
 * ingoing: { t | t in G and t.o = r}
 *
 *
 *
 *
 * Navigating to the set of related triples:
 * Let G be the set of all triples and G1 and G2 subsetof G be two graphs.
 * { t2 | t1 in G1 and expr(t1, t2)}
 *
 *
 * We could use expressions over the additional variables ?x ?y ?z to perform joins:
 * (?s ?p ?o ?x ?y ?z)
 *
 * ?s = ?x
 *
 *
 * G1 x G2
 *
 *
 * @author raven
 *
 */
public class ResourceShape {

    private static final Logger logger = LoggerFactory.getLogger(ResourceShape.class);

    private Map<BinaryRelation, ResourceShape> out = new HashMap<BinaryRelation, ResourceShape>();
    private Map<BinaryRelation, ResourceShape> in = new HashMap<BinaryRelation, ResourceShape>();

    private ConceptExpr expr;

    public boolean isEmpty() {
        boolean result = out.isEmpty() && in.isEmpty();
        return result;
    }

    public Map<BinaryRelation, ResourceShape> getOutgoing() {
        return out;
    }

    public Map<BinaryRelation, ResourceShape> getIngoing() {
        return in;
    }

    public void extend(ResourceShape that) {
        // TODO Maybe we should create a deep clone of 'that' first
        this.out.putAll(that.out);
        this.in.putAll(that.in);
    }


    public static List<Concept> collectConcepts(ResourceShape source, boolean includeGraph) {
        List<Concept> result = new ArrayList<Concept>();
        collectConcepts(result, source, includeGraph);
        return result;
    }

    public static void collectConcepts(Collection<Concept> result, ResourceShape source, boolean includeGraph) {
        Generator<Var> vargen = VarGeneratorImpl2.create("v");

        collectConcepts(result, source, vargen, includeGraph);
    }

    public static void collectConcepts(Collection<Concept> result, ResourceShape source, Generator<Var> vargen, boolean includeGraph) {
        // Concept baseConcept = new Concept(null, Vars.x);
        Concept baseConcept = new Concept((Element)null, Vars.x);
        collectConcepts(result, baseConcept, source, vargen, includeGraph);
    }

    public static void collectConcepts(Collection<Concept> result, Concept baseConcept, ResourceShape source, Generator<Var> vargen, boolean includeGraph) {

        Map<BinaryRelation, ResourceShape> outgoing = source.getOutgoing();
        Map<BinaryRelation, ResourceShape> ingoing = source.getIngoing();

        collectConcepts(result, baseConcept, outgoing, false, vargen, includeGraph);
        collectConcepts(result, baseConcept, ingoing, true, vargen, includeGraph);

        //collectConcepts(result, null, source,);
    }

    public static void collectConcepts(Collection<Concept> result, Concept baseConcept, Map<BinaryRelation, ResourceShape> map, boolean isInverse, Generator<Var> vargen, boolean includeGraph) {

//        Var baseVar = baseConcept.getVar();

        {
            Set<BinaryRelation> raw = map.keySet();
            Collection<BinaryRelation> opt = group(raw);

            for(BinaryRelation relation : opt) {
                //Concept sc = new Concept(relation.getElement(), baseVar);
                Concept sc = baseConcept;
                Concept item = createConcept(sc, vargen, relation, isInverse, includeGraph);
                result.add(item);
            }
        }


        Multimap<ResourceShape, BinaryRelation> groups = HashMultimap.create();

        for(Entry<BinaryRelation, ResourceShape> entry : map.entrySet()) {
            groups.put(entry.getValue(), entry.getKey());
        }

        for(Entry<ResourceShape, Collection<BinaryRelation>> group : groups.asMap().entrySet()) {
            ResourceShape target = group.getKey();
            Collection<BinaryRelation> raw = group.getValue();

            Collection<BinaryRelation> opt = group(raw);


            for(BinaryRelation relation : opt) {
                //Concept sc = new Concept(relation.getElement(), baseVar);
                Concept sc = baseConcept;

                Concept item = createConcept(sc, vargen, relation, isInverse, includeGraph);

                //result.add(item);

                // Map the

                // Now use the concept as a base for its children
                collectConcepts(result, item, target, vargen, includeGraph);
            }


        }
    }


    public static List<BinaryRelation> group(Collection<BinaryRelation> relations) {
        List<BinaryRelation> result = new ArrayList<BinaryRelation>();


        Set<Node> concretePredicates = new HashSet<Node>();
        Set<Expr> simpleExprs = new HashSet<Expr>();

        // Find all relations that are simply ?p = expr
        for(BinaryRelation relation : relations) {
            Var s = relation.getSourceVar();
//            Var t = relation.getTargetVar();
            Element e = relation.getElement();

            if(e instanceof ElementFilter) {
                ElementFilter filter = (ElementFilter)e;
                Expr expr = filter.getExpr();
                Entry<Var, NodeValue> c = ExprUtils.extractConstantConstraint(expr);
                if(c != null && c.getKey().equals(s)) {
                    Node n = c.getValue().asNode();
                    concretePredicates.add(n);
                } else {
                    simpleExprs.add(expr);
                }
            } else {
                result.add(relation);
                //throw new RuntimeException("Generic re")
            }

        }

        if(!simpleExprs.isEmpty()) {
            Expr orified = ExprUtils.orifyBalanced(simpleExprs);
            BinaryRelation r = asRelation(orified);
            result.add(r);
        }

        if(!concretePredicates.isEmpty()) {
            ExprList exprs = new ExprList();
            for(Node node : concretePredicates) {
                Expr expr = org.apache.jena.sparql.util.ExprUtils.nodeToExpr(node);
                exprs.add(expr);
            }

            ExprVar ep = new ExprVar(Vars.p);
            Expr ex = exprs.size() > 1
                    ? new E_OneOf(ep, exprs)
                    : new E_Equals(ep, exprs.get(0));

            BinaryRelation r = asRelation(ex);
            result.add(r);
        }

        return result;
    }


    public static BinaryRelation asRelation(Expr expr) {
        ElementFilter e = new ElementFilter(expr);
        BinaryRelation result = new BinaryRelationImpl(e, Vars.p, Vars.o);

        return result;
    }


    public static Element remapVars(Element element, Map<Var, Var> varMap) {
        return null;
    }

    public static Query createQuery(ResourceShape resourceShape, Concept filter, boolean includeGraph) {
        List<Concept> concepts = ResourceShape.collectConcepts(resourceShape, includeGraph);

        Query result = createQuery(concepts, filter);
        return result;
    }

    /**
     * Deprecated, because with the construct approach we cannot get a tripe's context resource
     *
     * @param concepts
     * @param filter
     * @return
     */
    @Deprecated
    public static Query createQueryConstruct(List<Concept> concepts, Concept filter) {

        Template template = new Template(BasicPattern.wrap(Collections.singletonList(Triples.spo)));

        List<Concept> tmps = new ArrayList<Concept>();
        for(Concept concept : concepts) {
            Concept tmp = ConceptOps.intersect(concept, filter, null);
            tmps.add(tmp);
        }

        List<Element> elements = new ArrayList<Element>();
        for(Concept concept : tmps) {
            Element e = concept.getElement();
            elements.add(e);
        }

        Element element = ElementUtils.unionIfNeeded(elements);


        Query result = new Query();
        result.setQueryConstructType();
        result.setConstructTemplate(template);
        result.setQueryPattern(element);

        return result;
    }


//    public static MappedConcept<Map<Node, Graph>> createMappedConcept(ResourceShape resourceShape, Concept filter) {
//        Query query = createQuery(resourceShape, filter);
//        MappedConcept<Map<Node, Graph>> result = createMappedConcept(query);
//        return result;
//    }
//
//    public static MappedConcept<Map<Node, Graph>> createMappedConcept(Query query) {
//        BasicPattern bgp = new BasicPattern();
//        bgp.add(new Triple(Vars.s, Vars.p, Vars.o));
//        Template template = new Template(bgp);
//
//        Agg<Map<Node, Graph>> agg = AggMap.create(new BindingMapperExpr(new ExprVar(Vars.g)), new AggGraph(template));
//
//        Concept concept = new Concept(new ElementSubQuery(query), Vars.g);
//
//        MappedConcept<Map<Node, Graph>> result = new MappedConcept<Map<Node, Graph>>(concept, agg);
//        return result;
//    }

    public static MappedConcept<DatasetGraph> createMappedConcept2(ResourceShape resourceShape, Concept filter, boolean includeGraph) {
        Query query = createQuery(resourceShape, filter, includeGraph);
        logger.debug("Created query from resource shape: " + query);
        MappedConcept<DatasetGraph> result = createMappedConcept2(query);
        return result;
    }

    public static MappedConcept<Graph> createMappedConcept(ResourceShape resourceShape, Concept filter, boolean includeGraph) {
        Query query = createQuery(resourceShape, filter, includeGraph);
        logger.debug("Created query from resource shape: " + query);
        MappedConcept<Graph> result = createMappedConcept(query);
        return result;
    }

    public static MappedConcept<DatasetGraph> createMappedConcept2(Query query) {
        QuadPattern qp = new QuadPattern();
        qp.add(new Quad(Vars.g, Vars.s, Vars.p, Vars.o));

        Agg<DatasetGraph> agg = new AggDatasetGraph(qp);

        Concept concept = new Concept(new ElementSubQuery(query), Vars.x);

        MappedConcept<DatasetGraph> result = new MappedConcept<DatasetGraph>(concept, agg);
        return result;
    }


    public static MappedConcept<Graph> createMappedConcept(Query query) {
        // TODO We need to include the triple direction in var ?z

        BasicPattern bgp = new BasicPattern();
        bgp.add(new Triple(Vars.s, Vars.p, Vars.o));
        Template template = new Template(bgp);

        //Agg<Map<Node, Graph>> agg = AggMap.create(new BindingMapperExpr(new ExprVar(Vars.g)), new AggGraph(template));
        Agg<Graph> agg = new AggGraph(template, Vars.z);

        Concept concept = new Concept(new ElementSubQuery(query), Vars.x);

        MappedConcept<Graph> result = new MappedConcept<Graph>(concept, agg);
        return result;
    }

    public static Query createQuery(List<Concept> concepts, Concept filter) {

        List<Concept> tmps = new ArrayList<Concept>();
        for(Concept concept : concepts) {
            Concept tmp = ConceptOps.intersect(concept, filter, null);
            tmps.add(tmp);
        }

        List<Element> elements = new ArrayList<Element>();
        for(Concept concept : tmps) {
            Element e = concept.getElement();

            // Check if the Vars.g is part of the element - if not, create a sub query that remaps ?s to ?g
            Collection<Var> vs = PatternVars.vars(e);
            if(!vs.contains(Vars.x)) {
                Query q = new Query();
                q.setQuerySelectType();
                q.getProject().add(Vars.x, new ExprVar(Vars.s));

                if(vs.contains(Vars.g)) {
                    q.getProject().add(Vars.g);
                }
                q.getProject().add(Vars.s);
                q.getProject().add(Vars.p);
                q.getProject().add(Vars.o);
                q.getProject().add(Vars.z);
                q.setQueryPattern(e);

                e = new ElementSubQuery(q);
            }



            elements.add(e);
        }

        Element element = ElementUtils.unionIfNeeded(elements);

        Query result;
        if(elements.size() > 1) {

            result = new Query();
            result.setQuerySelectType();
            result.getProject().add(Vars.x);
            result.getProject().add(Vars.g);
            result.getProject().add(Vars.s);
            result.getProject().add(Vars.p);
            result.getProject().add(Vars.o);
            result.getProject().add(Vars.z);
            result.setQueryPattern(element);
        } else{
            result = ((ElementSubQuery)element).getQuery();
        }

        return result;
    }

    /**
     * Creates elements for this node and then descends into its children
     *
     * @param predicateConcept
     * @param target
     * @param isInverse
     * @return
     */
    public static Concept createConcept(Concept baseConcept, Generator<Var> vargen, BinaryRelation predicateRelation, boolean isInverse, boolean includeGraph) {
        Var sourceVar;

        Var baseVar = baseConcept.getVar();
        Element baseElement = baseConcept.getElement();


        Triple triple = isInverse
                ? new Triple(Vars.o, Vars.p, Vars.s)
                : new Triple(Vars.s, Vars.p, Vars.o);

        BasicPattern bp = new BasicPattern();
        bp.add(triple);

        Element etmp = new ElementTriplesBlock(bp);

        Element e2 = includeGraph
                ? new ElementNamedGraph(Vars.g, etmp)
                : etmp;


        Element e;
        if(baseElement != null) {
            //Var baseVar = baseConcept.getVar();
            //Element baseElement = baseConcept.getElement();


            // Rename the variables s, p, o with fresh variables from the vargenerator
            Map<Var, Var> rename = new HashMap<Var, Var>();
            rename.put(Vars.s, vargen.next());
            rename.put(Vars.p, vargen.next());
            rename.put(Vars.z, vargen.next());
            rename.put(Vars.o, Vars.s);

            rename.put(baseVar, Vars.x);

            sourceVar = MapUtils.getOrElse(rename, baseVar, baseVar);
            //Element e1 = Element
            Element e1 = ElementUtils.createRenamedElement(baseElement, rename);
            e = ElementUtils.mergeElements(e1, e2);

        }   else {
            e = e2;
            sourceVar = Vars.s;
        }


        Collection<Var> eVars = PatternVars.vars(e);
        Set<Var> pVars = predicateRelation.getVarsMentioned();

        // Add the predicateConcept
        Map<Var, Var> pc = VarUtils.createDistinctVarMap(eVars, pVars, true, vargen);
        // Map the predicate concept's var to ?p
        pc.put(predicateRelation.getSourceVar(), Vars.p);
        pc.put(predicateRelation.getTargetVar(), Vars.o);


        Element e3 = ElementUtils.createRenamedElement(predicateRelation.getElement(), pc);
        Element newElement = ElementUtils.mergeElements(e, e3);

        Element e4 = new ElementBind(Vars.z, NodeValue.makeBoolean(isInverse));
        newElement = ElementUtils.mergeElements(newElement, e4);


        Concept result = new Concept(newElement, sourceVar);

        return result;
    }


    /**
     * Whether a triple matches any of the ingoing or outgoing filter expression of this node
     * @param triple
     * @param functionEnv
     * @return
     */
//    public boolean contains(Triple triple, FunctionEnv functionEnv) {
//        Set<Expr> exprs = Sets.union(outgoing.keySet(), ingoing.keySet());
//
//        boolean result = contains(exprs, triple, functionEnv);
//        return result;
//    }

    public static boolean contains(Collection<Expr> exprs, Triple triple, FunctionEnv functionEnv) {
        Binding binding = TripleUtils.tripleToBinding(triple);

        boolean result = false;
        for(Expr expr : exprs) {
            NodeValue nodeValue = expr.eval(binding, functionEnv);

            if(nodeValue.equals(NodeValue.TRUE)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Get all triples about the titles of books, together with the books'
     * male buyers' names and age.
     *
     * The generated query has the structure
     *
     * Construct {
     *   ?s ?p ?o
     * } {
     *   { ?x a Book . Bind(?x as ?s)} # Root concept
     *
     *     { # get the 'buyer' triples
     *       ?s ?p ?o . Filter(?p = boughtBy) // outgoing
     *       { ?o gender male }} // restricting the resources of the relation
     *
     *
     *       #Optional {
     *       #  ?x ?y ?z . Filter(?y = rdfs:label)
     *       #}
     *     }
     *   Union {
     *     { # get the buyers names - requires repetition of above's pattern
     *       ?x ?y ?s . Filter(?y = boughtBy)
     *       { ?s gender male }} // restricting the resources of the relation
     *
     *       ?s ?p ?o . Filter(?p = rdfs:label)
     *     }
     *   Union
     *     {
     *       ?s ?p ?o . Filter(?p = dc:title && langMatches(lang(?o), 'en')) # outgoing
     *     }
     * }
     *
     *
     * @param root
     * @return
     */
//    public static Query createQuery(ResourceShape root) {
//        List<Element> unionMembers = new ArrayList<Element>();
//    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((in == null) ? 0 : in.hashCode());
        result = prime * result
                + ((out == null) ? 0 : out.hashCode());
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
        ResourceShape other = (ResourceShape) obj;
        if (in == null) {
            if (other.in != null)
                return false;
        } else if (!in.equals(other.in))
            return false;
        if (out == null) {
            if (other.out != null)
                return false;
        } else if (!out.equals(other.out))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ResourceShape [outgoing=" + out + ", ingoing=" + in
                + "]";
    }

//    public static Graph fetchData(SparqlService sparqlService, ResourceShape shape, Node node) {
//        RDFConnection qef = sparqlService.getRDFConnection();
//
//        Graph result = fetchData(qef, shape, node);
//        return result;
//    }


    public static Graph fetchData(QueryExecutionFactoryQuery qef, ResourceShape shape, Node node) {
        MappedConcept<Graph> mc = ResourceShape.createMappedConcept(shape, null, false);
        LookupService<Node, Graph> ls = LookupServiceUtils.createLookupService(qef, mc);
        Map<Node, Graph> map = ls.fetchMap(Collections.singleton(node));
//        if(map.size() > 1) {
//            throw new RuntimeException("Should not happen");
//        }

        Graph result = map.isEmpty() ? null : map.values().iterator().next();
        return result;

    }


}
