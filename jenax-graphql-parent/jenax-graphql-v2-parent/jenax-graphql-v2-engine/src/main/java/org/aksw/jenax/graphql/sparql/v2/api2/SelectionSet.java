package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class SelectionSet<T>
    extends BasicConnectInfo
{
    protected final Map<String, T> selections;

    public SelectionSet(List<Var> defaultTargetVars, Set<Var> visibleVars, Map<String, T> selections) {
        super(defaultTargetVars, visibleVars);
        this.selections = selections;
    }

    public Collection<T> getSelections() {
        return selections.values();
    }

    public Map<String, T> getSelectionMap() {
        return selections;
    }
}
