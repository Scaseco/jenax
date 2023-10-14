package org.aksw.jenax.sparql.fragment.api;

import java.util.List;

import org.apache.jena.sparql.core.Var;

/** Not yet used - Interface for sparql-based objects that are partitioned by
 * certain variables */
public interface HasPartitionVars {
    List<Var> getPartitionVars();
}
