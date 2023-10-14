package org.aksw.jenax.facete.treequery2.impl;

import java.util.List;
import java.util.Map;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.apache.jena.sparql.core.Var;

// A specification of a relation with ordering, partitioning and filtering
// The attributes of this class should be covered by RelationQuery
public class PartitionedRelationSpec {


    // DONE means that RelationQuery supports the feature.
    protected Long limit; // DONE
    protected Long offset; // DONE
    protected Fragment baseElement; // DONE

    protected Map<FacetStep, Var> premappedPaths;


    protected List<FacetPath> partitionPaths;
    protected NodeQueryOld interPartitionSortConditions; // sort partitions
    protected List<FacetPath> intraPartitionSortConditions; // sort the values within partitions

    protected Fragment1 filter; // immediate filter or set intersection



}
