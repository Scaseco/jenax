package org.aksw.jena_sparql_api.data_query.impl;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.relationlet.VarRefStatic;

public class NodeVarRefStaticSupplier
	extends NodeCustom<Supplier<VarRefStatic>>
{
	protected NodeVarRefStaticSupplier(Supplier<VarRefStatic> value) {
		super(value);
	}
}
