package org.aksw.facete.v3.api;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetPathOps;
import org.aksw.jenax.path.core.FacetStep;

import com.google.common.collect.MapMaker;

public class TreeQueryNodeImpl

    implements TreeQueryNode
{
    protected TreeQueryImpl treeModel;
    protected TreeQueryNodeImpl parent;
    // protected Map<FacetStep, TreeQueryNode> stepToChild = new LinkedHashMap<>();
    protected Map<FacetStep, TreeQueryNode> stepToChild = new MapMaker().weakValues().makeMap();

    // The parent must only weakly reference the children.

    public TreeQueryNodeImpl(TreeQueryImpl treeModel, TreeQueryNodeImpl parent) {
        super();
        this.treeModel = treeModel;
        this.parent = parent;
    }

    @Override
    public TreeQueryNode getOrCreateChild(FacetStep step) {
        TreeQueryNode result = stepToChild.computeIfAbsent(step, s -> {
            return new TreeQueryNodeImpl(this.treeModel, this);
        });
        return result;
    }

    /** Rotate the tree such that this node becomes the root */
    @Override
    public void chRoot() {
        // TreeQueryNode originalParent
        if (parent != null) {
            parent.chRoot();
        }

        // This node becomes the parent of its current parent
        if (parent != null) {
            FacetStep reachingStep = reachingStep();

            // Invert the direction
            String alias = reachingStep.getAlias();
            String adjustedAlias;
            if (alias == null) {
                adjustedAlias = "parent";
            } else if (alias.startsWith("parent.")) {
                adjustedAlias = alias.substring("parent.".length());
            } else if (alias.equals("parent")) {
                adjustedAlias = null;
            } else {
                adjustedAlias = "parent." + alias;
            }

            FacetStep invertedStep = new FacetStep(reachingStep.getNode(), !reachingStep.isForward(), adjustedAlias, reachingStep.getTargetComponent());
            parent.parent = this;

            // Remove 'this' from the children
            parent.stepToChild.remove(reachingStep);

            // Add the parent as a child
            stepToChild.put(invertedStep, parent);

            parent = null;
            treeModel.root = this;
        }
    }

    @Override
    public TreeQueryImpl getTree() {
        return treeModel;
    }

    @Override
    public TreeQueryNode getParent() {
        return parent;
    }

    public FacetStep getStepToChild(TreeQueryNode child) {
        FacetStep result = stepToChild.entrySet().stream().filter(e -> e.getValue() == child).map(Entry::getKey).findFirst().orElse(null);
        return result;
        // this.fwd().via(null);
    }

    @Override
    public FacetStep reachingStep() {
        if (parent == null) {
            throw new RuntimeException("Method must not be called on a root node");
        }

        FacetStep result = parent.getStepToChild(this);
        return result;
    }

    @Override
    public FacetPath getFacetPath() {
        FacetPath result = getParent() == null ? FacetPathOps.newRelativePath() : parent.getFacetPath().resolve(reachingStep());
        return result;
    }

    @Override
    public String toString() {
        return stepToChild.toString();
    }

    @Override
    public TreeQueryNode resolve(FacetPath facetPath) {
        TreeQueryNode tmp = facetPath.isAbsolute() ? treeModel.root() : this;
        for (FacetStep step : facetPath.getSegments()) {
            if (FacetPathOps.PARENT.equals(step)) {
                tmp = tmp.getParent();
            } else if (FacetPathOps.SELF.equals(step)) {
                // Nothing to do
            } else {
                tmp = tmp.getOrCreateChild(step);
            }
        }
        return tmp;
    }
}
