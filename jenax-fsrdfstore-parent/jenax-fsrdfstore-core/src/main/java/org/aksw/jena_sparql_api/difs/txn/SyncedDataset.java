package org.aksw.jena_sparql_api.difs.txn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.aksw.commons.txn.impl.ContentSync;
import org.aksw.commons.txn.impl.FileSyncImpl;
import org.aksw.commons.txn.impl.PathState;
import org.aksw.jenax.arq.dataset.diff.DatasetGraphDiff;
import org.aksw.jenax.arq.util.quad.SetFromDatasetGraph;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;


public class SyncedDataset {
    private static final Logger logger = LoggerFactory.getLogger(SyncedDataset.class);

    /**
     * A class that holds the original and current state of path metadata.
     *
     * @author raven
     */
    public static class State {
        protected PathState originalState;
        protected PathState currentState;

        public State(PathState originalState, PathState currentState) {
            super();
            this.originalState = originalState;
            this.currentState = currentState;
        }

        public PathState getOriginalState() {
            return originalState;
        }

        public void setOriginalState(PathState originalState) {
            this.originalState = originalState;
        }

        public PathState getCurrentState() {
            return currentState;
        }

        public void setCurrentState(PathState currentState) {
            this.currentState = currentState;
        }

        @Override
        public String toString() {
            return "State [originalState=" + originalState + ", currentState=" + currentState + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((currentState == null) ? 0 : currentState.hashCode());
            result = prime * result + ((originalState == null) ? 0 : originalState.hashCode());
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
            State other = (State) obj;
            if (currentState == null) {
                if (other.currentState != null)
                    return false;
            } else if (!currentState.equals(other.currentState))
                return false;
            if (originalState == null) {
                if (other.originalState != null)
                    return false;
            } else if (!originalState.equals(other.originalState))
                return false;
            return true;
        }
    }

    protected FileSyncImpl fileSync;

    protected State state;
    protected DatasetGraph originalState;
    protected DatasetGraphDiff diff = null;

    // TODO allowEmptyGraphs is probably not needed in here - the diff has a set of removed graphs
    // and the outside code can determine whether to mark a graph as removed if it is empty

    public SyncedDataset(FileSyncImpl fileSync) {
        super();
        this.fileSync = fileSync;
    }


