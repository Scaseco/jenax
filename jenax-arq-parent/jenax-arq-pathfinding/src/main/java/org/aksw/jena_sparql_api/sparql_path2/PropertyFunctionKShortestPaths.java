package org.aksw.jena_sparql_api.sparql_path2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.aksw.commons.util.Directed;
import org.aksw.commons.util.list.ListUtils;
import org.aksw.commons.util.triplet.TripletPath;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.connectionless.SparqlService;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropFuncArgType;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Select ?path {
 *   ?s path:find (?expr ?path ?target ?k)
 * }
 *
 * <ul>
 *   <li>?s: The start node for which to find paths</li>
 *   <li>?expr: A string representation of a property expression</li>
 *   <li>?path: The target variable, this variable will be bound to</li>
 *   <li>?target: A target node the property path must terminate it</li>
 *   <li>?k: How many paths, ordered by length, to retrieve</li>
 * </ul>
 * ?path i
 *
 *
 * @author raven
 *
 */
public class PropertyFunctionKShortestPaths
    extends PropertyFunctionEval
{
    private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionKShortestPaths.class);

    public static final String DEFAULT_IRI = "http://jsa.aksw.org/fn/kShortestPaths";

    public static final Symbol PROLOGUE = Symbol.create("prologue");
    public static final Symbol SPARQL_SERVICE = Symbol.create("sparqlService");

    protected Function<SparqlService, SparqlKShortestPathFinder> serviceToPathFinder;
    //protected Gson gson;

    public PropertyFunctionKShortestPaths(Function<SparqlService, SparqlKShortestPathFinder> serviceToPathFinder) { //Gson gson) {
        super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_EITHER);
//        this.gson = gson;
        this.serviceToPathFinder = serviceToPathFinder;
    }

    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject,
            Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

        Context ctx = execCxt.getContext();

        // TODO: Get the currently running query so we can inject the prefixes
        System.out.println("CONTEXT" + ctx);

        Prologue prologue = (Prologue)ctx.get(PROLOGUE);
        SparqlService ss = (SparqlService)ctx.get(SPARQL_SERVICE);
        Objects.requireNonNull(ss);
        QueryExecutionFactory qef = ss.getQueryExecutionFactory();
        Objects.requireNonNull(qef);

        List<Node> argList = argObject.getArgList();

        Node pathNode = ListUtils.getOrNull(argList, 0);
        Node outNode = ListUtils.getOrNull(argList, 1);
        Node targetNode = ListUtils.getOrNull(argList, 2);
        Node kNode = ListUtils.getOrNull(argList, 3);

        if(targetNode != null) {
            if(targetNode.isVariable()) {
                targetNode = binding.get((Var)targetNode);
            }

            // Note: Target node may be a blank from the binding
            if(targetNode != null && targetNode.isBlank()) {
                targetNode = null;
            }
        }

        Long tmpK = null;
        if(kNode != null && kNode.isLiteral()) {
            Object o = kNode.getLiteralValue();
            if(o instanceof Number) {
                tmpK = ((Number)o).longValue();
            }
        }
        final Long k = tmpK;

        // pathNode and outNode are mandatory, the other arguments are optional
        Objects.requireNonNull(pathNode);
        Objects.requireNonNull(outNode);

        if(!outNode.isVariable()) {
            throw new RuntimeException("Output node must be a variable");
        }
        Var outVar = (Var)outNode;

        //argObject.asExprList(pfArg)
        //if(!pathNode.isLiteral() || !pathN


//        System.out.println("so far so good");
//        System.out.println(argSubject.getArg());
//        System.out.println("Symbol" + execCxt.getContext().get(Name.create("test")));
        Node sv = argSubject.getArg();
        Node s = sv.isVariable() ? binding.get((Var)sv) : sv;

        final List<TripletPath<Node, Node>> rdfPaths = new ArrayList<>();

        String pathStr = pathNode.getLiteralLexicalForm();

        Path path = PathParser.parse(pathStr, prologue);

        SparqlKShortestPathFinder pathFinder = serviceToPathFinder.apply(ss);
        if(pathFinder == null) {
            logger.info("Falling back on default k shortest path finder service");
            pathFinder = new SparqlKShortestPathFinderMem(ss.getQueryExecutionFactory());
        }

        Iterator<TripletPath<Node, Directed<Node>>> itPaths = pathFinder.findPaths(s, targetNode,path, k);

//
//        PathExecutionUtils.executePath(path, s, targetNode, qef, p -> {
//            rdfPaths.add(p);
//            boolean r = k == null ? false : rdfPaths.size() >= k;
//            return r; });
//
//        Gson gson = new Gson();
//        List<Binding> bindings = new ArrayList<Binding>();
//
//        for(MyPath<Node, Node> rdfPath : rdfPaths) {
////            JsonElement json = gson.toJsonTree(rdfPath);
////            NodeValue rdfPathNodeValue = new NodeValueJson(json);
////            Node rdfPathNode = rdfPathNodeValue.asNode();
//            Node rdfPathNode = NodeFactory.createLiteral("" + rdfPath);
//
//            //Node rdfPathNode = NodeFactory.createLiteral(rdfPath.toString());
//            Binding b = BindingFactory.binding(binding, outVar, rdfPathNode);
//
//            bindings.add(b);
//        }

        //Iterable<NestedPath<Node, Node>> tmp = () -> itPaths;
        Iterable<TripletPath<Node, Directed<Node>>> tmp = () -> itPaths;
        Iterator<Binding> itBindings = StreamSupport.stream(tmp.spliterator(), false).map(p -> {
          Node pNode = NodeFactory.createLiteral("" + p);
          Binding r = BindingFactory.binding(binding, outVar, pNode);
          return r;
        }).iterator();

        QueryIterator result = QueryIterPlainWrapper.create(itBindings);
        return result;
    }

}
