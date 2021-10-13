package org.aksw.jena_sparql_api.constraint.api;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.constraint.util.NodeRanges;
import org.apache.jena.ext.com.google.common.collect.Sets;


/**
 * Description of an entity using different dimensions which can be
 * constrained to values.
 *
 * Subclasses/wrappers/utils may implemented special rules for dependent dimensions.
 * For example, constraining the rdf term type dimension to IRI may olso
 * clear all non-IRI value spaces from the value dimension.
 *
 * @author raven
 *
 */
public class MultiConstraint {
    protected Map<String, NodeRanges> dimensionToConstraint;

    protected boolean isContradicting = false;

    public void intersect(MultiConstraint that) {
        Set<String> commonDimensions = Sets.intersection(this.dimensionToConstraint.keySet(), that.dimensionToConstraint.keySet());


    }


    public void union(MultiConstraint other) {
        for (Entry<String, NodeRanges> e : other.dimensionToConstraint.entrySet()) {
            NodeRanges nr = dimensionToConstraint.computeIfAbsent(e.getKey(), x -> NodeRanges.create());

            nr.stateUnion(e.getValue());
        }

        // If this was non-contradicting before then after a union it will be still
        // non-contracting - otherwise reevaluate

    }


    /* Invoked internally to update cache when needed */
    protected boolean evalContradicting() {
        for (Entry<String, NodeRanges> e : dimensionToConstraint.entrySet()) {
            boolean contrib = e.getValue().isContradicting();
            if (contrib) {
                isContradicting = true;
            }
        }

        return isContradicting;
    }

    // Returns cached state
    public boolean isContradicting() {
        return isContradicting;
    }
}

