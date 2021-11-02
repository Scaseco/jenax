package org.aksw.jena_sparql_api.data_query.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.data_query.api.PathAccessorRdf;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;



//class PathNode {
//	protected GraphVar graphVar;
//	protected Node node;
//
//	public void addProperty(Node p, Node o) {
//
//	}
//}

/**
 * Interface for injecting a (constraining) element into a query fragment template
 *
 * @author Claus Stadler, May 28, 2018
 *
 */
interface QueryFragmentFactory {
    QueryFragment create(Element element, List<Var> joinVars);
}

class QueryFragmentFactorySubQueryImpl
    implements QueryFragmentFactory
{
    /** A CONSTRUCT query */
    protected Query queryFragment;

    /** Variables mentioned in the <b>query pattern</b> (i.e. not the template) used for joining in other elements */
    protected List<Var> defaultJoinVars;



    @Override
    public QueryFragment create(Element element, List<Var> targetJoinVars) {


        // TODO If the query pattern is a subquery, inject into the subquery's query pattern

        Set<Var> sourceVars = null;
        Collection<Var> targetVars = PatternVars.vars(element);
        Generator<Var> varGen = VarGeneratorImpl2.create();
        Map<Var, Var> varMap = VarUtils.createJoinVarMap(sourceVars, targetVars, defaultJoinVars, targetJoinVars, varGen);

        Element renamedElement = ElementUtils.createRenamedElement(element, varMap);
        Element result = ElementUtils.mergeElements(queryFragment.getQueryPattern(), renamedElement);

        Query result2 = queryFragment.cloneQuery();
        return null;
    }


//	public void Element createJoinedElement(Element element, Set<Var> joinVars) {
//		Element result = ElementUtils.join;
//	}
}


/**
 * A query fragment is construct query with a designated variable.
 *
 * TODO Clarify whether there needs to be a join() function that injects a given element
 *
 * Component to retrieve additional attributes for a given concept
 * In contrast to the BinaryRelation class, which uses designated source and target variables,
 * this class allows mapping property paths to variables vie the template
 *
 *
 * @author Claus Stadler, May 27, 2018
 *
 */
public class QueryFragment {
    protected Var conceptVar;
//	protected Element element;
//	protected BasicPattern template;
    protected Query query;
    //protected Set<Triple> triples;


    public QueryFragment(Var conceptVar, Query query) {
        super();
        this.conceptVar = conceptVar;
        this.query = query;
    }


    @Override
    public String toString() {
        return "QueryFragment [conceptVar=" + conceptVar + ", query=" + query + "]";
    }




    public static Triple createTriple(boolean isReverse, Node s, Node p, Node o) {
        Triple result = isReverse
                ? new Triple(o, p, s)
                : new Triple(s, p, o)
                ;

        return result;
    }

    public static QueryFragment createForPredicate(Node p, boolean isReverse) {
//        Map<BinaryRelation, ResourceShape> map = isReverse
//                ? resourceShape.getIngoing()
//                : resourceShape.getOutgoing();


//        ResourceShape rs = map.get(relation);
//        if(rs == null) {
//            rs = new ResourceShape();
//            map.put(relation, rs);
//        }

        Triple t = createTriple(isReverse, Vars.s, p, Vars.o);

        BasicPattern bgp = new BasicPattern();
        bgp.add(t);
        Query query = new Query();
        query.setConstructTemplate(new Template(bgp));
        query.setQueryPattern(new ElementTriplesBlock(bgp));

        //Element element = new ElementTriplesBlock();
        QueryFragment result = new QueryFragment(Vars.s, query);
        return result;
    }


    /**
     * - BGPs: BGPs are converted to Map<Path, Var>
     *   - TODO What to do with
     *     - forks in the path ?x foo [ bar ?y, baz ?z ]
     *     - multiple endpoints (a special case of a fork)
     * -
     * - Optional blocks are ignored as they are considered to correspond to the
     *   attribute part - i.e. they do not constrain the concept
     *
     * @param query
     */
    public static void extractFacetConstraints(Query query) {
    }



