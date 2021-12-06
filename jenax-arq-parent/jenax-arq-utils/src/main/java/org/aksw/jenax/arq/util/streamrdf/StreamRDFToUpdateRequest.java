package org.aksw.jenax.arq.util.streamrdf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.aksw.commons.util.concurrent.CompletionTracker;
import org.aksw.jenax.arq.util.prefix.PrefixMappingTrie;
import org.aksw.jenax.arq.util.update.UpdateRequestUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.update.UpdateRequest;


/**
 * A StreamRDF sink that dispatches batches of triples/quads as
 * SPARQL update requests.
 *
 * @author raven
 *
 */
public class StreamRDFToUpdateRequest
    implements StreamRDF
{
    // private static final Quad POISON = Quad.ANY;

    public static final int DEFAULT_BATCH_SIZE = 100;

    // protected LinkSparqlUpdate updateLink;
    protected BlockingQueue<Quad> queue = new LinkedBlockingQueue<>();
    protected int batchSize;
    protected Node graphNode = Quad.defaultGraphIRI;

    protected ReadWriteLock prologueLock = new ReentrantReadWriteLock();

    // protected ExecutorService executorService;
    protected CompletionTracker completionTracker;
    protected Consumer<UpdateRequest> insertHandler;

    protected Prologue prologue;

    public StreamRDFToUpdateRequest(Prologue prologue, int batchSize, ExecutorService executorService, Consumer<UpdateRequest> insertHandler) {
        this.prologue = prologue;
        this.batchSize = batchSize;
        this.insertHandler = insertHandler;
        this.completionTracker = new CompletionTracker(executorService);
    }

    public static StreamRDF createWithTrie(int batchSize, ExecutorService executorService, Consumer<UpdateRequest> insertHandler) {
        return new StreamRDFToUpdateRequest(new Prologue(new PrefixMappingTrie()), batchSize, executorService, insertHandler);
    }

    public void sendBatch() {
        List<Quad> quads = new ArrayList<>();
        queue.drainTo(quads);
        UpdateRequest ur = new UpdateRequest(new UpdateDataInsert(new QuadDataAcc(quads)));

        Lock readLock = prologueLock.readLock();
        try {
            readLock.lock();
            PrefixMapping pm = new PrefixMappingTrie();
            pm.setNsPrefixes(prologue.getPrefixMapping());
            ur.setPrefixMapping(pm);

            String base = prologue.getBaseURI();
            if (base != null) {
                ur.setBaseURI(base);
            }
        } finally {
            readLock.unlock();
        }

        completionTracker.execute(() -> {
            UpdateRequestUtils.optimizePrefixes(ur);
            insertHandler.accept(ur);
        });
    }

    public void checkSendBatch() {
        if (queue.size() > batchSize) {
            sendBatch();
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void triple(Triple triple) {
        quad(new Quad(graphNode, triple));
    }

    @Override
    public void quad(Quad quad) {
        queue.add(quad);
        checkSendBatch();
    }

    @Override
    public void base(String base) {
        sendBatch();
        Lock writeLock = prologueLock.writeLock();
        try {
            writeLock.lock();
            prologue.setBaseURI(base);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void prefix(String prefix, String iri) {
        Lock writeLock = prologueLock.writeLock();
        try {
            writeLock.lock();
            prologue.setPrefix(prefix, iri);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void finish() {
        // queue.add(POISON);
        sendBatch();
//        executorService.shutdown()
        completionTracker.shutdown();
        try {
            // executorService.awaitTermination(1, TimeUnit.MINUTES);
            completionTracker.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void abort() {
        // executorService.shutdownNow();
    }
}

