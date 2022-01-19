package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.PolymorphicOnly;

public interface OpN
	extends Op
{
	@IriNs("rpif")
	@PolymorphicOnly
	List<Op> getSubOps();
	
	OpN setSubOps(List<Op> subOps);
	
	@Override
	default List<Op> getChildren() {
		List<Op> result = getSubOps();
		return result;
	}
}
