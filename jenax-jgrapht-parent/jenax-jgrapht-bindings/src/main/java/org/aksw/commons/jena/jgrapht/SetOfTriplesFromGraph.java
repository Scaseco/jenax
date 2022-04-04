package org.aksw.commons.jena.jgrapht;

import java.util.AbstractSet;
import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.github.jsonldjava.shaded.com.google.common.collect.Iterators;

public class SetOfTriplesFromGraph
	extends AbstractSet<Triple>
{
	protected Graph graph;
	protected Node confinementPredicate;

	public SetOfTriplesFromGraph(Graph graph, Node confinementProperty) {
		super();
		this.graph = graph;
		this.confinementPredicate = confinementProperty;
	}

	@Override
	public boolean add(Triple e) {
		boolean tmp = graph.contains(e);
		if(!tmp) {
			graph.add(e);
		}
		
		boolean result = !tmp;
		return result;
	}

	@Override
	public boolean remove(Object o) {
		boolean result = false;
		if(o instanceof Triple) {
			Triple triple = (Triple)o;
			result = graph.contains(triple);
			if(result) {
				graph.remove(triple.getSubject(), triple.getPredicate(), triple.getObject());
			}
		}
		return result;
	}
	
	@Override
	public boolean contains(Object o) {
		boolean result = o instanceof Triple ? graph.contains((Triple)o) : false;
		return result;
	}
	
	@Override
	public Iterator<Triple> iterator() {
		return graph.find(null, confinementPredicate, null);
	}

	@Override
	public int size() {
		int result = Iterators.size(graph.find(null, confinementPredicate, null));
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((confinementPredicate == null) ? 0 : confinementPredicate.hashCode());
		result = prime * result + ((graph == null) ? 0 : graph.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SetOfTriplesFromGraph other = (SetOfTriplesFromGraph) obj;
		if (confinementPredicate == null) {
			if (other.confinementPredicate != null)
				return false;
		} else if (!confinementPredicate.equals(other.confinementPredicate))
			return false;
		if (graph == null) {
			if (other.graph != null)
				return false;
		} else if (!graph.equals(other.graph))
			return false;
		return true;
	}
}