    /**
     * Create a query fragment that counts the number of distinct values for all properties.
     * The result is a map from each constrained property to its corresponding query fragment.
     * The 'null' fragment is for all other properties.
     *
     * <pre>{@code
     * # For each $constrainedProperty$:
     *
     * CONSTRUCT {
     *   ?p :facetCount ?c
     * } WHERE {
     *   { SELECT ?p (COUNT(DISTINCT ?o) AS ?c {
     *   	$concept(?s)$
     *      $constraintElementWithout($constrainedProperty$)$
     *      ?s ?p ?o . # may be reversed
     *   	FILTER(?p = $constrainedProperty$)
     *   } GROUP BY ?p }
     * }
     * }</pre>
     * @return
     */
//	public static Map<Node, QueryFragment> createForFacetCount(Path path, boolean direction, DimensionConstraintBlock constraints) {
////		Collection<VPath<?>> vpaths = constraints.getPaths();
////
////		// Distill the concrete paths
////		Collection<Path> paths = null;
////
////
////		// For each constraint property starting from the path, create the query fragment
////
////
////		//
//
//		return null;
//	}

//
//	public static QueryFragment createElementForPredicate(DimensionConstraintBlock constraints, VPath<?> path) {
//		// Create a new constraint block without the given path
//		DimensionConstraintBlock copy = constraints.copyWithoutPath(path);
//
//		// Convert the constraint block to a QueryFragment
//
//
//		return null;
//	}


//	public QuadFilterPatternCanonical toQueryFragment(PathConstraintBlock constraints) {
//		Collection<VPath<?>> paths = constraints.getPaths();
//
//		for(VPath<?> path : paths) {
//			PathResolver pathResolver = new PathResolverImpl(pathFragment, mapperEngine, reachingPropertyName, parent);
//
//			PathResolverVarMapper pathVarMapper = new PathResolverVarMapper(pathResolver, elements, aliasMapper);
//
//			path.accept(pathVarMapper);
//			Set<Element> elements = pathVarMapper.getElements();
//		}
//
//	}
//
//	public static <P> Collection<Element> toElements(DimensionConstraintBlock constraintBlock) {
//		// TODO implement
//		//PathResolver pathResolver = new PathResolverImpl(pathFragment, mapperEngine, reachingPropertyName, parent);
//		//PathResolverVarMapper pathVarMapper = new PathResolverVarMapper(pathResolver, elements, aliasMapper);
//
//		Collection<P> paths = constraintBlock.paths;
//
//		Set<Triple> triples = new LinkedHashSet<>();
//		Map<SPath, Node> map = new HashMap<>();
//		Generator<Var> varGen = VarGeneratorImpl2.create();
//
//		toElement(paths, accessor, triples, map, varGen);
//
//		BasicPattern bgp = new BasicPattern();
//		triples.forEach(bgp::add);
//		return Collections.singleton(new ElementTriplesBlock(bgp));
//	}


    public static <P> void toElement(Iterable<P> paths, PathAccessorRdf<P> accessor, Set<Element> elements, Map<P, BinaryRelation> pathToNode, Set<Var> forbiddenVars, Generator<Var> varGen) {
        for(P path : paths) {
            toElement(path, accessor, pathToNode, forbiddenVars, varGen);
        }
    }

    public static <P> Var getOrCreateAlias(P path, PathAccessorRdf<P> accessor, Map<P, BinaryRelation> pathToNode, Set<Var> forbiddenVars, Generator<Var> varGen) {
        varGen = VarGeneratorBlacklist.create(varGen, forbiddenVars);

        String tmp = accessor.getAlias(path);


        if(tmp == null) {
            P parent = accessor.getParent(path);
//			if(parent == null) {
//				path = null;
//			}

            BinaryRelation br = pathToNode.get(path);
            tmp = br != null ? br.getTargetVar().getName() : varGen.next().getName();
        }

        Var result = Var.alloc(tmp);

        //Var result = pathToNode.computeIfAbsent(path, br -> Optional.ofNullable(aliasStr).map(Var::alloc).orElse(varGen.next()));
        return result;
    }

