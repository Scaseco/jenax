package org.aksw.facete.v3.bgp.utils;

import java.util.Objects;
import java.util.Optional;

import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.data_query.api.PathAccessor;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class PathAccessorImpl
    implements PathAccessor<BgpNode>
{
//	protected BgpNode query;
//
//	public PathAccessorImpl(BgpNode query) {
//		this.query = query;
//	}

    protected Model model;

    /**
     * A model which may contain a set of paths; only needed for tryMapToPath which tests whether an RDF term 'x' denotes a path object is the given model.
     * The test is based on the existence of a 'x' type BgpNode triple.
     *
     */
    public PathAccessorImpl() {
        this((Model)null);
    }

    /**
     * Ctor version that in the future may resolve paths only if they are connected to the given resource
     * @param rdfNode
     */
    public PathAccessorImpl(RDFNode rdfNode) {
        this.model = rdfNode.getModel();
    }

    public PathAccessorImpl(Model model) {
        this.model = model;
    }

    @Override
    public Class<BgpNode> getPathClass() {
        return BgpNode.class;
    }

    @Override
    public BgpNode getParent(BgpNode path) {
        BgpNode result = Optional.ofNullable(path.parent()).map(BgpMultiNode::parent).orElse(null);
        return result;
        //return path.parent();
    }

    @Override
    public Fragment2 getReachingRelation(BgpNode path) {
        Fragment2 result = BgpNode.getReachingRelation(path);
        return result;
        //		return path.getReachingRelation();
    }

    @Override
    public boolean isReverse(BgpNode path) {
        Fragment2 br = getReachingRelation(path);
        Element brE = br.getElement();
        Triple t = Objects.requireNonNull(ElementUtils.extractTriple(brE));


        boolean result = !br.getSourceVar().equals(t.getSubject());
        return result;
    }

    @Override
    public String getPredicate(BgpNode path) {
        Fragment2 br = getReachingRelation(path);
        Triple t = ElementUtils.extractTriple(br.getElement());

        Node node = t == null ? null : t.getPredicate();
        String result = node == null ? null : node.getURI();
        return result;
    }

    @Override
    public String getAlias(BgpNode path) {
        return path.alias();
    }

//	@Override
//	public FacetNode tryMapToPath(Expr expr) {
//		FacetNode result = null;
//
//		if(expr.isConstant()) {
//		 	NodeValue nv = ExprUtils.eval(expr);
//		 	Node node = nv.asNode();
//
//		 	if(node.isBlank()) {
//
//			 	Model model = query.modelRoot().getModel();
//
//			 	//ModelUtils.convertGraphNodeToRDFNode(node, model);
//			 	Resource state = model.wrapAsResource(node);
//
//			 	boolean isFacetNode = state.hasProperty(Vocab.parent) || state.getModel().contains(null, Vocab.root, state);
//			 	result = isFacetNode ? new FacetNodeImpl(query, state) : null;
//		 	}
//		}
//		return result;
//	}

    @Override
    public BgpNode tryMapToPath(Node node) {
        Objects.requireNonNull(model, "Testing whether a node denotes a path object requires a model to test against, but none was set.");

        //FacetNode result = null;
        BgpNode result = null;

         if(node.isBlank()) {

             //Model model = query.modelRoot().getModel();
             //Model model = query.getModel();

             //ModelUtils.convertGraphNodeToRDFNode(node, model);
             BgpNode state = model.wrapAsResource(node).as(BgpNode.class);

             boolean isBgpNode = state.hasProperty(RDF.type, Vocab.BgpNode);

             //boolean isFacetNode = state.hasProperty(Vocab.parent) || state.getModel().contains(null, Vocab.root, state);
             //result = isFacetNode ? new FacetNodeImpl(query, state) : null;
             result = isBgpNode ? state : null;
         }

         return result;
    }
}
