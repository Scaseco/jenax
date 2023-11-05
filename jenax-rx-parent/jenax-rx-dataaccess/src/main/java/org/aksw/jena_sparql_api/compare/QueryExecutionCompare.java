package org.aksw.jena_sparql_api.compare;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.commons.collections.diff.ListDiff;
import org.aksw.commons.util.io.out.OutputStreamUtils;
import org.aksw.commons.util.string.StringUtils;
import org.aksw.jenax.arq.util.binding.ResultSetCompareUtils;
import org.aksw.jenax.arq.util.binding.TableUtils;
import org.aksw.jenax.arq.util.triple.ModelDiff;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 1/5/12
 *         Time: 12:33 AM
 */
public class QueryExecutionCompare
    implements QueryExecution
{

    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionCompare.class);



    public static ModelDiff compareModel(Model a, Model b) {
        ModelDiff result = new ModelDiff();

        result.getAdded().add(b);
        result.getAdded().remove(a);

        result.getRemoved().add(a);
        result.getRemoved().remove(b);

        return result;
    }

    public static Diff<Dataset> compareDataset(Dataset a, Dataset b) {
        Diff<Dataset> result = new Diff<>(DatasetFactory.create(), DatasetFactory.create(), DatasetFactory.create());

        result.getAdded().asDatasetGraph().addAll(b.asDatasetGraph());
        a.asDatasetGraph().find().forEachRemaining(result.getAdded().asDatasetGraph()::delete);

        result.getRemoved().asDatasetGraph().addAll(a.asDatasetGraph());
        b.asDatasetGraph().find().forEachRemaining(result.getRemoved().asDatasetGraph()::delete);

        return result;
    }


    private boolean isOrdered; // Whether the result sets are ordered
    private QueryExecution a;
    private QueryExecution b;

    private Query query = null;
    private String queryString;

    private Diff<Table> resultSetDiff = null; // The diff after the query execution
    private ModelDiff modelDiff = null;
    private Diff<Dataset> datasetDiff = null;
    private Diff<Boolean> askDiff = null;


    public boolean isDifference() {
        if(resultSetDiff != null) {
            return !(resultSetDiff.getAdded().isEmpty() && resultSetDiff.getRemoved().isEmpty());
        } else if(modelDiff != null) {
            return !(modelDiff.getAdded().isEmpty() && modelDiff.getRemoved().isEmpty());
        } else if(askDiff != null) {
            return !(askDiff.getAdded() == askDiff.getRemoved());
        } else {
            throw new RuntimeException("Cannot retrieve difference because query was not executed.");
        }
    }

    public QueryExecutionCompare(Query query, QueryExecution a, QueryExecution b, boolean isOrdered) {
        this(query, "" + query, a, b, isOrdered);
    }

    public QueryExecutionCompare(Query query, String queryString, QueryExecution a, QueryExecution b, boolean isOrdered) {
        this.query = query;
        this.queryString = queryString;
        this.a = a;
        this.b = b;
        this.isOrdered = isOrdered;
    }

    /**
     * Set the initial association of variables and values.
     * May not be supported by all QueryExecution implementations.
     *
     * @param binding
     */
    @Override
    public void setInitialBinding(QuerySolution binding) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setInitialBinding(Binding binding) {
        throw new RuntimeException("not implemented");
    }

    /**
     * The dataset against which the query will execute.
     * May be null, implying it is expected that the query itself
     * has a dataset description.
     */
    @Override
    public Dataset getDataset() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * The properties associated with a query execution -
     * implementation specific parameters  This includes
     * Java objects (so it is not an RDF graph).
     * Keys should be URIs as strings.
     * May be null (this implementation does not provide any configuration).
     */
    @Override
    public Context getContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * The query associated with a query execution.
     * May be null (QueryExecution may have been created by other means)
     */
    @Override
    public Query getQuery() {
        return query;
    }

    /**
     * Execute a SELECT query
     */
    @Override
    public ResultSet execSelect() {
        ResultSetRewindable x;
        ResultSetRewindable y;
        long timeA = -1;
        long timeB = -1;
        try {
            Stopwatch asw = Stopwatch.createStarted();
            ResultSet r = a.execSelect();
            x = ResultSetFactory.makeRewindable(r);
            //System.out.println("ResultSet [A]");
            //ResultSetFormatter.out(System.out, x);
            x.reset();
            timeA = asw.stop().elapsed(TimeUnit.MILLISECONDS);

            Stopwatch bsw = Stopwatch.createStarted();
            ResultSet s = b.execSelect();
            y = ResultSetFactory.makeRewindable(s);
            //System.out.println("ResultSet [B]");
            //ResultSetFormatter.out(System.out, y);
            y.reset();
            timeB = bsw.stop().elapsed(TimeUnit.MILLISECONDS);
        } catch(RuntimeException e) {
            // Set diff in order to indicate that the execution was performed
            resultSetDiff = Diff.<Table>create(TableFactory.createEmpty(), TableFactory.createEmpty()); //new ListDiff<>();
            throw new RuntimeException(e);
        }



        ListDiff<Binding> tmp = (isOrdered)
                ? ResultSetCompareUtils.compareOrdered(x, y)
                : ResultSetCompareUtils.compareUnordered(x, y);

        resultSetDiff = Diff.create(
                TableUtils.createTable(Var.varList(x.getResultVars()), tmp.getAdded()),
                TableUtils.createTable(Var.varList(y.getResultVars()), tmp.getRemoved()));

        // Reset x once more in order to return it
        x.reset();

        logResultSet();
        String relation = timeA == timeB ? "=" : (timeA > timeB ? ">" : "<");
        logger.debug("Execution time relation: [" + timeA + " " + relation + " " + timeB + "]");

        return x;
    }

    public void log(long added, long removed) {
        String msg = added + "\t" + removed + "\t" + StringUtils.urlEncode("" + query);

        boolean isEqual = added == 0 && removed == 0;
        if(isEqual) {
            logger.info("[ OK ] " + msg);
        } else {
            logger.warn("[FAIL] " + msg);
        }
    }


    public void log(Table ra, Table rb) {
        log(ra.size(), rb.size());
        boolean isEqual = ra.isEmpty() && rb.isEmpty();

        if(!isEqual) {
            ResultSet rsa = TableUtils.toResultSet(ra);
            ResultSet rsb = TableUtils.toResultSet(rb);

            logger.debug("Differences detected for query: \n" + queryString);
            logger.debug("Excessive:\n" + ResultSetFormatter.asText(rsa));
            logger.debug("Missing:\n" + ResultSetFormatter.asText(rsb));
        }
    }

    public void logResultSet() {
        log(resultSetDiff.getAdded(), resultSetDiff.getRemoved());
    }

    public void logModel() {
        log(modelDiff.getAdded().size(), modelDiff.getRemoved().size());

        logger.debug("Query: " + query);
        logger.debug("Excessive:\n" + toString(modelDiff.getAdded(), RDFFormat.TURTLE_PRETTY));
        logger.debug("Missing:\n" + toString(modelDiff.getRemoved(), RDFFormat.TURTLE_PRETTY));
    }

    public void logDataset() {
        log(datasetDiff.getAdded().asDatasetGraph().size(), datasetDiff.getRemoved().asDatasetGraph().size());

        logger.debug("Query: " + query);
        logger.debug("Excessive:\n" + toString(datasetDiff.getAdded(), RDFFormat.TRIG_PRETTY));
        logger.debug("Missing:\n" + toString(datasetDiff.getRemoved(), RDFFormat.TRIG_PRETTY));
    }

    public static String toString(Model model, RDFFormat format) {
        String result = OutputStreamUtils.toStringUtf8(out -> RDFDataMgr.write(out, model, format));
        return result;
    }

    public static String toString(Dataset dataset, RDFFormat format) {
        String result = OutputStreamUtils.toStringUtf8(out -> RDFDataMgr.write(out, dataset, format));
        return result;
    }

    public void logAsk() {
        boolean  added = askDiff.getAdded();
        boolean  removed = askDiff.getRemoved();

        String msg = added + "\t" + removed + "\t" + StringUtils.urlEncode("" + query);

        if(added == removed) {
            logger.trace("[ OK ] " + msg);
        } else {
            logger.warn("[FAIL] " + msg);
        }
    }


    /**
     * Execute a CONSTRUCT query
     */
    @Override
    public Model execConstruct() {
        return execConstruct(ModelFactory.createDefaultModel());
    }

    /**
     * Execute a CONSTRUCT query, putting the statements into 'model'.
     *
     * @return Model The model argument for casaded code.
     */
    @Override
    public Model execConstruct(Model model) {
        Model x;
        Model y;
        try {
             x = a.execConstruct();
             y = b.execConstruct();
        } catch(RuntimeException e) {
            // Set diff in order to indicate that the execution was performed
            modelDiff = new ModelDiff();
            throw e;
        }

        modelDiff = compareModel(x, y);

        logModel();

        return x;
    }

    /**
     * Execute a DESCRIBE query
     */
    @Override
    public Model execDescribe() {
        return execDescribe(ModelFactory.createDefaultModel());
    }

    /**
     * Execute a DESCRIBE query, putting the statements into 'model'.
     *
     * @return Model The model argument for casaded code.
     */
    @Override
    public Model execDescribe(Model model) {
        Model x;
        Model y;
        try {
             x = a.execDescribe();
             y = b.execDescribe();
        } catch(RuntimeException e) {
            // Set diff in order to indicate that the execution was performed
            modelDiff = new ModelDiff();
            throw e;
        }

        modelDiff = compareModel(x, y);

        logModel();

        return x;
    }

    /**
     * Execute an ASK query
     */
    @Override
    public boolean execAsk() {
        boolean x;
        boolean y;

        try {
             x = a.execAsk();
             y = b.execAsk();
        } catch(RuntimeException e) {
            // Set diff in order to indicate that the execution was performed
            askDiff = new Diff<Boolean>(false, false, null);
            throw e;
        }

        askDiff = new Diff<Boolean>(x, y, null);

        logAsk();

        return x;
    }

    /**
     * Stop in mid execution.
     * This method can be called in parallel with other methods on the
     * QueryExecution object.
     * There is no guarantee that the concrete implementation actual
     * will stop or that it will do so immediately.
     * No operations on the query execution or any associated
     * result set are permitted after this call and may cause exceptions to be thrown.
     */
    @Override
    public void abort() {
        try {
            a.abort();
        } finally {
            b.abort();
        }
    }

    /**
     * Close the query execution and stop query evaluation as soon as convenient.
     * It is important to close query execution objects in order to release
     * resources such as working memory and to stop the query execution.
     * Some storage subsystems require explicit ends of operations and this
     * operation will cause those to be called where necessary.
     * No operations on the query execution or any associated
     * result set are permitted after this call.
     * This method should not be called in parallel with other methods on the
     * QueryExecution object.
     */
    @Override
    public void close() {
        try {
            a.close();
        } finally {
            b.close();
        }
    }

    /**
     * Set a timeout on the query execution.
     * Processing will be aborted after the timeout (which starts when the approprate exec call is made).
     * Not all query execution systems support timeouts.
     * A timeout of less than zero means no timeout.
     */
    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        a.setTimeout(timeout, timeoutUnits);
        b.setTimeout(timeout, timeoutUnits);
    }

    /**
     * Set time, in milliseconds
     *
     * @see #setTimeout(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void setTimeout(long timeout) {
        a.setTimeout(timeout);
        b.setTimeout(timeout);
    }

    /**
     * Set timeouts on the query execution; the first timeout refers to time to first result,
     * the second refers to overall query execution after the first result.
     * Processing will be aborted if a timeout expires.
     * Not all query execution systems support timeouts.
     * A timeout of less than zero means no timeout; this can be used for timeout1 or timeout2.
     */
    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        a.setTimeout(timeout1, timeUnit1, timeout2, timeUnit2);
        b.setTimeout(timeout1, timeUnit1, timeout2, timeUnit2);
    }

    /**
     * Set time, in milliseconds
     *
     * @see #setTimeout(long, java.util.concurrent.TimeUnit, long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void setTimeout(long timeout1, long timeout2) {
        a.setTimeout(timeout1, timeout2);
        b.setTimeout(timeout1, timeout2);
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        Model tmp = execConstruct();
        Iterator<Triple> result = tmp.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toSet().iterator();
        return result;
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        Model tmp = execDescribe();
        Iterator<Triple> result = tmp.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toSet().iterator();
        return result;
    }

    @Override
    public long getTimeout1() {
        return a.getTimeout1();
    }

    @Override
    public long getTimeout2() {
        return a.getTimeout2();
    }

    /* (non-Javadoc)
     * @see org.apache.jena.query.QueryExecution#isClosed()
     */
    @Override
    public boolean isClosed() {
        return a.isClosed() && b.isClosed();
    }

    @Override
    public Iterator<Quad> execConstructQuads() {
        Dataset tmp = execConstructDataset();
        Iterator<Quad> result = tmp.asDatasetGraph().find();
        return result;
    }

    @Override
    public Dataset execConstructDataset() {
        return execConstructDataset(DatasetFactory.create());
    }

    @Override
    public Dataset execConstructDataset(Dataset dataset) {
        Dataset x;
        Dataset y;
        try {
             x = a.execConstructDataset();
             y = b.execConstructDataset();
        } catch(RuntimeException e) {
            // Set diff in order to indicate that the execution was performed
            datasetDiff = new Diff<>(DatasetFactory.create(), DatasetFactory.create(), DatasetFactory.create());
            throw e;
        }

        datasetDiff = compareDataset(x, y);

        logDataset();

        return x;
    }

    @Override
    public JsonArray execJson() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public String getQueryString() {
        return null;
    }


    /*
    @Override
    public Iterator<Triple> execConstructTriples() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }*/
}
