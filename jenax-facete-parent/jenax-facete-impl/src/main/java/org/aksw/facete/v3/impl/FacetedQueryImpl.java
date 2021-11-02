package org.aksw.facete.v3.impl;

import java.util.Collection;
import java.util.function.Supplier;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetNodeResource;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.FacetedQueryResource;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;


public class FacetedQueryImpl
    implements FacetedQueryResource
{
    // The actual state is stored in a model rooted in a certain resource
    protected SparqlQueryConnection conn;
    protected Supplier<? extends UnaryRelation> conceptSupplier;

//	protected Function<? super Resource, ? extends UnaryRelation> conceptParser;

    protected XFacetedQuery modelRoot;


//	protected transient FacetNodeResource root;
//	protected transient FacetNodeResource focus;

    public static FacetedQueryImpl create(SparqlQueryConnection conn) {
        return create(ModelFactory.createDefaultModel(), conn);
    }

    public static FacetedQueryImpl create(Model model, SparqlQueryConnection conn) {
        return create(model.createResource().as(XFacetedQuery.class), conn);
    }

    public static FacetedQueryImpl create(Resource modelRoot, SparqlQueryConnection conn) {
        return create(modelRoot.as(XFacetedQuery.class), conn);
    }


    public static void initResource(Resource resource) {
        initResource(resource.as(XFacetedQuery.class));
    }

    public static void initResource(XFacetedQuery modelRoot) {
        if(modelRoot.getBgpRoot() == null) {
            modelRoot.setBgpRoot(modelRoot.getModel().createResource()
                    .addProperty(RDF.type, Vocab.BgpNode)
                    .as(BgpNode.class)
            );
        }

        if(modelRoot.getFocus() == null) {
            modelRoot.setFocus(modelRoot.getBgpRoot());
        }
    }

    public static FacetedQueryImpl create(XFacetedQuery modelRoot, SparqlQueryConnection conn) {
        initResource(modelRoot);

        return new FacetedQueryImpl(modelRoot, () -> ConceptUtils.subjectConcept, conn);
    }

    public FacetedQueryImpl(XFacetedQuery modelRoot, Supplier<? extends UnaryRelation> conceptSupplier, SparqlQueryConnection conn) {
        this.modelRoot = modelRoot;
        this.conceptSupplier = conceptSupplier;
        this.conn = conn;
    }

//	public FacetedQueryImpl() {
//
//		this.root = new FacetNodeImpl(this, modelRoot.getBgpRoot());
//		this.focus = this.root;
//	}

    @Override
    public XFacetedQuery modelRoot() {
        return modelRoot;
    }

    @Override
    public FacetNodeResource root() {
        return new FacetNodeImpl(this, modelRoot.getBgpRoot());
        //return root;
    }

    @Override
    public FacetNodeResource focus() {
        return new FacetNodeImpl(this, modelRoot.getFocus());

        //return focus;
    }

    @Override
    public void focus(FacetNode facetNode) {
        // Ensure this is the right impl
        FacetNodeImpl impl = (FacetNodeImpl)facetNode;
        modelRoot.setFocus(impl.state());
        //this.focus = impl;
    }

    @Override
    public Concept toConcept() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public FacetedQuery baseConcept(Supplier<? extends UnaryRelation> conceptSupplier) {
        this.conceptSupplier = conceptSupplier;
        return this;
    }

    @Override
    public FacetedQuery baseConcept(UnaryRelation concept) {
        return baseConcept(() -> concept);
    }

    @Override
    public UnaryRelation baseConcept() {
        UnaryRelation result = conceptSupplier.get();
        return result;
    }

    @Override
    public FacetedQuery connection(SparqlQueryConnection conn) {
        this.conn = conn;
        return this;
    }

    @Override
    public SparqlQueryConnection connection() {
        return conn;
    }

    @Override
    public Collection<FacetConstraint> constraints() {
        return modelRoot.constraints();
//		Collection<FacetConstraint> result = new SetFromPropertyValues<>(modelRoot, Vocab.constraint, FacetConstraint.class);
//		return result;
    }


//	@Override
//	public FacetNode find(Object id) {
//		FacetNode result = id instanceof FacetNode
//				? (FacetNode)id
//				: id instanceof Node ? : null;
//
//		if(id instanceof FacetNode) {
//		if(id instanceof Node) {
//			node =
//		}
//	}

}
