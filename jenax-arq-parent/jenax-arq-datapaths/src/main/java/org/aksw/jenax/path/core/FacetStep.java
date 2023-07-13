package org.aksw.jenax.path.core;

import java.io.Serializable;
import java.util.Objects;

import org.aksw.commons.util.direction.Direction;
import org.aksw.jenax.arq.util.node.PathUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Path0;

public class FacetStep
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    protected P_Path0 step;
    protected String alias;

    /** The component targeted by this step. A step corresponds to a tuple:
     * By default a path points to the values reachable via this step, but in the case of RDF it could also refer to the predicate or graph component. */
    protected Node targetComponent;

    /** Constants for addressing components of a quad */
    public static final Var TARGET = Var.alloc("target");
    public static final Var PREDICATE = Var.alloc("predicate");
    public static final Var SOURCE = Var.alloc("source");
    public static final Var GRAPH = Var.alloc("graph");
    public static final Var TUPLE = Var.alloc("tuple"); // A placeholder to refer to the tuple rather than one of its components - which corresponds to the the triple/quad (or or general tuple expression)

    public static boolean isTarget(Node component) { return Objects.equals(TARGET, component); }
    public static boolean isPredicate(Node component) { return Objects.equals(PREDICATE, component); }
    public static boolean isSource(Node component) { return Objects.equals(SOURCE, component); }
    public static boolean isGraph(Node component) { return Objects.equals(GRAPH, component); }
    public static boolean isTuple(Node component) { return Objects.equals(TUPLE, component); }

    /** TODO Include a constant for the graph? */

    public static FacetStep of(Node node, Direction direction, String alias, Node component) {
        return new FacetStep(node, direction.isForward(), alias, component);
    }

    public static FacetStep fwd(String iri) {
        return fwd(NodeFactory.createURI(iri));
    }

    public static FacetStep fwd(Resource node) {
        return fwd(node.asNode());
    }

    public static FacetStep fwd(Resource node, String alias) {
        return fwd(node.asNode(), alias);
    }

    public static FacetStep fwd(Node node) {
        return of(node, Direction.FORWARD, null, TARGET);
    }

    public static FacetStep fwd(Node node, String alias) {
        return of(node, Direction.FORWARD, alias, TARGET);
    }

    public static FacetStep bwd(String iri) {
        return bwd(NodeFactory.createURI(iri));
    }

    public static FacetStep bwd(Resource node) {
        return bwd(node.asNode());
    }

    public static FacetStep bwd(Resource node, String alias) {
        return bwd(node.asNode(), alias);
    }

    public static FacetStep bwd(Node node) {
        return of(node, Direction.BACKWARD, null, TARGET);
    }

    public static FacetStep bwd(Node node, String alias) {
        return of(node, Direction.BACKWARD, alias, TARGET);
    }


    public FacetStep(Node node, boolean isForward, String alias) {
        this(PathUtils.createStep(node, isForward), alias, null);
    }

    public FacetStep(Node node, boolean isForward, String alias, Node targetComponent) {
        this(PathUtils.createStep(node, isForward), alias, targetComponent);
    }

    public FacetStep(P_Path0 step, String alias) {
        this(step, alias, null);
    }

    public FacetStep(P_Path0 step, String alias, Node targetComponent) {
        super();
        this.step = step;
        this.alias = alias;
        this.targetComponent = targetComponent;
    }

    /** Create a copy of this step with the component set to the given value. Used for preallocation of sparql variables for the different components. */
    public FacetStep copyStep(Node newComponent) {
        return new FacetStep(step, alias, newComponent);
    }


    public P_Path0 getStep() {
        return step;
    }

    public Node getNode() {
        return step.getNode();
    }

    public Direction getDirection() {
        return Direction.ofFwd(isForward());
    }

    @Deprecated // Use getDirection
    public boolean isForward() {
        return step.isForward();
    }

    public Node getTargetComponent() {
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
        return "FacetStep [step=" + step + ", alias=" + alias + ", targetComponent=" + targetComponent + "]";
    }
}
