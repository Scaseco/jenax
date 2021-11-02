package org.aksw.facete.v3.api;

import java.util.List;

public interface PathBase<T extends PathBase<T, S>, S>
{
	/**
	 * Steps can be seen as transitions/edges between nodes of a path.
	 * So steps may be based on an underlying graph model and thus
	 * include references to nodes, but the abstraction of 
	 * this class does not mandate it.
	 * 
	 * @return
	 */
	List<S> getSteps();
	
	T getParent();
	S getLastStep();
	
	// Create a path by appending a step to this path
	T subPath(S step);	
}
