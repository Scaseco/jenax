package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

/**
 * Base class for objects that contain multiple selections.
 * Sub classes are Connective and Fragment.
 */
public abstract class BasicConnectInfo {
    protected final List<Var> defaultTargetVars;

    // XXX Visible is ambiguous: The in-scope variables vs the "exported" variables (it may be desirable to export only a subset of the visible ones)
    // Right now visible vars should capture all variables visible in the graph pattern OpVars.visibleVars
    protected final Set<Var> visibleVars;

    public BasicConnectInfo(List<Var> defaultTargetVars, Set<Var> visibleVars) {
        super();
        this.defaultTargetVars = defaultTargetVars;
        this.visibleVars = visibleVars;
    }

    /**
     * The set of variables that fields can connect to.
     * For a fragment spread, these variables must be mapped to those of an {@link Element}.
     */
    public Set<Var> getVisibleVars() {
        return visibleVars;
    }

    /**
     * The default target variables of this element. If a sub field's parentVars is not set then these variables will be used instead.
     * If set, then defaultTargetVars must be a subset of visibleVars.
     */
    public List<Var> getDefaultTargetVars() {
        return defaultTargetVars;
    }
}
