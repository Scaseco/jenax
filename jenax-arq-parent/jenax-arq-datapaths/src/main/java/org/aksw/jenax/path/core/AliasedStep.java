package org.aksw.jenax.path.core;

import java.io.Serializable;
import java.util.Objects;

import org.aksw.jenax.arq.util.node.PathUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

public class AliasedStep
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    protected P_Path0 step;
    protected String alias;

    public AliasedStep(P_Path0 step, String alias) {
        super();
        this.step = step;
        this.alias = alias;
    }

    public AliasedStep(Node node, boolean isForward, String alias) {
        this(PathUtils.createStep(node, isForward), alias);
    }

    public P_Path0 getStep() {
        return step;
    }

    public Node getNode() {
        return step.getNode();
    }

    public boolean isForward() {
        return step.isForward();
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, step);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AliasedStep other = (AliasedStep) obj;
        return Objects.equals(alias, other.alias) && Objects.equals(step, other.step);
    }

    @Override
    public String toString() {
        return "AliasedStep [step=" + step + ", alias=" + alias + "]";
    }
}