    public static <P> BinaryRelation toElement(P path, PathAccessorRdf<P> accessor, Map<P, BinaryRelation> pathToNode, Set<Var> forbiddenVars, Generator<Var> varGen) {
        BinaryRelation result = pathToNode.get(path);

        // If the relation segment has not been created for the path, compute it for the parent first
        if(result == null) {

            P parentPath = accessor.getParent(path);

            if(parentPath == null) {

                Var s = getOrCreateAlias(path, accessor, pathToNode, forbiddenVars, varGen);
                result = new BinaryRelationImpl(new ElementGroup(), s, s);

            } else {
                //boolean isReverse = accessor.isReverse(path);

                BinaryRelation parentSegment = toElement(parentPath, accessor, pathToNode, forbiddenVars, varGen);
                Var s = parentSegment.getTargetVar();

                // Obtain the alias for the given node
                Var o = getOrCreateAlias(path, accessor, pathToNode, forbiddenVars, varGen);

                BinaryRelation br = accessor.getReachingRelation(path);
                //br = isReverse ? br.reverse() : br;

//				Set<Var> usedVars = elements.stream()
//						.map(PatternVars::vars)
//						.flatMap(Collection::stream)
//						.collect(Collectors.toSet());

                Set<Var> vars = br.getVarsMentioned();
                vars.remove(br.getSourceVar());
                vars.remove(br.getTargetVar());

                Map<Var, Var> map = VarUtils.createDistinctVarMap(forbiddenVars, vars, false, varGen);
                map.put(br.getSourceVar(), s);
                map.put(br.getTargetVar(), o);


                Element segment = ElementUtils.createRenamedElement(br.getElement(), map);

                result = new BinaryRelationImpl(segment, s, o);
            }

            pathToNode.put(path, result);
        }

        return result;
    }


    public static Property facetCount = ResourceFactory.createProperty("http://example.org/facetCount");



    public static void aggregate(QueryFragment queryFragment, Path path, Aggregator agg) {

    }


    public static QueryFragment createForFacetCountRemainder(Concept concept, boolean isReverse) {
        //BasicPattern bpg = new BasicPattern();
        Query query = new Query();

        // Generate new vars for the property and the count
        Generator<Var> varGen = VarGeneratorBlacklist.create("v", concept.getVarsMentioned());

        Var s = concept.getVar();
        Var p = varGen.next();
        Var o = varGen.next();
        Var c = varGen.next();


        BasicPattern bgp = new BasicPattern();
        bgp.add(new Triple(p, facetCount.asNode(), c));
        Template template = new Template(bgp);
        query.setConstructTemplate(template);

        VarExprList proj = query.getProject();
        proj.add(p);
        query.allocAggregate(new AggCountVarDistinct(new ExprVar(Vars.o)));

        List<Element> elts = new ArrayList<>();
        elts.addAll(concept.getElements());
        elts.add(ElementUtils.createElement(createTriple(isReverse, s, p, o)));

        Element queryPattern = ElementUtils.groupIfNeeded(elts);
        query.setQueryPattern(queryPattern);

        query.addGroupBy(p);

        QueryFragment result = new QueryFragment(o, query);
        return result;
    }

    /**
     * Create the query element that fetches the facet counts (i.e. number of distinct values)
     * of all properties
     *
     * <pre>{@code
     * CONSTRUCT {
     *   ?p :facetCount ?c
     * } WHERE {
     *   { SELECT ?p (COUNT(DISTINCT ?o) AS ?c {
     *   	$concept(?s)$
     *      ?s ?p ?o . # may be reversed
     *   	FILTER(!(?p IN ($exclusions$)))
     *   } GROUP BY ?p }
     * }
     * }</pre>
     *
     * @param excludes
     * @return
     */
//	public static QueryFragment createForFacetCountRemainder(DimensionConstraintBlock constraints, List<Node> excludes, boolean isReverse) {
//
//		Var var = Vars.s;
//
//
//		Collection<Element> elements = null; //toElements(constraints);
//		// Add element with the excludes
//		if(!excludes.isEmpty()) {
//			List<Expr> tmpExprs = excludes.stream().map(node -> NodeValue.makeNode(node)).collect(Collectors.toList());
//			ExprList exprs = new ExprList(tmpExprs);
//
//			Expr expr = new E_OneOf(new ExprVar(var), exprs);
//			elements.add(new ElementFilter(expr));
//		}
//
//		Concept concept = new Concept(new ArrayList<>(elements), var);
//		QueryFragment result = createForFacetCountRemainder(concept, isReverse);
//
//
//		//AttributeQuery result = new AttributeQuery(Vars.s, element, bgp);
//		return result;
//	}

}

