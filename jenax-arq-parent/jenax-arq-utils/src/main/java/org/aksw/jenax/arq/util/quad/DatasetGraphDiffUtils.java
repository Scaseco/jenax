package org.aksw.jenax.arq.util.quad;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class DatasetGraphDiffUtils {

    /** Convert a diff of dataset graphs to one using views of sets of quads */
    public static Diff<Set<Quad>> wrapDatasetGraph(Diff<? extends DatasetGraph> diff) {
        SetFromDatasetGraph added = new SetFromDatasetGraph(diff.getAdded());
        SetFromDatasetGraph removed = new SetFromDatasetGraph(diff.getRemoved());

        Diff<Set<Quad>> result = Diff.<Set<Quad>>create(added, removed);
        return result;
    }
}
