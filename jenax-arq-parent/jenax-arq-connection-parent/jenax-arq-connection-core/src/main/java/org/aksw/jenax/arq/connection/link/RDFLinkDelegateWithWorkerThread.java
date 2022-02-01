package org.aksw.jenax.arq.connection.link;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

public class RDFLinkDelegateWithWorkerThread
    extends WorkerThreadBase
    implements RDFLink
{
    protected RDFLink delegate;

    public RDFLinkDelegateWithWorkerThread(RDFLink delegate) {
        super();
        this.delegate = delegate;
    }

    public static RDFLink wrap(RDFLink delegate) {
        return new RDFLinkDelegateWithWorkerThread(delegate);
    }

    public RDFLink getDelegate() {
        return delegate;
    }

    @Override
    public void begin(TxnType type) {
        submit(() -> getDelegate().begin(type));
    }

    @Override
    public void begin(ReadWrite readWrite) {
        submit(() -> getDelegate().begin(readWrite));
    }

    @Override
    public boolean promote(Promote mode) {
        return submit(() -> getDelegate().promote(mode));
    }

    @Override
    public void commit() {
        submit(() -> getDelegate().commit());
    }

    @Override
    public void abort() {
        submit(() -> getDelegate().abort());
    }

    @Override
    public void end() {
        submit(() -> getDelegate().end());
    }

    @Override
    public ReadWrite transactionMode() {
        return submit(() -> getDelegate().transactionMode());
    }

    @Override
    public TxnType transactionType() {
        return submit(() -> getDelegate().transactionType());
    }

    @Override
    public boolean isInTransaction() {
        return submit(() -> getDelegate().isInTransaction());
    }

    @Override
    public DatasetGraph getDataset() {
        return submit(() -> getDelegate().getDataset());
    }

    @Override
    public QueryExec query(Query query) {
        return submit(() -> new QueryExecDelegate(getDelegate().query(query)));
    }

    @Override
    public QueryExecBuilder newQuery() {
        return submit(() -> getDelegate().newQuery());
    }

    @Override
    public UpdateExecBuilder newUpdate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(UpdateRequest update) {
        submit(() -> getDelegate().update(update));
    }

    @Override
    public Graph get() {
        return submit(() -> getDelegate().get());
    }

    @Override
    public Graph get(Node graphName) {
        return submit(() -> getDelegate().get(graphName));
    }

    @Override
    public void load(String file) {
        submit(() -> getDelegate().load(file));
    }

    @Override
    public void load(Node graphName, String file) {
        submit(() -> getDelegate().load(graphName, file));
    }

    @Override
    public void load(Graph graph) {
        submit(() -> getDelegate().load(graph));
    }

    @Override
    public void load(Node graphName, Graph graph) {
        submit(() -> getDelegate().load(graphName, graph));
    }

    @Override
    public void put(String file) {
        submit(() -> getDelegate().put(file));
    }

    @Override
    public void put(Node graphName, String file) {
        submit(() -> getDelegate().put(graphName, file));
    }

    @Override
    public void put(Graph graph) {
        submit(() -> getDelegate().put(graph));
    }

    @Override
    public void put(Node graphName, Graph graph) {
        submit(() -> getDelegate().put(graphName, graph));
    }

    @Override
    public void delete(Node graphName) {
        submit(() -> getDelegate().delete(graphName));
    }

    @Override
    public void delete() {
        submit(() -> getDelegate().delete());
    }

    @Override
    public void loadDataset(String file) {
        submit(() -> getDelegate().loadDataset(file));
    }

    @Override
    public void loadDataset(DatasetGraph dataset) {
        submit(() -> getDelegate().loadDataset(dataset));
    }

    @Override
    public void putDataset(String file) {
        submit(() -> getDelegate().putDataset(file));
    }

    @Override
    public void putDataset(DatasetGraph dataset) {
        submit(() -> getDelegate().putDataset(dataset));
    }

    @Override
    public void clearDataset() {
        submit(() -> getDelegate().clearDataset());
    }

    @Override
    public boolean isClosed() {
        return submit(() -> getDelegate().isClosed());
    }

    @Override
    public void close() {
        if (es == null || !es.isShutdown()) {
            submit(() -> getDelegate().close());
            es.shutdownNow();
        }
    }





    class QueryExecDelegate
        implements QueryExec
    {
        protected QueryExec delegate;

        public QueryExecDelegate(QueryExec delegate) {
            super();
            this.delegate = delegate;
        }

        public QueryExec getDelegate() {
            return delegate;
        }

        @Override
        public DatasetGraph getDataset() {
            return submit(() -> getDelegate().getDataset());
        }

        @Override
        public Context getContext() {
            return submit(() -> getDelegate().getContext());
        }

        @Override
        public Query getQuery() {
            return submit(() -> getDelegate().getQuery());
        }

        @Override
        public String getQueryString() {
            return submit(() -> getDelegate().getQueryString());
        }

        @Override
        public RowSet select() {
            return submit(() -> new RowSetDelegate(getDelegate().select(), es));
        }

        @Override
        public Graph construct(Graph graph) {
            return submit(() -> getDelegate().construct(graph));
        }

        @Override
        public Iterator<Triple> constructTriples() {
            return submit(() -> new IteratorDelegateWithWorkerThread<>(getDelegate().constructTriples(), es));
        }

        @Override
        public Iterator<Quad> constructQuads() {
            return submit(() -> new IteratorDelegateWithWorkerThread<>(getDelegate().constructQuads(), es));
        }

        @Override
        public DatasetGraph constructDataset(DatasetGraph dataset) {
            return submit(() -> getDelegate().constructDataset(dataset));
        }

        @Override
        public Graph describe(Graph graph) {
            return submit(() -> getDelegate().describe(graph));
        }

        @Override
        public Iterator<Triple> describeTriples() {
            return submit(() -> new IteratorDelegateWithWorkerThread<>(getDelegate().describeTriples(), es));
        }

        @Override
        public boolean ask() {
            return submit(() -> getDelegate().ask());
        }

        @Override
        public JsonArray execJson() {
            return submit(() -> getDelegate().execJson());
        }

        @Override
        public Iterator<JsonObject> execJsonItems() {
            return submit(() -> new IteratorDelegateWithWorkerThread<>(getDelegate().execJsonItems(), es));
        }

        @Override
        public void abort() {
            submit(() -> getDelegate().abort());
        }

        @Override
        public void close() {
            submit(() -> getDelegate().close());
        }

        @Override
        public boolean isClosed() {
            return submit(() -> getDelegate().isClosed());
        }

    }



    static class RowSetDelegate
        extends IteratorDelegateWithWorkerThread<Binding, RowSet>
        implements RowSet
    {
        protected RowSet delegate;

        public RowSetDelegate(RowSet delegate, ExecutorService es) {
            super(delegate, es);
        }

        @Override
        protected Binding copy(Binding item) {
            return BindingFactory.copy(item);
        }

        @Override
        public List<Var> getResultVars() {
            return helper.submit(() -> getDelegate().getResultVars());
        }

        @Override
        public long getRowNumber() {
            return helper.submit(() -> getDelegate().getRowNumber());
        }
    }


}



