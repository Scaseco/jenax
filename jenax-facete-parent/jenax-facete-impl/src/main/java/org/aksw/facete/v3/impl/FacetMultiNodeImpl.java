package org.aksw.facete.v3.impl;

import java.util.Map;

import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetNodeResource;
import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.apache.jena.rdf.model.Resource;


public class FacetMultiNodeImpl
    implements FacetMultiNode
{
    protected FacetNodeResource parent;
//	protected Property property;
//	protected boolean isFwd;

    protected BgpMultiNode state;

    public FacetMultiNodeImpl(FacetNodeResource parent, BgpMultiNode state) {//Property property, boolean isFwd) {
        super();
        this.parent = parent;
        this.state = state;
//		this.property = property;
//		this.isFwd = isFwd;
    }

//	public Set<Resource> liveBackingSet() {
//		Set<Resource> result = isFwd
//				? new SetFromPropertyValues<>(parent.state(), property, Resource.class)
//				: new SetFromResourceAndInverseProperty<>(parent.state(), property, Resource.class);
//
//		return result;
//	}

//	@Override
//	public Set<FacetNode> children() {
//		return new CollectionFromConverter(
//			new SetFromPropertyValues<>(parent.state(), property, Resource.class),
//			Converter.from(FacetNodeImpl::new, FacetNode::state);
//		);
//	}


    @Override
    public boolean hasMultipleReferencedAliases() {
        return false;
    }

    /**
     * Gets or creates a single successor
     *
     */
    @Override
    public FacetNode one() {
        //state.one();

        return new FacetNodeImpl(parent.query(), state.one());

//		// TODO We could use .children as well
//		Set<Resource> set = liveBackingSet();
//
//		FacetNode result;
//		Resource r;
//		if(set.isEmpty()) {
//			r = parent.state().getModel().createResource();
//			set.add(r);
//
//			r.addProperty(Vocab.parent, parent.state());
//		}
//
//		if(set.size() == 1) {
//			result = new FacetNodeImpl(parent.query(), set.iterator().next());
//		} else {
//			throw new RuntimeException("Multiple aliases defined");
//		}
//
//		return result;
    }

    @Override
    public void remainingValues() {
        // TODO Auto-generated method stub

    }
    @Override
    public void availableValues() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean contains(FacetNode facetNode) {
        boolean result;
        if(!(facetNode instanceof FacetNodeResource)) {
            result = false;
        } else {
            FacetNodeResource tmp = (FacetNodeResource)facetNode;
            Resource r = tmp.state();

            result = state.contains(r.as(BgpNode.class));

//			Set<Resource> set = liveBackingSet();
//			result = set.contains(r);
        }
        return result;
    }

    @Override
    public FacetNode viaAlias(String alias) {
        if (alias == null) {
            return one();
        } else {
            throw new RuntimeException("not implemented yet");
        }
    }

    @Override
    public Map<String, FacetNode> list() {
        throw new RuntimeException("not implemented yet");
    }

//    @Override
//    public FacetNode viaAlias(String alias, Integer component) {
//        throw new RuntimeException("Not implemented yet");
//    }
}
