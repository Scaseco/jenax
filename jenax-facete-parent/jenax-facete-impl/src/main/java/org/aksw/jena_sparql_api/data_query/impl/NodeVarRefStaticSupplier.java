package org.aksw.jena_sparql_api.data_query.impl;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.relationlet.VarRefStatic;
import org.aksw.jenax.arq.util.node.NodeCustom;

public class NodeVarRefStaticSupplier
	extends NodeCustom<Supplier<VarRefStatic>>
{
	protected NodeVarRefStaticSupplier(Supplier<VarRefStatic> value) {
		super(value);
	}
}
