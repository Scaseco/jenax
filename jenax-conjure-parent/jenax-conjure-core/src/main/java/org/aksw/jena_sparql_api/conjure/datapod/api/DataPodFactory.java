package org.aksw.jena_sparql_api.conjure.datapod.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRef;

public interface DataPodFactory {
	DataPod create(DataRef dataRef);
}
