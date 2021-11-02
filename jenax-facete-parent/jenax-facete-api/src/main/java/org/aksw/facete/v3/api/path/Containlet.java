package org.aksw.facete.v3.api.path;

import java.util.Collection;

public interface Containlet {
	Containlet getParent();
	Collection<Triplet> getTriplets();
}
