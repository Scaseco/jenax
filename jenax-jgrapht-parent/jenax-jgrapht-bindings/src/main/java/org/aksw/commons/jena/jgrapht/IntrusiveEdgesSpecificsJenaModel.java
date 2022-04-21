package org.aksw.commons.jena.jgrapht;

import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.jgrapht.graph.IntrusiveEdgesSpecifics;

public class IntrusiveEdgesSpecificsJenaModel
	implements IntrusiveEdgesSpecifics<RDFNode, Statement>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7382770638639511162L;

	public IntrusiveEdgesSpecificsJenaModel(Model model, Property confinementProperty) {
		super();
		this.model = model;
		this.confinementProperty = confinementProperty;
	}

	protected Model model;
	protected Property confinementProperty;
	
	@Override
	public RDFNode getEdgeSource(Statement e) {
		return e.getSubject();
	}

	@Override
	public RDFNode getEdgeTarget(Statement e) {
		return e.getObject();
	}

	@Override
	public boolean add(Statement e, RDFNode sourceVertex, RDFNode targetVertex) {
		Statement stmt = model.createStatement(sourceVertex.asResource(), confinementProperty, targetVertex);
		boolean tmp = model.contains(stmt);
		if(!tmp) {
			model.add(stmt);
		}
		boolean result = !tmp;
		return result;
	}

	@Override
	public boolean containsEdge(Statement e) {
		boolean result = model.contains(e);
		return result;
	}

	@Override
	public Set<Statement> getEdgeSet() {
		return new SetOfStatementsFromModel(model, confinementProperty);
	}

	@Override
	public void remove(Statement e) {
		model.remove(e);
	}

	@Override
	public double getEdgeWeight(Statement e) {
		return 1.0;
	}

	@Override
	public void setEdgeWeight(Statement e, double weight) {
		if(weight != 1.0) {
			throw new UnsupportedOperationException();
		}
	}

}
