package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.util.closeable.AutoCloseableBase;
import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map all non-select sparql query types to select queries.
 * Works for CONSTRUCT and ASK. DESCRIBE and JSON not yet supported.
 */
public abstract class QueryExecBaseSelect
    extends AutoCloseableBase
    implements QueryExecBaseIterator
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecBaseSelect.class);
    protected Query query;

    /**
     * If true, then triples and quads are produced by raw substitution with the bindings.
     * This means literals and variables (that are not substituted) may appear in any component.
     */
    protected boolean rawTuples;

    // protected final Object lock = new Object();
    protected volatile QueryExec activeQueryExec;
    protected volatile boolean isCancelled;

    public QueryExecBaseSelect(Query query) {
        this(query, false);
    }

    public QueryExecBaseSelect(Query query, boolean rawTuples) {
        super();
        this.query = query;
        this.rawTuples = rawTuples;
    }

    /**
     * The actual method that needs to be implemented. The argument is always a SPARQL
     * query of select type.
     * The RowSet may need to implement close() in order to close an underlying query execution.
     */
    protected abstract QueryExec doSelect(Query selectQuery);

    // XXX Not ideal - dataset and context should be available directly
    @Override
    public DatasetGraph getDataset() {
        return activeQueryExec == null ? null : activeQueryExec.getDataset();
    }

    @Override
    public Context getContext() {
        return activeQueryExec == null ? null : activeQueryExec.getContext();
    }

    @Override
    public void abort() {
        if (!isCancelled) {
            synchronized (this) {
                if (!isCancelled) {
                    isCancelled = true;
                    if (activeQueryExec != null) {
                        activeQueryExec.abort();
                    }
                }
            }
        }
    }

    @Override
    protected void closeActual() throws Exception {
        // Synchronized on 'this' by base class
        if (activeQueryExec != null) {
            activeQueryExec.close();
        }
    }

    protected void setActiveQueryExec(QueryExec queryExec) {
        synchronized (this) {
            if (activeQueryExec != null) {
                activeQueryExec.close();
            }

            activeQueryExec = queryExec;
            if (isCancelled) {
                activeQueryExec.abort();
            }

            if (isClosed) {
                this.activeQueryExec.close();
            }
        }
    }

    protected RowSet internalSelect(Query selectQuery) {
        QueryExec tmp;
        synchronized (this) {
            ensureOpen();

            if (activeQueryExec != null) {
                throw new RuntimeException("Query execution has already been started");
            }

            if (isCancelled) {
                throw new QueryCancelledException();
            }

            tmp = doSelect(selectQuery);
            setActiveQueryExec(tmp);
        }
        return tmp.select();
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
    public boolean ask() {
        if (!query.isAskType()) {
            throw new RuntimeException("ASK query expected. Got: ["
                    + query.toString() + "]");
        }

        Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());
        selectQuery.setLimit(1);

        RowSet rs = internalSelect(selectQuery);

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
        RowSet rs = internalSelect(clone);
        Iterator<Quad> result;
        if (rawTuples) {
            result = Iter.flatMap(rs, b -> Iter.map(
                template.getQuads().iterator(), q -> Substitute.substitute(q, b)));
        } else {
            result = TemplateLib.calcQuads(template.getQuads(), rs);
        }
        return result;
    }

    @Override
    public Iterator<Triple> constructTriples() {
        if (!query.isConstructType()) {
            throw new RuntimeException("CONSTRUCT query expected. Got: ["
                    + query.toString() + "]");
        }

        Template template = query.getConstructTemplate();
        Query clone = adjust(query);
        RowSet rs = internalSelect(clone);
        Iterator<Triple> result;
        if (rawTuples) {
            result = Iter.flatMap(rs, b -> Iter.map(
                template.getTriples().iterator(), t -> Substitute.substitute(t, b)));
        } else {
            result = TemplateLib.calcTriples(template.getTriples(), rs);
        }
        return result;
    }

    @Override
    public RowSet select() {
        if (query != null && !query.isSelectType()) {
            throw new RuntimeException("SELECT query expected. Got: ["
                    + query.toString() + "]");
        }
        return internalSelect(query);
    }

    @Override
    public Query getQuery() {
        return query;
    }

    @Override
    public String getQueryString() {
        return query == null ? null : query.toString();
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

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

