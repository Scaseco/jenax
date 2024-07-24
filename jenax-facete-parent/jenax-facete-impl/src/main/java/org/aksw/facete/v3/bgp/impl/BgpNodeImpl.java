package org.aksw.facete.v3.bgp.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.aksw.commons.collections.maps.MapFromValueConverter;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.bgp.api.BgpDirNode;
import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.aksw.jena_sparql_api.utils.views.map.MapFromKeyConverter;
import org.aksw.jena_sparql_api.utils.views.map.MapFromResourceUnmanaged;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Var;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

import com.google.common.base.Converter;


/**
 *
 * Notes for model revision:
 * - Right now we assume that the parent is reached by traversing 'one' in reverse direction
 *   This will break once we add the conjunctive constraint features to multinode.
 *
 *
 * @author Claus Stadler, Dec 27, 2018
 *
 */
public class BgpNodeImpl
    extends ResourceImpl
    implements BgpNode
{

    public BgpNodeImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public Map<Resource, BgpMultiNode> createMap(Property p) {
        Map<RDFNode, Resource> map = new MapFromResourceUnmanaged(this, p, Vocab.property);

        Map<Resource, Resource> m = new MapFromKeyConverter<>(map, Converter.from(r -> r.as(Resource.class), RDFNode::asResource));
        Map<Resource, BgpMultiNode> result = new MapFromValueConverter<>(m, Converter.from(r -> r.as(BgpMultiNode.class), RDFNode::asResource));
        return result;
    }

    @Override
    public Map<Resource, BgpMultiNode> fwdMultiNodes() {
        Map<Resource, BgpMultiNode> result = createMap(Vocab.fwd);
        return result;
    }

    @Override
    public Map<Resource, BgpMultiNode> bwdMultiNodes() {
        Map<Resource, BgpMultiNode> result = createMap(Vocab.bwd);
        return result;
    }


    @Override
    public BgpDirNode fwd() {
        return new BgpDirNodeImpl(this, true);
    }

    @Override
    public BgpDirNode bwd() {
        return new BgpDirNodeImpl(this, false);
    }

    @Override
    public BgpNode as(String varName) {
        ResourceUtils.setLiteralProperty(this, Vocab.alias, varName);
        return this;
    }

//	@Override
//	public FacetNode as(Var var) {
//		// TODO Auto-generated method stub
//		return null;
//	}

    @Override
    public String alias() {
        String result = ResourceUtils.tryGetLiteralPropertyValue(this, Vocab.alias, String.class)
            //.map(Var::alloc)
            .orElse(null);

        return result;
    }

    @Override
    public BgpNode root() {
        //TODO Use guava's Traverser.forTree(tree)...
        BgpNode result = TreeUtils.<BgpNode>findRoot(this, n -> Optional.ofNullable(n.parent()).map(BgpMultiNode::parent).orElse(null));
        return result;
    }

    @Override
    public BgpNode as(Var var) {
        return as(var == null ? null : var.getName());
    }

    @Override
    public BgpMultiNode parent() {
        //BgpMultiNode result = ResourceUtils.getPropertyValue(this, Vocab.parent, BgpMultiNode.class);

        BgpMultiNode result = ResourceUtils.getReversePropertyValue(this, Vocab.child, BgpMultiNode.class);
        return result;
    }

    @Override
    public BgpNode chRoot() {
        turnParentToChild(this);
        return this;
    }


    /**
     * ISSUE What if a reaching predicate already exists as a child (fwd or bwd) predicate?
     *
     * {
     *   distribution: {
     *     on: {
     *     }
     * }
     *
     *
     *
     * @param start
     */
    public static void turnParentToChild(BgpNode start) {


        BgpMultiNode pmn = start.parent();
        if(pmn != null) {

            // If the parent reached this node in forward direction, turn it into a backward step
            // and vice versa
            Direction dir = pmn.getDirection();
            Property prop = pmn.reachingProperty();

            // Unset a possible (primary) one status for this node
            Set<BgpNode> parentOne = new SetFromPropertyValues<>(pmn, Vocab.one, BgpNode.class);
            Set<BgpNode> parentChildren = new SetFromPropertyValues<>(pmn, Vocab.child, BgpNode.class);
            parentOne.remove(start);
            parentChildren.remove(start);



            BgpNode pn = pmn.parent();
            turnParentToChild(pn);

            switch(dir) {
            case FORWARD: {
                Map<Resource, BgpMultiNode> fwdMap = pn.fwdMultiNodes();
                Map<Resource, BgpMultiNode> bwdMap = start.bwdMultiNodes();

                BgpMultiNode toMerge = fwdMap.get(prop);
                if(!toMerge.equals(pmn)) {
                    throw new RuntimeException("Sanity check failed");
                }
                fwdMap.remove(prop);


                // if bwdMap already has an entry for prop, merge all bgpnodes of pmn into it
                BgpMultiNode existing = bwdMap.get(prop);
                if(existing != null) {
                    existing.children().addAll(pmn.children());

                    Set<BgpNode> existingOnes = new SetFromPropertyValues<>(existing, Vocab.one, BgpNode.class);
                    if(existingOnes.isEmpty()) {
                        BgpNode priorOne = pmn.one();
                        if(priorOne != null) {
                            existingOnes.add(priorOne);
                        }
                    } else {
                        // if there is a one on existing, leave it be and clear it on pmn
                        parentOne.clear();
                    }
                    pmn.children().clear();
                    // pmn should now no longer be needed - and it should
                    // not have any attributes anymore besides the property
                    // Remove it and perform a sanity check
                    pmn.removeAll(Vocab.property);

                    Set<Statement> stmts = ResourceUtils.listReverseProperties(pmn, null).andThen(pmn.listProperties()).toSet();
                    if(!stmts.isEmpty()) {
                        throw new RuntimeException("Sanity check failed - expected empty set but got: " + stmts);
                    }

                } else {
                    // Make sure pmn is not referenced by
                    BgpNode pmnParent = pmn.parent();
                    if(pmnParent == null) {
                        bwdMap.put(prop, pmn);
                    } else {
                        // pmn is still referenced elsewhere, so we need to split
                        BgpMultiNode newPmn = start.getModel().createResource().as(BgpMultiNode.class);
                        bwdMap.put(prop, newPmn);
                    }
                }
//				if(bwdMap.containsKey(prop)) {
//					throw new RuntimeException("This case is not supported yet");
//				}


                // Register start's parent as a child to starts's multi node 'prop' in bwd direction
                BgpMultiNode mn = bwdMap.get(prop);
                mn.children().add(pn);
                break;
            }
            case BACKWARD: {
                Map<Resource, BgpMultiNode> bwdMap = pn.bwdMultiNodes();
                Map<Resource, BgpMultiNode> fwdMap = start.fwdMultiNodes();

                BgpMultiNode toMerge = bwdMap.get(prop);
                if(!toMerge.equals(pmn)) {
                    throw new RuntimeException("Sanity check failed");
                }

                BgpMultiNode existing = fwdMap.get(prop);
                if(existing != null) {
                    existing.children().addAll(pmn.children());

                    Set<BgpNode> existingOnes = new SetFromPropertyValues<>(existing, Vocab.one, BgpNode.class);
                    if(existingOnes.isEmpty()) {
                        BgpNode priorOne = pmn.one();
                        if(priorOne != null) {
                            existingOnes.add(priorOne);
                        }
                    } else {
                        // if there is a prior one on existing, leave it be and clear it on pmn
                        parentOne.clear();
                    }
                    pmn.children().clear();

                    // pmn should now no longer be needed - and it should
                    // not have any attributes anymore besides the property
                    // Remove it and perform a sanity check
                    pmn.removeAll(Vocab.property);

                    Set<Statement> stmts = ResourceUtils.listReverseProperties(pmn, null).andThen(pmn.listProperties()).toSet();
                    if(!stmts.isEmpty()) {
                        throw new RuntimeException("Sanity check failed - expected empty set but got: " + stmts);
                    }
                } else {
                    // Make sure pmn is not referenced by
                    BgpNode pmnParent = pmn.parent();
                    if(pmnParent == null) {
                        fwdMap.put(prop, pmn);
                    } else {
                        // pmn is still referenced elsewhere, so we need to split
                        BgpMultiNode newPmn = start.getModel().createResource().as(BgpMultiNode.class);
                        fwdMap.put(prop, newPmn);
                    }
                }

//				if(fwdMap.containsKey(prop)) {
//					throw new RuntimeException("This case is not supported yet");
//				}
//				bwdMap.remove(prop);
//				fwdMap.put(prop, pmn);

                BgpMultiNode mn = fwdMap.get(prop);
                mn.children().add(pn);

                break;
            }
            default:
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public String toString() {
        return "" + BgpNodeUtils.toSimplePath(this) + "; " + super.toString();
    }

//	@Override
//	public String toString() {
//		return "BgpNodeImpl " + super.toString() + "[root()=" + root() + ", parent()=" + parent() + "]";
//	}
//
//	@Override
//	public BgpNode parent(BgpMultiNode parent) {
//		ResourceUtils.getReverseProperty(s, p)
//
//		Vocab.fwd
//
//		//ResourceUtils.setProperty(this, Vocab.parent, parent);
//
//		return this;
//	}
}
