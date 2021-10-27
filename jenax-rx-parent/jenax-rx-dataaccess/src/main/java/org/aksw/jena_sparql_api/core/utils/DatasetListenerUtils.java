package org.aksw.jena_sparql_api.core.utils;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.UpdateContext;
import org.apache.jena.sparql.core.Quad;

public class DatasetListenerUtils {
    public static void notifyListeners(Iterable<DatasetListener> listeners, Diff<Set<Quad>> diff, UpdateContext updateContext) {
        for(DatasetListener listener : listeners) {
            listener.onPreModify(diff, updateContext);
        }
    }
}
