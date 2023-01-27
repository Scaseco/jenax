package org.aksw.jenax.arq.util.dataset;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DynamicDatasets.DynamicDatasetGraph;

public class DynamicDatasetUtils {

    /** Returns the argument unless it is a DynamicDataset for which
     * {@link #isUnwrappable(DynamicDatasetGraph)} is true. In that case
     * {@link DynamicDatasetGraph#getOriginal()} is returned.
     **/
    public static DatasetGraph unwrap(DatasetGraph dg) {
        DatasetGraph result = dg;
        if (dg instanceof DynamicDatasetGraph) {
            DynamicDatasetGraph ddg = (DynamicDatasetGraph)dg;
            if (isUnwrappable(ddg)) {
                result = ddg.getOriginal();
            }
        }
        return result;
    }

    /**
     * Returns true iff the argument's original default and named graphs are non-null.
     * Typically, this shouldn't happen. This method mainly exists for robustness as to
     * avoid null pointer exceptions.
     */
    public static boolean isUnwrappable(DynamicDatasetGraph ddg) {
        boolean result = ddg.getOriginal() != null &&
                ddg.getOriginalDefaultGraphs() != null && ddg.getOriginalNamedGraphs() != null;
        return result;
    }

    /** Convenience method to cast the argument as a DynamicDatasetGraph if it is an unwrappable one. */
    public static DynamicDatasetGraph asUnwrappableDynamicDatasetOrNull(DatasetGraph dg) {
        DynamicDatasetGraph result = null;
        if (dg instanceof DynamicDatasetGraph) {
            DynamicDatasetGraph ddg = (DynamicDatasetGraph)dg;
            if (isUnwrappable(ddg)) {
                result = ddg;
            }
        }
        return result;
    }

    /**
     * Returns the argument unless it is a DynamicDataset for which
     * {@link #isUnwrappable(DynamicDatasetGraph)} is true. In that case
     * {@link DynamicDatasetGraph#getOriginal()} is returned.
     */
    public static DatasetGraph unwrapOriginal(DatasetGraph dg) {
        DatasetGraph result = dg;
        if (dg instanceof DynamicDatasetGraph) {
            DynamicDatasetGraph ddg = (DynamicDatasetGraph)dg;
            if (isUnwrappable(ddg)) {
                result = ddg.getOriginal();
            }
        }
        return result;
    }

    /**
     * Returns the given arguments as a pair unless the dataset is an unwrappable DynamicDataset.
     * In that case, query's default/named graphs are modified in-place to match those of the dataset.
     * The returned pair holds the modified query and the original (unwrapped) dataset.
     */
    public static Pair<Query, DatasetGraph> unwrap(Query query, DatasetGraph dataset) {
        Pair<Query, DatasetGraph> result = null;
        if (dataset instanceof DynamicDatasetGraph) {
            DynamicDatasetGraph ddg = (DynamicDatasetGraph)dataset;
            if (isUnwrappable(ddg)) {
                // Query copy = query.cloneQuery(); Should we clone to be safe?
                query.getGraphURIs().clear();
                query.getNamedGraphURIs().clear();
                ddg.getOriginalDefaultGraphs().forEach(n -> query.getGraphURIs().add(n.getURI()));
                ddg.getOriginalNamedGraphs().forEach(n -> query.getNamedGraphURIs().add(n.getURI()));
                result = Pair.create(query, ddg.getOriginal());
            }
        }
        if (result == null) {
            result = Pair.create(query, dataset);
        }
        return result;
    }
}
