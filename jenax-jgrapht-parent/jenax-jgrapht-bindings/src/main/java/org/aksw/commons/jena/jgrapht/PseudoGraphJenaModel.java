package org.aksw.commons.jena.jgrapht;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StmtIteratorImpl;
// import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.IntrusiveEdgesSpecifics;


/**
 * Wrapper for exposing a Jena model as a JGraphT directed pseudo model.
 *
 *
 *
 * @author raven
 *
 */

public class PseudoGraphJenaModel
    implements Graph<RDFNode, Statement>
{
    protected Model model;
    protected GraphType graphType;
    
    protected Property confinementProperty; // May be null

    //protected transient EdgeFactory<RDFNode, Statement> edgeFactory;

    protected EdgeFactoryJenaModel edgeSupplier;
    protected IntrusiveEdgesSpecifics<RDFNode, Statement> intrusiveEdgesSpecifics;

    public PseudoGraphJenaModel(Model model) {
    	this(model, DefaultGraphType.directedPseudograph());
    }

    public PseudoGraphJenaModel(Model model, Property confinementProperty) {
    	this(model, DefaultGraphType.directedPseudograph(), confinementProperty);
    }

    public PseudoGraphJenaModel(Model model, GraphType graphType) {
    	this(model, graphType, null);
    }

    public PseudoGraphJenaModel(Model model, GraphType graphType, Property confinementProperty) {
    	this(model, graphType, confinementProperty, confinementProperty);
    }
    
    public PseudoGraphJenaModel(Model model, GraphType graphType, Property confinementProperty, Property insertProperty) {
        super();
        this.model = model;
        this.graphType = graphType;
        this.confinementProperty = confinementProperty;
        
        edgeSupplier = new EdgeFactoryJenaModel(model, insertProperty);
        intrusiveEdgesSpecifics = new IntrusiveEdgesSpecificsJenaModel(model, confinementProperty);

        //edgeFactory = new EdgeFactoryJenaModel(model, insertProperty);
    }


    @Override
    public Set<Statement> getAllEdges(RDFNode sourceVertex, RDFNode targetVertex) {
        Set<Statement> result = listStatements(sourceVertex, confinementProperty, targetVertex).toSet();

        return result;
    }

    @Override
    public Statement getEdge(RDFNode sourceVertex, RDFNode targetVertex) {
        Set<Statement> edges = getAllEdges(sourceVertex, targetVertex);
        // TODO Maybe throw an exception or return null if there are multiple edges
        Statement result = edges.iterator().next();
        return result;
    }

//    @Override
//    public EdgeFactory<RDFNode, Statement> getEdgeFactory() {
//        return edgeSupplier;
//    }

    @Override
    public Supplier<Statement> getEdgeSupplier() {
        return edgeSupplier;
    }

    @Override
    public Statement addEdge(RDFNode sourceVertex, RDFNode targetVertex) {
    	Statement result = edgeSupplier.createEdge(sourceVertex, targetVertex);        	
        model.add(result);

        return result;
    }

    @Override
    public boolean addEdge(RDFNode sourceVertex, RDFNode targetVertex, Statement e) {
        boolean isValid = e.getSubject().equals(sourceVertex) && e.getObject().equals(targetVertex);
        if(!isValid) {
            throw new RuntimeException("Source and/or target vertex does not match those of the triple: " + sourceVertex + " " + targetVertex + " " + e);
        }

        if(confinementProperty != null && !e.getPredicate().equals(confinementProperty)) {
            throw new RuntimeException("Graph is confined to predicate " + confinementProperty + " therefore cannot add edge with predicate " + e);
        }

        boolean result = !model.contains(e);
        if(result) {
            model.add(e);
        }
    	
        return result;
    }

    @Override
    public boolean addVertex(RDFNode v) {
    	// Approximation of the semantics - as long as there is no Statement with the given RDFNode v,
    	// addVertex will return true for that v.
    	boolean result = !containsVertex(v);
    	return result;
    }

    @Override
    public boolean containsEdge(RDFNode sourceVertex, RDFNode targetVertex) {
        // TODO Not sure if contains works with RDFNode.ANY - may have to use !.find().toSet().isEmpyt()
        boolean result = sourceVertex != null &&
        		sourceVertex.isResource() &&
        		model.contains(sourceVertex.asResource(), confinementProperty, targetVertex);

        return result;
    }

    @Override
    public boolean containsEdge(Statement e) {
        boolean result = model.contains(e);
        return result;
    }

    @Override
    public boolean containsVertex(RDFNode v) {
        boolean result =
                (v != null && v.isResource() &&
                model.contains(v.asResource(), null, (RDFNode)null)) ||
                model.contains(null, null, v);

        return result;
    }

    @Override
    public Set<Statement> edgeSet() {
        Set<Statement> result = listStatements(null, confinementProperty, null).toSet();
        return result;
    }

    @Override
    public int degreeOf(RDFNode vertex) {
        return inDegreeOf(vertex) + outDegreeOf(vertex);
    }

    @Override
    public Set<Statement> edgesOf(RDFNode vertex) {
        Set<Statement> result =
        		listStatements(vertex, confinementProperty, null).andThen(
        		listStatements(null, confinementProperty, vertex)).toSet();

        return result;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Statement> edges) {
        boolean result = edges.stream().map(this::removeEdge).reduce(false, (r, e) -> r || e);
    	
        return result;
    }

    @Override
    public Set<Statement> removeAllEdges(RDFNode sourceVertex, RDFNode targetVertex) {
    	Set<Statement> result = listStatements(sourceVertex, confinementProperty, targetVertex).toSet();
    	result.forEach(model::remove);
    	
    	return result;
    }

    @Override
    public boolean removeAllVertices(Collection<? extends RDFNode> vertices) {
    	boolean result = vertices.stream().map(this::removeVertex).reduce(false, (r, v) -> r || v);
    	return result;
    }

    @Override
    public Statement removeEdge(RDFNode sourceVertex, RDFNode targetVertex) {
    	Statement result = getEdge(sourceVertex, targetVertex);
    	
    	if(result != null) {
    		model.remove(result);
    	}

    	return result;
    }

    @Override
    public boolean removeEdge(Statement e) {
    	boolean result = model.contains(e);
    	
    	if(result) {
    		model.remove(e);
    	}
    	
        return result;
    }


    @Override
    public boolean removeVertex(RDFNode v) {
    	// Null handling is delegated to model
    	boolean result = model.containsResource(v);
        
    	if(v != null && v.isResource()) {
            model.remove(v.asResource(), confinementProperty, (RDFNode)null);
        }

        model.remove(null, confinementProperty, v);
        return result;
    }


    @Override
    public Set<RDFNode> vertexSet() {
        Set<RDFNode> result = new LinkedHashSet<>();
        model.listStatements(null, confinementProperty, (RDFNode)null).forEachRemaining(stmt -> {
                result.add(stmt.getSubject());
                result.add(stmt.getObject());
        });
        return result;
    }

    @Override
    public RDFNode getEdgeSource(Statement e) {
        return e.getSubject();
    }

    @Override
    public RDFNode getEdgeTarget(Statement e) {
        return e.getObject();
    }

    @Override
    public GraphType getType() {
        return graphType; //DefaultGraphType.directedPseudograph();
    }

    @Override
    public double getEdgeWeight(Statement e) {
        return 1;
    }

    @Override
    public int inDegreeOf(RDFNode vertex) {
        int result = incomingEdgesOf(vertex).size();
        return result;
    }

    @Override
    public Set<Statement> incomingEdgesOf(RDFNode vertex) {
        Set<Statement> result = listStatements(null, confinementProperty, vertex).toSet();
        return result;
    }

    @Override
    public int outDegreeOf(RDFNode vertex) {
        int result = outgoingEdgesOf(vertex).size();
        return result;
    }

    @Override
    public Set<Statement> outgoingEdgesOf(RDFNode vertex) {
        Set<Statement> result = listStatements(vertex, confinementProperty, null).toSet();
        return result;
    }

    @Override
    public void setEdgeWeight(Statement statement, double weight) {
        throw new UnsupportedOperationException("RDF graph is not weighted");
    }

    public StmtIterator listStatements(RDFNode sourceVertex, Property property, RDFNode targetVertex) {    	
    	StmtIterator result = null;

    	// If the sourceVertex is a Resource or null - as required by the Model API, use Model::listStatements
    	// - otherwise return an empty iterator;
    	if(sourceVertex == null || sourceVertex.isResource()) {
        	Resource s = sourceVertex == null ? null : sourceVertex.asResource();
    		result = model.listStatements(s, property, targetVertex);
    	} else {
    		result = new StmtIteratorImpl(Collections.<Statement>emptySet().iterator());
    	}
     	
        return result;
	
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((confinementProperty == null) ? 0 : confinementProperty.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PseudoGraphJenaModel other = (PseudoGraphJenaModel) obj;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (confinementProperty == null) {
            if (other.confinementProperty != null)
                return false;
        } else if (!confinementProperty.equals(other.confinementProperty))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "PseudoGraphJena [model=" + model + ", predicate=" + confinementProperty + "]";
    }

	@Override
	public Supplier<RDFNode> getVertexSupplier() {
		return model::createResource;
	}


	@Override
	public RDFNode addVertex() {
		RDFNode result = Optional.ofNullable(getVertexSupplier())
				.orElseThrow(UnsupportedOperationException::new)
				.get();
			
		addVertex(result);
		return result;
	}
}
