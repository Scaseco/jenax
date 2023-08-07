package org.aksw.facete.v3.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.util.Directed;
import org.aksw.facete.v3.api.traversal.TraversalNode;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetPathOps;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

import com.google.common.annotations.Beta;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;


/**
 * An object backed by the set of resources at a certain (possibly empty) path of properties.
 *
 *
 * @author Claus Stadler, Jul 23, 2018
 *
 */
public interface FacetNode
    extends TraversalNode<FacetNode, FacetDirNode, FacetMultiNode>, Castable
{

    default FacetNode traverse(FacetPath path) {
        return goTo(this, path);
    }

    static FacetNode goTo(FacetNode start, FacetPath path) {
        FacetNode current = path.isAbsolute() ? start.root() : start;
        for (FacetStep step : path.getSegments()) {
            Direction dir = Direction.ofFwd(step.isForward());
            Node node = step.getNode();
            FacetMultiNode fmn = current.step(node, dir);

            String alias = step.getAlias();
            if (alias == null || alias.isEmpty()) {
                current = fmn.one();
            } else {
                current = fmn.viaAlias(alias);
            }
        }
        return current;
    }

    /**
     * TODO Maybe remove this method; do we really need it?
     *
     * The list of taken steps to reach the current node.
     * The last item in the list is this node together with the reaching direction - UNLESS
     * this node is the root, then the list is empty.
     *
     * The path never includes the root node.
     *
     *
     * @return
     */
    default List<Directed<FacetNode>> path() {

        List<Directed<FacetNode>> result =
                Streams.stream(
                        Traverser.<FacetNode>forTree(x ->
                        Optional.ofNullable(x.parent())
                                .map(Collections::singleton)
                                .orElse(Collections.emptySet()))
                        .depthFirstPostOrder(this))
                .filter(x ->  x.parent() != null)
//              .map(x -> PathUtils.createStep(pathAccessor.getPredicate(pathAccessor.getParent(x)), !pathAccessor.isReverse(pathAccessor.getParent(x))))
                .map(x -> new Directed<>(x, x.reachingDirection().isBackward()))
                .collect(Collectors.toList());


        return result;
    }


    // Newer API intended to supersede path()
    default FacetPath facetPath() {
        List<Directed<FacetNode>> list = path();
        List<FacetStep> steps = list.stream().map(dfn -> {
            boolean isFwd = dfn.isForward();
            FacetNode fn = dfn.getValue();
            FacetStep step = fn.reachingFacetStep();

            if (!isFwd) {
                step = step.toggleDirection();
            }
            return step;
        }).collect(Collectors.toList());
        return FacetPath.newAbsolutePath(steps);
    }

    FacetedQuery query();

    /**
     * Change the root of this FacetNode's faceted query to this node.
     * All transitive parents of this node are turned to children.
     *
     * Implementations of this operation should leave all FacetNodes intact.
     * FacetDirNodes may become invalidated by this operation under certain conditions.
     *
     *
     *
     *
     * TODO Below is probably outdated by now - check
     * It is not totally clear to me, what exact changes this method should do, but the corner stones are:
     * - There should be no changes to the id of all nodes in the rdf model corresponding to
     *   any of FacetNode, FacetDirNode and Constraint
     *   -> Clarify, whether the nodes used for the map structures on BgpMultiNode can remain the same
     *
     *   Ideally, changing the root back should give exactly the same query as before.
     *
     * - The changes are
     *   - Set the query()'s root to this node
     *   - Update the directions of the transitions accordingly
     *   - As this node becomes the root, its underlying BgpNode's parent must become null in the process
     *
     *
     * @return
     */
    @Beta
    FacetNode chRoot();

    @Beta
    FacetNode chFocus();


    FacetNode as(String varName);
    FacetNode as(Var var);
    Var alias();


    FacetNode parent();


    /**
     * Create a faceted query over the values of this node without constraining the set of matched items of the outer query.
     * This essentially allows for placing filters into OPTIONAL blocks:
     *
     * SELECT ?person ?city
     * {
     *   ?person hasAdress ?address
     *   OPTIONAL {
     *     ?address hasCountry ?country . FILTER(?country IN (...))
     *     ?address hasCity ?city . # Projected path with exists constraint on the sub facet
     *   }
     * }
     *
     *
     *
     */
    // FacetNode subFacetNode();

    // AliasedStep reachingStep();
    Direction reachingDirection();
    Node reachingPredicate();
    String reachingAlias();
    Node targetComponent();

    default FacetStep reachingFacetStep() {
        Direction d = reachingDirection();
        Node p = reachingPredicate();
        String a = reachingAlias();
        Node c = targetComponent();
        Node cc = c == null ? FacetStep.TARGET : c; // TODO targetComponent should never be null but right now it can happen
        FacetStep result = FacetStep.of(p, org.aksw.commons.util.direction.Direction.ofFwd(d.isForward), a, cc);
        return result;
    }

    default FacetNode resolve(Path<FacetStep> facetPath) {
        FacetNode tmp = facetPath.isAbsolute() ? this.root() : this;
        for (FacetStep step : facetPath.getSegments()) {
            if (FacetPathOps.PARENT.equals(step)) {
                tmp = tmp.parent();
            } else if (FacetPathOps.SELF.equals(step)) {
                // Nothing to do
            } else {
                tmp = tmp.step(step);
            }
        }
        return tmp;
    }

    BinaryRelation getReachingRelation();

    FacetNode root();

    /** Get the set of simple constraints affecting this facet.
     * Simple constraints are expressions making use of only a single variable.
     * The set of constraints is treated as a disjunction */
    //Set<Expr> getConstraints();

    /**
     * List all
     *
     * @return
     */
//	Set<FacetConstraint> constraints();

    ConstraintFacade<? extends FacetNode> enterConstraints(); // TODO Rename to enterConstraints()

    //Concept toConcept();

    // TODO Some API to get the values of this node by excluding all constraints
    FacetedDataQuery<RDFNode> availableValues();
    FacetedDataQuery<RDFNode> remainingValues();
}

