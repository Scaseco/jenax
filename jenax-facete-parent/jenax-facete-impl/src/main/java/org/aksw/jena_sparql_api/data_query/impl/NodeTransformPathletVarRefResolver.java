package org.aksw.jena_sparql_api.data_query.impl;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.relationlet.RelationletSimple;
import org.aksw.jena_sparql_api.relationlet.VarRefStatic;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

public class NodeTransformPathletVarRefResolver
	implements NodeTransform
{
	protected RelationletSimple relationlet;
	
	public NodeTransformPathletVarRefResolver(RelationletSimple relationlet) {
		this.relationlet = relationlet;
	}

	// Substitute the node with reference
	@Override
	public Node apply(Node t) {
		Node result = t;
		if(t instanceof NodeVarRefStaticSupplier) {
			Supplier<VarRefStatic> varRefSupplier = ((NodeVarRefStaticSupplier)t).getValue();
			VarRefStatic varRef = varRefSupplier.get();

			result = relationlet.resolve(varRef);
		}
		
		return result;
	}
}
