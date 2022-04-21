package org.aksw.commons.jena.jgrapht;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
//import org.jgrapht.EdgeFactory;
import org.jgrapht.GraphType;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.IntrusiveEdgesSpecifics;


/**
 * Wrapper for exposing a Jena graph as a JGraphT directed pseudo graph.
 *
 * Note: All graph lookups are done via a .find() method that does additional filtering for supporting
 * variables as vertices.
 *
 *
 * @author raven
 *
 */
public class PseudoGraphJenaGraph
    implements org.jgrapht.Graph<Node, Triple>
{
    protected org.apache.jena.graph.Graph graph;
    
    // The graph type describing the features of the underlying RDF graph
    // By default DefaultGraphType.directedPseudograph()
    protected GraphType graphType;
    
    
   /**
     * Predicate to which to confine the underlying Jena graph. May be Node.ANY
     * to use all triples regardless to their predicate.
     *
     */
    protected Node confinementPredicate; // May be Node.ANY - should not be null

    //protected EdgeFactory<Node, Triple> edgeFactory;
    protected EdgeFactoryJenaGraph edgeSupplier;
    protected IntrusiveEdgesSpecifics<Node, Triple> intrusiveEdgesSpecifics;
    

    public PseudoGraphJenaGraph(Graph graph) {
        this(graph, DefaultGraphType.directedPseudograph());
    }
    
    public PseudoGraphJenaGraph(Graph graph, GraphType graphType) {
        this(graph, graphType, Node.ANY, null);
    }

    public PseudoGraphJenaGraph(Graph graph, Node confinementPredicate) {
    	this(graph, DefaultGraphType.directedPseudograph(), confinementPredicate);
    }

    public PseudoGraphJenaGraph(Graph graph, GraphType graphType, Node confinementPredicate) {
    	this(graph, graphType, confinementPredicate, confinementPredicate);
    }


    /**
     * Setting insert predicate to null prevents inserts
     * 
     * @param graph
     * @param graphType
     * @param confinementPredicate
     * @param insertPredicate
     */
    public PseudoGraphJenaGraph(Graph graph, GraphType graphType, Node confinementPredicate, Node insertPredicate) {
        super();
        this.graph = graph;
        this.graphType = graphType;
        this.confinementPredicate = confinementPredicate;

        edgeSupplier = new EdgeFactoryJenaGraph(insertPredicate);
        intrusiveEdgesSpecifics = new IntrusiveEdgesSpecificsJenaGraph(graph, confinementPredicate);
        //edgeFactory = new EdgeFactoryJenaGraph(insertPredicate);
    }


    @Override
    public Set<Triple> getAllEdges(Node sourceVertex, Node targetVertex) {
        return find(sourceVertex, confinementPredicate, targetVertex).toSet();
    }

    @Override
    public Triple getEdge(Node sourceVertex, Node targetVertex) {
        Set<Triple> edges = getAllEdges(sourceVertex, targetVertex);
        // TODO Maybe throw an exception or return null if there are multiple edges

        Triple result = edges.isEmpty() ? null : edges.iterator().next();
        if(result == null) {
            throw new RuntimeException("null edge should not happen");
        }

        return result;
    }

//    @Override
//    public EdgeFactory<Node, Triple> getEdgeFactory() {
//        return edgeSupplier;
//    }

    @Override
    public Supplier<Triple> getEdgeSupplier() {
    	return edgeSupplier;
    }

    @Override
    public Triple addEdge(Node sourceVertex, Node targetVertex) {
        //Triple result = edgeFactory.createEdge(sourceVertex, targetVertex);
    	//Triple e = edgeSupplier.get();
    	Triple result = edgeSupplier.createEdge(sourceVertex, targetVertex);//intrusiveEdgesSpecifics.add(e, sourceVertex, targetVertex)
        graph.add(result);

        return result;
    }

    @Override
    public boolean addEdge(Node sourceVertex, Node targetVertex, Triple e) {
        boolean isValid = e.getSubject().equals(sourceVertex) && e.getObject().equals(targetVertex);
        if(!isValid) {
            throw new RuntimeException("Source and/or target vertex does not match those of the triple: " + sourceVertex + " " + targetVertex + " " + e);
        }

        if(!confinementPredicate.equals(Node.ANY) && e.getPredicate().equals(confinementPredicate)) {
            throw new RuntimeException("Graph is confined to predicate " + confinementPredicate + " therefore cannot add edge with predicate " + e);
        }

        boolean result = !graph.contains(e);
        if(result) {
            graph.add(e);
        }
        return result;
    }

    @Override
    public boolean addVertex(Node v) {
    	// Approximation of the semantics - as long as there is no Triple with the given Node v,
    	// addVertex will return true for that v.
    	boolean result = !containsVertex(v);
    	return result;
    }

    @Override
    public boolean containsEdge(Node sourceVertex, Node targetVertex) {
        boolean result = find(sourceVertex, confinementPredicate, targetVertex).hasNext();
        return result;
    }

    @Override
    public boolean containsEdge(Triple e) {
        boolean result = find(e.getSubject(), e.getPredicate(), e.getObject()).hasNext();
        return result;
    }

    @Override
    public boolean containsVertex(Node v) {
        boolean result =
                find(v, confinementPredicate, Node.ANY).hasNext() ||
                find(Node.ANY, confinementPredicate, v).hasNext();
        return result;
    }

    @Override
    public Set<Triple> edgeSet() {
        Set<Triple> result = find(Node.ANY, confinementPredicate, Node.ANY).toSet();
        return result;
    }

    @Override
    public int degreeOf(Node vertex) {
        return inDegreeOf(vertex) + outDegreeOf(vertex);
    }

    @Override
    public Set<Triple> edgesOf(Node vertex) {
        Set<Triple> result = new HashSet<>();
        find(vertex, confinementPredicate, Node.ANY).forEachRemaining(result::add);
        find(Node.ANY, confinementPredicate, vertex).forEachRemaining(result::add);

        return result;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Triple> edges) {
        Iterator<Triple> it = edges.stream().map(e -> (Triple)e).iterator();
        GraphUtil.delete(graph, it);
        return true;
    }

    @Override
    public Set<Triple> removeAllEdges(Node sourceVertex, Node targetVertex) {
        graph.remove(sourceVertex, confinementPredicate, targetVertex);
        return null;
    }

    @Override
    public boolean removeAllVertices(Collection<? extends Node> vertices) {
        boolean result = false;
        for(Node v : vertices) {
            result = result || removeVertex(v);
        }

        return result;
    }

    @Override
    public Triple removeEdge(Node sourceVertex, Node targetVertex) {
        Triple result = new Triple(sourceVertex, confinementPredicate, targetVertex);
        removeEdge(result);
//    	graph.remove(result.get, p, o);
//        return true;
        return null;
    }

    @Override
    public boolean removeEdge(Triple e) {
        if(!e.getPredicate().equals(confinementPredicate) && !confinementPredicate.equals(Node.ANY)) {
            throw new RuntimeException("Cannot remove edge outside of confinement - predicate must be: " + confinementPredicate + " but got " + e);
        }

        graph.remove(e.getSubject(), e.getPredicate(), e.getObject());
        return true;
    }


    @Override
    public boolean removeVertex(Node v) {
        graph.remove(v, confinementPredicate, Node.ANY);
        graph.remove(Node.ANY, confinementPredicate, v);
        // FIXME Return proper result
        return true;
    }


    @Override
    public Set<Node> vertexSet() {
        Set<Node> result = new HashSet<>();
        find(Node.ANY, confinementPredicate, Node.ANY).forEachRemaining(triple -> {
            result.add(triple.getSubject());
            result.add(triple.getObject());
        });
        return result;
    }

    @Override
    public Node getEdgeSource(Triple e) {
        return e.getSubject();
    }

    @Override
    public Node getEdgeTarget(Triple e) {
        return e.getObject();
    }

    @Override
    public GraphType getType() {
        return graphType;
    }

    /**
     * FIXME: We could delegate requests to edge weights to a lambda which e.g. gets this value from the RDF
     */
    @Override
    public double getEdgeWeight(Triple e) {
        return 1;
    }

    @Override
    public void setEdgeWeight(Triple triple, double weight) {
        throw new UnsupportedOperationException("RDF graph is not weighted");
    }

    @Override
    public int inDegreeOf(Node vertex) {
        int result = incomingEdgesOf(vertex).size();
        return result;
    }

    @Override
    public Set<Triple> incomingEdgesOf(Node vertex) {
        Set<Triple> result = find(Node.ANY, confinementPredicate, vertex).toSet();
        return result;
    }

    @Override
    public int outDegreeOf(Node vertex) {
        int result = outgoingEdgesOf(vertex).size();
        return result;
    }

    @Override
    public Set<Triple> outgoingEdgesOf(Node vertex) {
        Set<Triple> result = find(vertex, confinementPredicate, Node.ANY).toSet();
        return result;
    }
    
  
    /**
     * A delegate to graph.find - single point for adding any custom find semantics should it become necessary
     *
     * @param graph
     * @param s
     * @param p
     * @param o
     * @return
     */
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
    	ExtendedIterator<Triple> result = graph.find(s, p, o);
    	return result;
    }

    @Override
    public String toString() {
        return "PseudoGraphJenaGraph [graph=" + graph + ", confinementPredicate=" + confinementPredicate
                + ", edgeFactory=" + edgeSupplier + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((confinementPredicate == null) ? 0 : confinementPredicate.hashCode());
        result = prime * result + ((edgeSupplier == null) ? 0 : edgeSupplier.hashCode());
        result = prime * result + ((graph == null) ? 0 : graph.hashCode());
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
        PseudoGraphJenaGraph other = (PseudoGraphJenaGraph) obj;
        if (confinementPredicate == null) {
            if (other.confinementPredicate != null)
                return false;
        } else if (!confinementPredicate.equals(other.confinementPredicate))
            return false;
        if (edgeSupplier == null) {
            if (other.edgeSupplier != null)
                return false;
        } else if (!edgeSupplier.equals(other.edgeSupplier))
            return false;
        if (graph == null) {
            if (other.graph != null)
                return false;
        } else if (!graph.equals(other.graph))
            return false;
        return true;
    }

	@Override
	public Supplier<Node> getVertexSupplier() {
		// Note: We could add a sanity check wrapper in the unlikely case createBlankNode yields a node that is already in the graph
		return NodeFactory::createBlankNode;
	}


	@Override
	public Node addVertex() {
		Node result = Optional.ofNullable(getVertexSupplier())
			.orElseThrow(UnsupportedOperationException::new)
			.get();
		
		addVertex(result);
		return result;
	}

}
