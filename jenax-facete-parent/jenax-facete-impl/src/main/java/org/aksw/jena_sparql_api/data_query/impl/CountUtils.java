package org.aksw.jena_sparql_api.data_query.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.util.range.CountInfo;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.Range;


//interface Relation2 {
//	joinOn();
//	filter(Expr)
//
//	getRoot();
//}

public class CountUtils {
    public static CountInfo toCountInfo(Range<Long> range) {
        CountInfo result;

        if(range.hasLowerBound()) {
            long lowerBound = range.lowerEndpoint();
            boolean hasMoreItems = !(range.hasUpperBound() && lowerBound == range.upperEndpoint().longValue());
            result = new CountInfo(lowerBound, hasMoreItems, null);
        } else {
            throw new IllegalArgumentException("Range must have a lower bound");
        }

        return result;
    }

    public static Map<Node, Fragment2> createQueriesPreCountCore(
        Map<Node, Fragment2> overrides,
        Fragment3 defaultRelation, // TODO Maybe use a lambda Function<Node, BinaryRelation> instead
        Var countVar,
        Collection<Node> properties) {


        Map<Node, Fragment2> result = new HashMap<>();

        // Use the default relation for every property that is not in the overrides map
        for(Node p : properties) {
            Fragment2 r = overrides.get(p);

            if(r == null) {
                Fragment3 tr = null; //defaultRelation.filterP(p);
                Fragment2 br = new Fragment2Impl(tr.getElement(), tr.getS(), tr.getO());
                result.put(p, br);
            } else {
                result.put(p, r);
            }
        }


        return result;
    }

//	public static Query createQueryCount(Map<Node, BinaryRelation> components, Long subLimit) {
//		return null;
//
//
//	}
//
//createQueriesPreCount: function(facetRelationIndex, countVar, properties, rowLimit) {
//    var result = this.createQueriesPreCountCore(facetRelationIndex, countVar, properties, function(relation, property, countVar) {
//        var r = RelationUtils.createQueryRawSize(relation, property, countVar, rowLimit);
//        return r;
//    });
//
//    return result;
//},
}
