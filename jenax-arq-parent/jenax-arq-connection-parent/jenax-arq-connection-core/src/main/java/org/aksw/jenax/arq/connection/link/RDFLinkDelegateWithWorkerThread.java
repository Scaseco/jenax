package org.aksw.jenax.arq.connection.link;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.update.UpdateRequest;

/**
 * Wraps an {@link RDFLink} such that all method calls originate from the same
 * (worker) thread.
 *
 * Transactions in jena are per-thread rather than per-connection. This means,
 * that if a main thread starts a write transaction on a connection and later a worker
 * thread wants to perform a write on the same connection it results in a deadlock.
 *
 * This wrapper resolves the deadlock situation at the cost of processing any request
 * from any thread made to the RDFLink sequentially in a single additional thread.
 *
 * @author raven
 *
 */
public class RDFLinkDelegateWithWorkerThread
    implements RDFLink
{
    protected RDFLink delegate;
    protected ExecutorService es;

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

//    public static void submit(ExecutorService executorService, Runnable runnable) {
//        try {
//            executorService.submit(runnable).get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static <T> T submit(ExecutorService executorService, Callable<T> callable) {
        try {
            return executorService.submit(callable).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public void submit(Runnable runnable) {
        submit(() -> { runnable.run(); return null; });
    }

    public <T> T submit(Callable<T> callable) {
        if (es == null) {
            synchronized (this) {
                if (es == null) {
                    es = Executors.newSingleThreadExecutor();
                }
            }
        }

        T result = submit(es, callable);
        return result;
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
        return submit(() -> getDelegate().query(query));
    }

    @Override
    public QueryExecBuilder newQuery() {
        return submit(() -> getDelegate().newQuery());
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

}