    public static Instant getTimestamp(Path path) {
        Instant result = null;
        try {
            if (Files.exists(path)) {
                FileTime timestamp = Files.getLastModifiedTime(path);
                if (timestamp == null) {
                    timestamp = FileTime.fromMillis(0l);
                }

                result = timestamp.toInstant();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public State getState() {
        Path originalSourcePath = fileSync.getOldContentPath();
        Path diffSourcePath = fileSync.getCurrentPath();

        Instant originalTimestamp = getTimestamp(originalSourcePath);
        Instant diffTimestamp = diffSourcePath == originalSourcePath
                ? originalTimestamp
                : getTimestamp(diffSourcePath);

        State result = new State(
            new PathState(originalSourcePath, originalTimestamp),
            new PathState(diffSourcePath, diffTimestamp)
        );

        logger.debug("Read state: " + result);

        return result;
    }

    public void updateState() {
        this.state = getState();
    }

    protected void readData(DatasetGraph datasetGraph, InputStream in) {
        RDFDataMgrEx.readAsGiven(originalState, in, Lang.TRIG);
    }

    protected void writeData(OutputStream out, DatasetGraph datasetGraph) {
        RDFDataMgrEx.writeAsGiven(out, datasetGraph, RDFFormat.TRIG_BLOCKS, null);
    }

    protected DatasetGraph newDatasetGraph() {
//        return DatasetGraphFactoryEx.createInsertOrderPreservingDatasetGraph();
         // return new DatasetGraphFactory().createTxnMem();
        return DatasetGraphFactory.create();
    }

    protected DatasetGraphDiff newDatasetGraphDiff(DatasetGraph base) {
        return DatasetGraphDiff.createNonTxn(base);
    }

    public void forceLoad() {
        state = getState();

        originalState = newDatasetGraph();

        try (InputStream in = Files.newInputStream(state.getCurrentState().getPath())) {
            readData(originalState, in);
        } catch (AccessDeniedException ex) {
            // FIXME The file may not exist but it may also be an authorization issue
            logger.warn("Access denied: " + ExceptionUtils.getRootCauseMessage(ex));
        } catch (NoSuchFileException ex) {
            // Ignore - this leads to an empty dataset
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!state.getCurrentState().getPath().equals(state.getOriginalState().getPath())) {
            DatasetGraph n = newDatasetGraph();
            try (InputStream in = Files.newInputStream(state.getCurrentState().getPath())) {
                readData(n, in);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            Set<Quad> oldQuads = SetFromDatasetGraph.wrap(originalState);
            Set<Quad> newQuads = SetFromDatasetGraph.wrap(n);

            Set<Quad> addedQuads = Sets.difference(newQuads, oldQuads);
            Set<Quad> removedQuads = Sets.difference(oldQuads, newQuads);

            diff = newDatasetGraphDiff(originalState);
            addedQuads.forEach(diff.getAdded()::add);
            removedQuads.forEach(diff.getRemoved()::add);
        } else {
            diff = newDatasetGraphDiff(originalState);
        }
    }

    public void ensureLoaded() {
        if (originalState == null) {
            forceLoad();
        }
    }

    public DatasetGraph getOriginalState() {
        ensureLoaded();
        return originalState;
    }

    public DatasetGraph getCurrentState() {
        ensureLoaded();
        return diff;
    }

    public DatasetGraphDiff getDiff() {
        return diff;
    }

    public DatasetGraph getAdditions() {
        return diff.getAdded();
    }

    public DatasetGraph getDeletions() {
        return diff.getRemoved();
    }

    public ContentSync getEntity() {
        return fileSync;
    }

//    public void load() {
//        ensureLoaded();
//    }

    public DatasetGraphDiff get() {
        // ensureLoaded();
        updateIfNeeded();
        return diff;
    }

    /**
     * Returns true if there are pending changes in memory; i.e. the set of added/removed triples is non-empty.
     *
     * @return
     */
    public boolean isDirty() {
        boolean result = diff != null && !(
                diff.getAdded().isEmpty() &&
                diff.getRemoved().isEmpty() &&
                diff.getAddedGraphs().isEmpty() &&
                diff.getRemovedGraphs().isEmpty());
        return result;
    }


    public void updateIfNeeded() {
        State verify = getState();

        if (!verify.equals(state)) {
            if (!isDirty()) {
                forceLoad();
            } else {
                throw new RuntimeException("Dataset was modified while there were pending changes");
            }
        }
    }

    public void ensureUpToDate() {
        Objects.requireNonNull(state);

        // Check the time stamps of the source resources
        State verify = getState();

        if (!verify.equals(state)) {
            throw new RuntimeException(
                String.format("Content of files was changed externally since it was loaded:\nExpected:\n%s: %s\n%s: %s\nActual:\n%s: %s\n%s: %s",
                state.getOriginalState().getPath(),
                state.getOriginalState().getTimestamp(),
                state.getCurrentState().getPath(),
                state.getCurrentState().getTimestamp(),
                verify.getOriginalState().getPath(),
                verify.getOriginalState().getTimestamp(),
                verify.getCurrentState().getPath(),
                verify.getCurrentState().getTimestamp()
            ));
        }
    }

//	public Synced set(T instance) {
//		this.instance = instance;
//	}

    /** Returns true if every graph (named or default) has zero triples */
    public static boolean isEffectivelyEmpty(DatasetGraph dg) {
        boolean result =
            Optional.ofNullable(dg.getDefaultGraph()).map(Graph::isEmpty).orElse(true) &&
            Streams.stream(dg.listGraphNodes())
            .allMatch(g -> {
                Graph graph = dg.getGraph(g);
                boolean r = graph.isEmpty();
                return r;
            });

        return result;
    }

    public void save() {
        if (isDirty()) {
            try {
                ensureUpToDate();

                fileSync.putContent(out -> {
                    // FIXME We need to derive a new dataset (view) that has
                    // all empty graphs from diff removed (hidden)

                    boolean isEmpty = isEffectivelyEmpty(diff);

                    if (!isEmpty) {
                        writeData(out, diff);
                    }
                });

                diff.materialize();
                // Update metadata
                updateState();



            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
