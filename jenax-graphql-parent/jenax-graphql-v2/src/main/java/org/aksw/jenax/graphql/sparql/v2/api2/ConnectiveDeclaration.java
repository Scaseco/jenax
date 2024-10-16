package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

/** Describes the variables for a {@link Connective} but does not specify the graph pattern. */
public class ConnectiveDeclaration
    extends BasicConnectInfo
{
    /** The variables of the given element which to join on the parent variables. */
    protected final List<Var> connectVars;

    public ConnectiveDeclaration(List<Var> defaultTargetVars, Set<Var> visibleVars, List<Var> connectVars) {
        super(defaultTargetVars, visibleVars);
        this.connectVars = connectVars;
    }

    public List<Var> getConnectVars() {
        return connectVars;
    }
}
