package org.aksw.facete.v3.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class DataNodeImpl {
	
	Var var;

	//protected BasicPattern projection;
	//protected GraphVar 
	
	//protected QueryPattern;
	
//	public void add(Property p) {
//		pattern.add(this.var, p, getOrCreateVar(p.asNode()));
//	}
	

	Var getOrCreateVar(Node p) {
		Var result = Var.alloc("someFreshVar");
		return result;
	}
	
//	add(Property p, BinaryRelation p);
	
}
