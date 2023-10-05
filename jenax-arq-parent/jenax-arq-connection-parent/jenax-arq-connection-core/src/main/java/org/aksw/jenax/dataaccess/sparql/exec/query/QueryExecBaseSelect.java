package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.util.closeable.AutoCloseableBase;
import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map all non-select sparql query types to select queries.
 *
 * Describe queries are currently unsupported.
 */
public abstract class QueryExecBaseSelect
    extends AutoCloseableBase
    implements QueryExec
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecBaseSelect.class);

    protected Query query;
    protected Context context;

    protected RowSet activeRowSet = null;

    public QueryExecBaseSelect(Query query, Context context) {
        super();
        this.query = query;
        this.context = context;
    }

//    public static QueryExecFactoryQuery constructViaSelect(QueryExecFactoryQuery base) {
//    	return new QueryExecBaseSelect() {
//
//			@Override
//			protected RowSet createRowSet(Query selectQuery) {
//				// TODO Auto-generated method stub
//				return null;
//			}
//		};
//
//    }


    protected synchronized void setActiveRowSet(RowSet rowSet) {
        this.activeRowSet = rowSet;

        if (isClosed()) {
            activeRowSet.close();
        }
    }

    protected RowSet getActiveRowSet() {
        return activeRowSet;
    }

    /**
     * The actual method that needs to be implemented. The argument is always a SPARQL
     * query of select type.
     * The RowSet may need to implement close() in order to close an underlying query execution.
     */
    protected abstract RowSet createRowSet(Query selectQuery);

    protected synchronized final RowSet select(Query query) {
        // ensureOpen();

        if(this.getActiveRowSet() != null) {
            throw new RuntimeException("A query is already running");
        }

        RowSet rowSet = createRowSet(query);

        if(rowSet == null) {
            throw new RuntimeException("Failed to obtain a QueryExecution for query: " + query);
        }

        setActiveRowSet(activeRowSet);
        return rowSet;
    }

    @Override
    public boolean ask() {
        if (!query.isAskType()) {
            throw new RuntimeException("ASK query expected. Got: ["
                    + query.toString() + "]");
        }

        Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());
        selectQuery.setLimit(1);

        RowSet rs = select(selectQuery);

        long rowCount = 0;
        while(rs.hasNext()) {
            rs.next();
            ++rowCount;
        }

        if (rowCount > 1) {
            logger.warn("Received " + rowCount + " rows for the query ["
                    + query.toString() + "]");
        }

        return rowCount > 0;
    }

    @Override
    public Iterator<Quad> constructQuads() {
        if (!query.isConstructType()) {
            throw new RuntimeException("CONSTRUCT query expected. Got: ["
                    + query.toString() + "]");
        }

        Template template = query.getConstructTemplate();
        Query clone = adjust(query);
        RowSet rs = select(clone);
        Iterator<Quad> result = TemplateLib.calcQuads(template.getQuads(), rs);

        return result;
    }


    protected Iterator<Triple> constructStreaming(Query query) {
        if (!query.isConstructType()) {
            throw new RuntimeException("CONSTRUCT query expected. Got: ["
                    + query.toString() + "]");
        }

        Template template = query.getConstructTemplate();
        Query clone = adjust(query);
        RowSet rs = select(clone);
        Iterator<Triple> result = TemplateLib.calcTriples(template.getTriples(), rs);

        return result;
    }

    public static Query adjust(Query query) {
        Template template = query.getConstructTemplate();
        Set<Var> projectVars = QuadPatternUtils.getVarsMentioned(template.getQuads());

        Query clone = query.cloneQuery();
        clone.setQuerySelectType();

        //Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());

        clone.getProject().clear();
        if(projectVars.isEmpty()) {
            // If the template is variable free then project the first variable of the query pattern
            // If the query pattern is variable free then just use the result star
            Set<Var> patternVars = SetUtils.asSet(PatternVars.vars(query.getQueryPattern()));
            if(patternVars.isEmpty()) {
                clone.setQueryResultStar(true);
            } else {
                Var v = patternVars.iterator().next();
                clone.setQueryResultStar(false);
                clone.getProject().add(v);
            }
        } else {
            clone.setQueryResultStar(false);
            clone.addProjectVars(projectVars);
        }

        return clone;
    }

    @Override
    public Graph construct(Graph result) {
        GraphUtil.add(result, constructTriples());
        return result;
    }

    @Override
    public Graph construct() {
        Graph result = GraphFactory.createDefaultGraph();
        construct(result);
        return result;
    }

    @Override
    public Iterator<Triple> constructTriples() {
        return constructStreaming(this.query);
    }

    @Override
    public RowSet select() {
        if (query != null && !query.isSelectType()) {
            throw new RuntimeException("SELECT query expected. Got: ["
                    + query.toString() + "]");
        }

        return createRowSet(query);
    }

    @Override
    public Query getQuery() {
        return query;
    }

    @Override
    public String getQueryString() {
        return query == null ? null : query.toString();
    }

    //@Override
    public void executeUpdate(UpdateRequest updateRequest)
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public JsonArray execJson() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DatasetGraph getDataset() {
        return null;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public DatasetGraph constructDataset(DatasetGraph dataset) {
        constructQuads().forEachRemaining(dataset::add);
        return dataset;
    }

    @Override
    public Graph describe(Graph graph) {
        describeTriples().forEachRemaining(graph::add);
        return graph;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void closeActual() {
        RowSet rowSet = getActiveRowSet();
        if (rowSet != null) {
            rowSet.close();
        }
    }

    @Override
    public void abort() {
        close();
    }

//	@Override
//	public Graph describe() {
//		Graph graph = GraphFactory.createDefaultGraph();
//		return describe(graph);
//	}

    /**
    * We use this query execution for retrieving the result set of the
    * where clause, but we neet the subFactory to describe the individual
    * resources then.
    *
    * @return
    */
    @Override
    public Iterator<Triple> describeTriples() {
        throw new UnsupportedOperationException("Method not migrated yet");
//		ResultSetCloseable rs = null;
//		if ( query.getQueryPattern() != null ) {
//		    Query q = new Query();
//		    q.setQuerySelectType();
//		    q.setResultVars();
//		    for(String v : query.getResultVars()) {
//		        q.addResultVar(v);
//		    }
//		    q.setQueryPattern(query.getQueryPattern());
//
//		    rs = this.executeCoreSelect(q);
//		}
//
//		// Note: We need to close the connection when we are done
//
//		Describer tmp = Describer.create(query.getResultURIs(), query.getResultVars(), rs, parentFactory);
//
//
//		final QueryExecution self = this;
//
//		Iterator<Triple> result = new IteratorWrapperClose<Triple>(tmp) {
//		    @Override
//		    public void close() {
//		        self.close();
//		    }
//	};
//
//	return result;
    }

    // Describe queries are sent as multiple individual queries, therefore we require a
    // back reference to the corresponding QueryExecutionFactory
    // protected QueryExecFactory parentFactory;

//
//	// TODO Move these two utility methods to a utility class
//	// Either the whole Sparql API should go to the jena module
//	// or it needs a dependency on that module...
//	public static Model createModel(Iterator<Triple> it) {
//		return createModel(ModelFactory.createDefaultModel(), it);
//	}
//
////	public static Graph createGraph(Graph result, Iterator<Triple> it) {
////		while(it.hasNext()) {
////		    Triple t = it.next();
////		    Statement stmt = org.apache.jena.sparql.util.ModelUtils.tripleToStatement(result, t);
////		    if (stmt != null) {
////		        result.add(stmt);
////		    }
////		}
////
////		return result;
////	}

    /**
    * A describe query is translated into a construct query.
    *
    *
    *
    * Lets see...
    * Describe ?a ?b ... &lt;x&gt;&lt;y&gt; Where Pattern { ... } becomes ...?
    *
    * Construct { ?a ?ap ?ao . ?b ?bp ?bo . } Where Pattern {  } Union {}
    * Ah, lets just query every resource individually for now
    *
    *
    * TODO Add support for concise bounded descriptions...
    *
    * @param result
    * @return
    */
//	@Override
//	public Graph describe(Graph result) {
//		GraphUtil.add(result, describeTriples());
//		return result;
//	}


    /*
    Generator generator = Gensym.create("xx_generated_var_");

    Element queryPattern = query.getQueryPattern();
    ElementPathBlock pathBlock;

    if(queryPattern == null) {
        ElementGroup elementGroup = new ElementGroup();

        pathBlock = new ElementPathBlock();
        elementGroup.addElement(pathBlock);
    } else {

        ElementGroup elementGroup = (ElementGroup)queryPattern;

        pathBlock = (ElementPathBlock)elementGroup.getElements().get(0);
    }

    //Template template = new Template();
    //template.

    BasicPattern basicPattern = new BasicPattern();

    System.out.println(queryPattern.getClass());

    for(Node node : query.getResultURIs()) {
        Var p = Var.alloc(generator.next());
        Var o = Var.alloc(generator.next());

        Triple triple = new Triple(node, p, o);

        basicPattern.add();
        //queryPattern.
    }

    for(String var : query.getResultVars()) {

    }


    Template template = new Template(basicPattern);


    Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());

    ResultSet rs = executeCoreSelect(selectQuery);


    //throw new RuntimeException("Sorry, DESCRIBE is not implemted yet.");
    */
    // }

}
