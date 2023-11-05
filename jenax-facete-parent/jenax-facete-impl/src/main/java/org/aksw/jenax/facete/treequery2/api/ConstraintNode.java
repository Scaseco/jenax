package org.aksw.jenax.facete.treequery2.api;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

public interface ConstraintNode<R>
    extends RootedFacetTraversable<R, ConstraintNode<R>>, Sortable<ConstraintNode<R>>
{
    ConstraintFacade<? extends ConstraintNode<R>> enterConstraints();

    /**
     * A variable name that uniquely identifies this node in the query model
     * Can be seen as a field id.
     */
    @Deprecated /** not really deprecated; but most code should use asJenaNode as it
    returns a reference that can be evaluated to a variable lazily */
    public Var var();

    default Node asJenaNode() {
        return NodeCustom.of(this);
    }

    public static ScopedFacetPath toScopedFacetPath(ConstraintNode<NodeQuery> constraintNode) {
        NodeQuery nodeQuery = constraintNode.getRoot();
        FacetPath constraintPath = constraintNode.getFacetPath();
        ScopedFacetPath result = NodeQuery.toScopedFacetPath(nodeQuery, constraintPath);
        return result;
    }

    FacetedDataQuery<RDFNode> availableValues();
    FacetedDataQuery<RDFNode> remainingValues();
}
