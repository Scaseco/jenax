package org.aksw.jenax.path.core;

import java.io.Serializable;
import java.util.Objects;

import org.aksw.jenax.arq.util.node.PathUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Path0;

public class FacetStep
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    protected P_Path0 step;
    protected String alias;

    /** Constants for addressing components of a quad */
    public static final Integer TARGET = null; // Use 0?
    public static final Integer PREDICATE = 1;
    public static final Integer SOURCE = 2;
    public static final Integer GRAPH = 3;
    public static final Integer ANY = -1; // A placeholder to refer to any component - which corresponds to the the triple/quad (or or general tuple expression)

    public static boolean isTarget(Integer component) { return Objects.equals(TARGET, component); }
    public static boolean isPredicate(Integer component) { return Objects.equals(PREDICATE, component); }
    public static boolean isSource(Integer component) { return Objects.equals(SOURCE, component); }
    public static boolean isGraph(Integer component) { return Objects.equals(GRAPH, component); }
    public static boolean isAny(Integer component) { return Objects.equals(ANY, component); }

    /** TODO Include a constant for the graph? */

    /** The component targeted by this step. A step corresponds to a tuple:
     * By default a path points to the values reachable via this step, but in the case of RDF it could also refer to the predicate or graph component. */
    protected Integer targetComponent;

    public static FacetStep fwd(Resource node, String alias) {
        return fwd(node.asNode(), alias);
    }

    public static FacetStep fwd(Node node, String alias) {
        return new FacetStep(node, true, alias, TARGET);
    }


    public FacetStep(Node node, boolean isForward, String alias) {
        this(PathUtils.createStep(node, isForward), alias, null);
    }

    public FacetStep(Node node, boolean isForward, String alias, Integer targetComponent) {
        this(PathUtils.createStep(node, isForward), alias, targetComponent);
    }

    public FacetStep(P_Path0 step, String alias) {
        this(step, alias, null);
    }

    public FacetStep(P_Path0 step, String alias, Integer targetComponent) {
        super();
        this.step = step;
        this.alias = alias;
        this.targetComponent = targetComponent;
    }

    /** Create a copy of this step with the component set to the given value. Used for preallocation of sparql variables for the different components. */
    public FacetStep copyStep(Integer newComponent) {
        return new FacetStep(step, alias, newComponent);
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

    public Integer getTargetComponent() {
        return targetComponent;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, targetComponent, step);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FacetStep other = (FacetStep) obj;
        return Objects.equals(alias, other.alias) && targetComponent == other.targetComponent
                && Objects.equals(step, other.step);
    }

    @Override
    public String toString() {
        return "AliasedStep [step=" + step + ", alias=" + alias + ", targetComponent=" + targetComponent + "]";
    }
}
