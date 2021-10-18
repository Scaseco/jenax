package org.aksw.jenax.arq.util.dataset;

import org.apache.jena.sparql.core.DatasetDescription;

public class DatasetDescriptionUtils {

    /**
     * Add all default- and named graph iris from source to target
     * TODO Prevent duplicates
     */
    public static void mergeInto(DatasetDescription target, DatasetDescription source) {
        target.addAllDefaultGraphURIs(source.getDefaultGraphURIs());
        target.addAllNamedGraphURIs(source.getNamedGraphURIs());
    }

}
