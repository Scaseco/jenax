package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.jena.jgrapht.PseudoGraphJenaModel;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.sparql_path.core.PathConstraintBase;
import org.aksw.jena_sparql_api.sparql_path.core.VocabPath;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.util.exec.QueryExecutionUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.path.PathUtils;
import org.aksw.jenax.sparql.path.SimplePath;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.vocabulary.OWL2;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.common.primitives.Ints;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ConceptPathFinderBidirectionalUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConceptPathFinderBidirectionalUtils.class);


    public static Single<Model> createDefaultDataSummary(SparqlQueryConnection dataConnection) {
        InputStream in = ConceptPathFinderBidirectionalUtils.class.getClassLoader().getResourceAsStream("concept-path-finder.conf.sparql");
        //Stream<SparqlStmt> stmts;
        Flowable<SparqlStmt> stmts;
        stmts = Flowable.fromIterable(() -> {
            try {
                return SparqlStmtUtils.parse(in, SparqlStmtParserImpl.create(Syntax.syntaxARQ, true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Single<Model> result = stmts
            //.peek(System.out::println)
            .filter(SparqlStmt::isQuery)
            .map(SparqlStmt::getAsQueryStmt)
            .map(SparqlStmtQuery::getQuery)
            .filter(q -> q.isConstructType())
            .map(dataConnection::queryConstruct)
            .toList()
            .map(list -> {
                Model r = ModelFactory.createDefaultModel();
                list.forEach(r::add);
                return r;
            });

        return result;
    }

    /**
     * Takes a concept and adds
     * SELECT DISTINCT ?t { concept(s) . OPTIONAL { ?s a ?tmp } BIND(IF(BOUND(?tmp), ?tmp, eg:untyped) AS ?t) }
     *
     *
     *
     * @return
     */
    public static UnaryRelation createUnboundAwareTypeQuery(UnaryRelation concept) {
//        Set<Var> vars = concept.getVarsMentioned();
//        Var s = concept.getVar();

        UnaryRelation result;
        if(concept.isSubjectConcept()) {
            result = Concept.parse("?t { ?s a ?t }");
        } else {

            Concept fragment = Concept.parse("?t { OPTIONAL { ?s a ?tmp } BIND(IF(BOUND(?tmp), ?tmp, eg:unbound) AS ?t) }", PrefixMapping.Extended);
            result = fragment
                    .prependOn(Vars.s)
                    .with(concept)
                    //.project(fragment.getVars())
                    .toUnaryRelation();
        }

        return result;
    }

//	Collection<BiPredicate<? super SimplePath, ? super P_Path0>> pathValidators,
//	PathConstraintBase pathConstraint,
//	BiFunction<? super GraphPath<RDFNode, Statement>, ? super Model, SimplePath> convertGraphPathToSparqlPath) {



    public static Flowable<SimplePath> findPathsCore(
            SparqlQueryConnection conn,
            UnaryRelation sourceConcept,
            UnaryRelation tmpTargetConcept,
            Long nPaths,
            Long maxLength,
            org.apache.jena.graph.Graph baseDataSummary,
            Boolean shortestPathsOnly,
            Boolean simplePathsOnly,
            Collection<BiPredicate<? super SimplePath, ? super P_Path0>> pathValidators,
            PathConstraintBase pathConstraint,
            BiFunction<? super GraphPath<RDFNode, Statement>, ? super Model, SimplePath> convertGraphPathToSparqlPath) {
        shortestPathsOnly = shortestPathsOnly == null ? false : shortestPathsOnly;
        simplePathsOnly = simplePathsOnly == null ? false : simplePathsOnly;



        UnaryRelation targetConcept = ConceptUtils.makeDistinctFrom(tmpTargetConcept, sourceConcept);

        logger.debug("Distinguished target concept: " + targetConcept);

        UnaryRelation typeConcept = createUnboundAwareTypeQuery(sourceConcept);

        Query typeQuery = typeConcept.asQuery();
        logger.debug("Property query: " + typeQuery);


        //System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("SELECT * { ?s ?p ?o }").execSelect()));
        //System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("" + propertyQuery).execSelect()));
        List<Node> types = QueryExecutionUtils.executeList(conn::query, typeQuery);
        logger.debug("Retrieved " + types.size() + " types"); // + " properties: " + types);

        org.apache.jena.graph.Graph ext = GraphFactory.createDefaultGraph();

        // Add the start nodes (types) to the transition model
        for(Node node : types) {

            // TODO Hack to see how this affects performance and quality
//		    if(node.getURI().startsWith("http://dbpedia.org/property/")) {
//		        continue;
//		    }

//        	Set<Node> types = baseDataguide.find(Node.ANY, VocabPath.hasOutgoingPredicate.asNode(), node)
//        		.mapWith(Triple::getSubject)
//        		.toSet();
//
//        	for(Node type : types) {
//                Triple triple = new Triple(VocabPath.start.asNode(), VocabPath.joinsWith.asNode(), type);
//                ext.add(triple);
//        	}

            Triple triple = new Triple(VocabPath.start.asNode(), VocabPath.connectsWith.asNode(), node);
            ext.add(triple);

//			System.out.println("JoinSummaryTriple: " + triple);
//            Statement stmt = joinSummaryGraph.asStatement(triple);
//            joinSummaryGraph.add(stmt);
        }


        org.apache.jena.graph.Graph union = new Union(baseDataSummary, ext);
        Model joinSummaryGraph = ModelFactory.createModelForGraph(union);

        //RDFDataMgr.write(System.out, joinSummaryGraph, RDFFormat.TURTLE_PRETTY);



        QueryExecutionFactory qefMeta = new QueryExecutionFactoryModel(joinSummaryGraph);


        Model unionModel = ModelFactory.createModelForGraph(union);
        //Graph<Node, Triple> graph = new PseudoGraphJenaGraph(union);
        Graph<RDFNode, Statement> graph = new PseudoGraphJenaModel(unionModel);

        // Now transform the target query so the find candidate nodes in the transition graph

        // Essentially:
        // ?moo prop1 ?foo . ?foo prop 2 ?bar .
        // becomes
        // Select ?s { ?s connectsTo ?prop1 . ?prop1 connectsTo ?foo }
        // In other words: we take the target concept, extract all quads

        //String test = "Prefix o:<http://foo.bar/> Prefix geo:<http://www.w3.org/2003/01/geo/wgs84_pos#> Select ?s { ?s o:connectsTo geo:long ; o:connectsTo geo:lat }";


        Concept targetCandidateConcept = pathConstraint.getPathConstraintsSimple(targetConcept);
        Query targetCandidateQuery = targetCandidateConcept.asQuery();

        //Query query = QueryFactory.create(test);
        logger.debug("TargetCandidateQuery: " + targetCandidateQuery);
        List<Node> candidates = QueryExecutionUtils.executeList(qefMeta::createQueryExecution, targetCandidateQuery);
        logger.debug("Got " + candidates.size() + " candidate target nodes"); // + " candidates: " + candidates);

        for(Node candidate : candidates) {
            Triple triple = new Triple(candidate, VocabPath.connectsWith.asNode(), VocabPath.end.asNode());
            ext.add(triple);
        }

        // Now that we know the candidates, we can start with out breath first search

        //DataSource ds = BreathFirstTask.createDb();


//        // Convert the join summary to a jGraphT object
//        Node startVertex = VocabPath.start.asNode();
//        Node endVertex = VocabPath.end.asNode();
        RDFNode startVertex = VocabPath.start;
        RDFNode endVertex = VocabPath.end;

//        DefaultDirectedGraph<Node, DefaultEdge> graph = new DefaultDirectedGraph<Node, DefaultEdge>(DefaultEdge.class);
//
//
//        graph.addVertex(startVertex);
//
//        //graph.addVertex(startVertex);
//        StmtIterator itStmt = joinSummaryGraph.listStatements(null, VocabPath.joinsWith, (RDFNode)null);
//        while(itStmt.hasNext()) {
//            Statement stmt = itStmt.next();
//
//            Node s = stmt.getSubject().asNode();
//            Node o = stmt.getObject().asNode();
//
//            //System.out.println(s + " --- " + s.equals(startVertex));
//
//            graph.addVertex(s);
//            graph.addVertex(o);
//            graph.addEdge(s, o);
//        }
//
//        logger.debug("Graph Metrics: " + graph.vertexSet().size() + " vertices, " + graph.edgeSet().size() + " edges; based on (at least) " + joinSummaryGraph.size() + " triples");



        //PathCallbackList callback = new PathCallbackList();
        //xx//KShortestPaths<Node, DefaultEdge> kShortestPaths = new KShortestPaths<Node, DefaultEdge>(graph, startVertex, nPaths, maxHops);

//        List<GraphPath<Node, Triple>> candidateGraphPaths = new ArrayList<GraphPath<Node, Triple>>();
//        int i = 0;
//        for(Node candidate : candidates) {
//            ++i;
//            logger.debug("Processing candidate " + i + "/" + candidates.size() + ": " + candidate + " (nPaths = " + nPaths + ", maxHops = " + maxHops + ")");
            //Resource dest = joinSummaryModel.asRDFNode(candidate).asResource();

//            if(startVertex.equals(candidate)) {
//                GraphPath<Node, Triple> graphPath = new GraphWalk<Node, Triple>(graph, startVertex, candidate, new ArrayList<Triple>(), 0.0);
//                candidateGraphPaths.add(graphPath);
//            }
//            else {
//            	AllDirectedPaths<Node, Triple> pathAlgo = new AllDirectedPaths<>(graph);
//            	List<GraphPath<Node, Triple>> candidateGraphPaths = pathAlgo.getAllPaths(startVertex, VocabPath.end.asNode(), true, 10);


                //DijkstraShortestPath<Node, Triple> dijkstraShortestPath = new DijkstraShortestPath<>(graph);
                //GraphPath<Node, Triple> tmp = dijkstraShortestPath.getPath(startVertex, candidate);


        logger.info("Invoking path finder...");

        List<GraphPath<RDFNode, Statement>> candidateGraphPaths;
        // TODO version 2 of the path finder needs to multiple path length by 2
        Integer _maxPathLength = maxLength == null ? null : maxLength.intValue() + 2; // The '+ 2' is for the edges of the start and end vertex

        int n = nPaths == null ? Integer.MAX_VALUE : Ints.checkedCast(nPaths);

        if(!shortestPathsOnly) {
            //graph.edgeSet().forEach(System.out::println);

            AllDirectedPaths<RDFNode, Statement> pathAlgo = new AllDirectedPaths<>(graph);
            candidateGraphPaths = pathAlgo.getAllPaths(startVertex, endVertex, simplePathsOnly, _maxPathLength);
        } else {

            if(n == 0) {
                // Prevent illegal argument exception at jgrapht
                candidateGraphPaths = Collections.emptyList();
            } else {
//                KShortestPathAlgorithm<RDFNode, Statement> kShortestPaths = _maxPathLength == null
//                        ? new YenKShortestPath<>(graph)
//                        : new YenKShortestPath<>(graph, _maxPathLength);

                KShortestPathAlgorithm<RDFNode, Statement> kShortestPaths;
                if (_maxPathLength == null) {
                    kShortestPaths = new YenKShortestPath<>(graph);
                } else {
                    throw new IllegalArgumentException("Limiting the maximum length of shortest paths was removed in jgraph; needs evaluation on how to proceed; raising this exception for now");
                }

                candidateGraphPaths = kShortestPaths.getPaths(startVertex, endVertex, n);
            }
        }

        logger.info("Found " + candidateGraphPaths.size() + " candidate paths");

//        Collections.sort(candidateGraphPaths, new GraphPathComparator<>());



//        boolean detectEquivalentPaths = false;
//        if(detectEquivalentPaths) {
//            Multimap<SimplePath, GraphPath<RDFNode, Statement>> index = Multimaps.index(candidateGraphPaths, path -> convertGraphPathToSparqlPath.apply(path, joinSummaryGraph));
//	        for(Entry<SimplePath, Collection<GraphPath<RDFNode, Statement>>> entry : index.asMap().entrySet()) {
//	        	if(entry.getValue().size() > 1) {
//	        		System.out.println("MULTI MAP " + entry.getKey());
//	        		entry.getValue().forEach(System.out::println);
//	        	}
//	        }
//        }



        // Note: We could invoke the graph computations in a FlowableOnSubscribe implementation
        Stream<SimplePath> tmp = candidateGraphPaths.stream()
            .map(path -> convertGraphPathToSparqlPath.apply(path, unionModel))
            .filter(Objects::nonNull)
            .distinct();

        if(pathValidators != null && !pathValidators.isEmpty()) {
            tmp = tmp.filter(x -> testPath(x, pathValidators));
        }

        List<SimplePath> paths = tmp.collect(Collectors.toList());
        // Sort paths for determinism
        Collections.sort(paths);

//        Flowable<SimplePath> result =
//        		Flowable.fromIterable(candidateGraphPaths)
//        		.map(path -> convertGraphPathToSparqlPath.apply(path, unionModel))
//        		.filter(Objects::nonNull);

        Flowable<SimplePath> result = Flowable.fromIterable(paths);
        //result.toList().map(c -> Collections.shuffle(c));
        return result;
    }


//    public static Flowable<SimplePath> applyPathValidators(Flowable<SimplePath> tmp, Collection<BiPredicate<? super SimplePath, ? super P_Path0>> pathValidators) {
//
//		// Convert the graph paths to 'ConceptPaths'
////	    Stream<SimplePath> tmp = candidateGraphPaths.stream()
////	        	.map(path -> convertGraphPathToSparqlPath.apply(path, unionModel))
////	        	.filter(x -> x != null);
//
//
//
//	    if(pathValidators != null && !pathValidators.isEmpty()) {
//	    	tmp = tmp.filter(x -> testPath(x, pathValidators));
//	    }
//        List<SimplePath> paths = tmp
//            	.distinct() // TODO I would like to get rid of this distinct here; I am not totally sure how duplicates come into existence in the first place; it has something to do with enumeration of paths in the data summary
//            	.collect(Collectors.toList());
//
//            // Sort paths for determinism
//            Collections.sort(paths);
//
//
//            if(n >= 0) {
//            	paths = paths.subList(0, Math.min(n, paths.size()));
//            }
//
//    }

    public static Predicate<SimplePath> createSparqlPathValidator(
        SparqlQueryConnection conn,
        UnaryRelation sourceConcept,
        UnaryRelation tmpTargetConcept) {


        UnaryRelation targetConcept = ConceptUtils.makeDistinctFrom(tmpTargetConcept, sourceConcept);


        //List<Path> paths = callback.getCandidates();

        // Cross check whether the path actually connects the source and target concepts

        //List<SimplePath> result = new ArrayList<SimplePath>();

        Predicate<SimplePath> result = path -> {
            Set<Var> vars = new HashSet<>();
            vars.addAll(sourceConcept.getVarsMentioned());
            vars.addAll(targetConcept.getVarsMentioned());

            Generator<Var> generator = VarGeneratorBlacklist.create("v", vars);

            boolean r = validatePath(conn, sourceConcept, targetConcept, path, generator);
            return r;
        };
        return result;

//        // TODO Make configurable
//        boolean abortOnFailedValidation = true;
//        Flowable<SimplePath> result = Flowable.fromIterable(paths)
//        	.filter(path -> {
//        		boolean r;
//        		try {
//        			r = validatePath(conn, sourceConcept, targetConcept, path, generator);
//        		} catch(Exception e) {
//        			if(abortOnFailedValidation) {
//        				throw new RuntimeException(e);
//        			} else {
//        				r = false;
//        			}
//        		}
//
//        		return r;
//        	});
//
//        return result;

//
//        for(SimplePath path : paths) {
//            List<Element> pathElements = SimplePath.pathToElements(path, sourceConcept.getVar(), targetConcept.getVar(), generator);
//
//            List<Element> tmp = new ArrayList<Element>();
//            if(!sourceConcept.isSubjectConcept()) {
//                tmp.addAll(sourceConcept.getElements());
//            }
//
//            // TODO Should we treat the case where the target concept is a subject concept in a special way?
//            //if(!targetConcept.isSubjectConcept()) {
//                tmp.addAll(targetConcept.getElements());
//            //}
//
//            tmp.addAll(pathElements);
//
//            if(pathElements.isEmpty()) {
//                if(!sourceConcept.getVar().equals(targetConcept.getVar()) && !sourceConcept.isSubjectConcept()) {
//                    tmp.add(new ElementFilter(new E_Equals(new ExprVar(sourceConcept.getVar()), new ExprVar(targetConcept.getVar()))));
//                }
//            }
//
//            ElementGroup group = new ElementGroup();
//            for(Element t : tmp) {
//                group.addElement(t);
//            }
//
//            Query query = new Query();
//            query.setQueryAskType();
//            query.setQueryPattern(group);
//
//            logger.debug("Verifying candidate with query: " + query);
//
//            QueryExecution xqe = conn.query(query);
//            boolean isCandidate = xqe.execAsk();
//            logger.debug("Verification result is [" + isCandidate + "] for " + query);
//
//            if(isCandidate) {
//                result.add(path);
//            }
//        }
//
//        return Flowable.fromIterable(result);
    }


    public static boolean testPath(SimplePath path, Collection<BiPredicate<? super SimplePath, ? super P_Path0>> validators) {
        List<P_Path0> steps = path.getSteps();
        int n = steps.size();

        boolean result = true;
        outer: for(int i = 0; i < n; ++i) {
            List<P_Path0> base = steps.subList(0, i);
            P_Path0 contrib = steps.get(i);

            for(BiPredicate<? super SimplePath, ? super P_Path0> validator : validators) {
                result = validator.test(new SimplePath(base), contrib);
                if(!result) {
                    break outer;
                }
            }

        }

//    	System.out.println("PATHVALIDATION [" + result + "] " + path);
//    	if(result == false) {
//    		System.out.println("DEBUG POINT");
//    	}
        return result;
    }


    public static boolean validatePath(SparqlQueryConnection conn, UnaryRelation sourceConcept, UnaryRelation targetConcept, SimplePath path, Generator<Var> generator) {

        List<Element> pathElements = SimplePath.pathToElements(path, sourceConcept.getVar(), targetConcept.getVar(), generator);

        Var sourceVar = sourceConcept.getVar();
        Var targetVar = pathElements.isEmpty() ? sourceVar : targetConcept.getVar();

        UnaryRelation src = sourceConcept;
        UnaryRelation tgt = targetConcept;
        Var sourceJoinVar = sourceVar;
        Var targetJoinVar = targetVar;

        // If the source concept is a subject concept, possibly swap it
        if(sourceConcept.isSubjectConcept()) {
            if(!targetConcept.isSubjectConcept()) {
                src = targetConcept;
                tgt = sourceConcept;

                sourceJoinVar = targetVar;
                targetJoinVar = sourceVar;
            }
        }

//	    if(Objects.equals(sourceJoinVar, targetJoinVar)) {
//        	System.out.println("DEBUG POINT Equal var");
//        }

        BinaryRelation pathRelation = new BinaryRelationImpl(ElementUtils.groupIfNeeded(pathElements), sourceVar, targetVar);
        Element group = pathRelation
            .prependOn(sourceJoinVar).with(src)
            .joinOn(targetJoinVar).with(tgt)
            .getElement();

        Query query = new Query();
        query.setQueryAskType();
        query.setQueryPattern(group);


        logger.debug("Verifying candidate with query: " + query);

        // For future reference: If the query is
        // { ?s  <http://vocab.gtfs.org/terms#parentStation>  ?v_1 .
        // ?v_1  ?p  ?o }
        // Then the second pattern actually expresses the constraint
        // that v_1 must appear as a subject
        // This is not optimal, but correct!

//        if(("" + query).contains("?v_1  ?p  ?o")) {
//        	System.out.println("DEBUG POINT");
//        }

        // TODO Make timeouts configurable
        boolean result;
        Rewrite rewrite = AlgebraUtils.createDefaultRewriter();
        query = QueryUtils.rewrite(query, rewrite::rewrite);
        try(QueryExecution qe = conn.query(query)) {
            //qe.setTimeout(30, TimeUnit.SECONDS, 30, TimeUnit.SECONDS);
            result = qe.execAsk();
        }

        logger.debug("Verification result is [" + result + "] for " + query);

        return result;
    }

    /**
     *
     * @param paths
     * @return (LinkedHash)Set of paths that validated
     */
//    public static Set<SimplePath> validatePaths(
//    		Generator<Var> generator,
//    		UnaryRelation sourceConcept,
//    		UnaryRelation targetConcept,
//    		RDFConnection conn,
//    		Collection<SimplePath> paths) {
//    	Set<SimplePath> result = paths.stream()
//    		.filter(path -> ConceptPathFinderBidirectionalUtils.validatePath(generator, sourceConcept, targetConcept, conn, path))
//    		.collect(Collectors.toCollection(LinkedHashSet::new));
//    	return result;
//    }


//
//    public static boolean validatePath(
//    		Generator<Var> generator,
//    		UnaryRelation sourceConcept,
//    		UnaryRelation targetConcept,
//    		RDFConnection conn,
//    		SimplePath path) {
//        List<Element> pathElements = SimplePath.pathToElements(path, sourceConcept.getVar(), targetConcept.getVar(), generator);
//
//        List<Element> tmp = new ArrayList<Element>();
//        if(!sourceConcept.isSubjectConcept()) {
//            tmp.addAll(sourceConcept.getElements());
//        }
//
//        // TODO Should we treat the case where the target concept is a subject concept in a special way?
//        //if(!targetConcept.isSubjectConcept()) {
//            tmp.addAll(targetConcept.getElements());
//        //}
//
//        tmp.addAll(pathElements);
//
//        if(pathElements.isEmpty()) {
//            if(!sourceConcept.getVar().equals(targetConcept.getVar()) && !sourceConcept.isSubjectConcept()) {
//                tmp.add(new ElementFilter(new E_Equals(new ExprVar(sourceConcept.getVar()), new ExprVar(targetConcept.getVar()))));
//            }
//        }
//
//        ElementGroup group = new ElementGroup();
//        for(Element t : tmp) {
//            group.addElement(t);
//        }
//
//        Query query = new Query();
//        query.setQueryAskType();
//        query.setQueryPattern(group);
//
//        logger.debug("Verifying candidate with query: " + query);
//
//        QueryExecution xqe = conn.query(query);
//        boolean isCandidate = xqe.execAsk();
//        logger.debug("Verification result is [" + isCandidate + "] for " + query);
//
//
//        return isCandidate;
////        if(isCandidate) {
////            result.add(path);
////        }
//    }

    public static Boolean isFwd(Node p) {
        Boolean result =
                VocabPath.hasOutgoingPredicate.asNode().equals(p) ? (Boolean)true :
                VocabPath.hasIngoingPredicate.asNode().equals(p) ? (Boolean)false :
                null;

        return result;
    }

    public static boolean isFwd3(Node p) {
        boolean result = !p.toString().contains("transition-inverse");
        return result;
    }


    // TODO We need access to the model in order to retrieve attributes of singular predicates
    // Either change Graph<Node, Triple> to Graph<RDFNode, Statement>, or pass the model as an argument
    public static SimplePath convertGraphPathToSparqlPath3(GraphPath<RDFNode, Statement> graphPath, Model model) {

        List<Statement> el = graphPath.getEdgeList();
        List<Statement> effectiveEdgeList = el.subList(1, el.size() - 1);

        SimplePath result = null;
        try {
            List<P_Path0> steps = Streams.mapWithIndex(effectiveEdgeList.stream(), Maps::immutableEntry)
//	        	.filter(e -> e.getValue() % 2 == 0)
                .map(e -> e.getKey())
                // We may get a NPE here if t.getPredicate() could not be classified
                .map(t -> {
                    // We always access transititon data via the non-inverse edge
                    Resource p = t.getPredicate();

                    boolean isFwd = true;
                    Resource transitionPredicate = p.getPropertyResourceValue(OWL2.annotatedProperty);
                    if(transitionPredicate == null) {
                        transitionPredicate = Optional.ofNullable(p.getPropertyResourceValue(OWL2.inverseOf))
                                .map(x -> x.getPropertyResourceValue(OWL2.annotatedProperty)).orElse(null);
                        isFwd = false;
                    }
                    // transitionPredicate should never be null at this stage

                    P_Path0 step = PathUtils.createStep(transitionPredicate.asNode(), isFwd);
                    return step;
                })
                .collect(Collectors.toList())
                ;

            result = new SimplePath(steps);
        } catch(Exception e) {
            logger.debug("Harmless exception - but may indicate a bug in the algo or issue with input data: ", e);
        }

//        effectiveEdgeList
//        	.stream().fi
//        for(int i = 0; i < effectiveEdgeList.size(); i += 2) {
//        	Triple t = effectiveEdgeList.get(i);
//
//        	Node p = t.getPredicate();
//            String propertyName = t.getObject().getURI();
//
//            Boolean isFwd =
//            		VocabPath.hasOutgoingPredicate.asNode().equals(p) ? (Boolean)true :
//            		VocabPath.hasIngoingPredicate.asNode().equals(p) ? (Boolean)false :
//            		null;
//
//            if(isFwd == null) {
//            	continue;
//            }
//
//            Step step = new Step(propertyName, !isFwd);
//            steps.add(step);
//        }
//
//        Path result = new Path(steps);
        return result;

    }

    public static SimplePath convertGraphPathToSparqlPath(GraphPath<RDFNode, Statement> graphPath, Model model) {

        List<Statement> el = graphPath.getEdgeList();
        List<Statement> effectiveEdgeList = el.subList(1, el.size() - 1);

        SimplePath result = null;
        try {
            List<P_Path0> steps = Streams.mapWithIndex(effectiveEdgeList.stream(), Maps::immutableEntry)
                .filter(e -> e.getValue() % 2 == 0)
                .map(e -> e.getKey())
                // We may get a NPE here if t.getPredicate() could not be classified
                .map(t -> PathUtils.createStep(t.getObject().asNode(), isFwd(t.getPredicate().asNode())))
                .collect(Collectors.toList())
                ;

            result = new SimplePath(steps);
        } catch(Exception e) {
            logger.debug("Harmless exception - but may indicate a bug in the algo or issue with input data: ", e);
        }

//        effectiveEdgeList
//        	.stream().fi
//        for(int i = 0; i < effectiveEdgeList.size(); i += 2) {
//        	Triple t = effectiveEdgeList.get(i);
//
//        	Node p = t.getPredicate();
//            String propertyName = t.getObject().getURI();
//
//            Boolean isFwd =
//            		VocabPath.hasOutgoingPredicate.asNode().equals(p) ? (Boolean)true :
//            		VocabPath.hasIngoingPredicate.asNode().equals(p) ? (Boolean)false :
//            		null;
//
//            if(isFwd == null) {
//            	continue;
//            }
//
//            Step step = new Step(propertyName, !isFwd);
//            steps.add(step);
//        }
//
//        Path result = new Path(steps);
        return result;

    }
//        for(Triple edge : graphPath.getEdgeList()) {
//            Node source = graph.getEdgeSource(edge);
//            Node target = graph.getEdgeTarget(edge);
//
//            boolean isInverse;
//
//            if(current.equals(source)) {
//                current = target;
//                isInverse = false;
//            }
//            else if(current.equals(target)) {
//                current = source;
//                isInverse = true;
//            }
//            else {
//                throw new RuntimeException("Should not happen");
//            }
//
//            String propertyName = current.getURI();
//            Step step = new Step(propertyName, isInverse);
//
//            steps.add(step);
//        }


}
