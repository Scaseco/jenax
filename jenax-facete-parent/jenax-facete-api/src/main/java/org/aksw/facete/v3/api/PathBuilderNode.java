package org.aksw.facete.v3.api;

import java.util.Map;
import java.util.function.Supplier;

import org.aksw.facete.v3.bgp.api.BinaryRelation;;

/**
 * What should be the nature of this class?
 * - Is it only an API over a model; and actual query generation is based on the underlying model?
 * - Is it a supplier for queries, and thus features a .getQuery() method <-- I think this is the right way
 *
 *
 *
 * @author Claus Stadler, Oct 12, 2018
 *
 * @param <T>
 */
public class PathBuilderNode<T> {

    //Supplier<Map<String, Relation>>
    Supplier<Map<String, BinaryRelation>> relationSupplier;


//	PathBuilderNode<T> as(String name) {
//		return this;
//	}
//
//	// Navigation via all predicates
//	PathBuilderNode step() {
//	}
//
//	// Navigation via specific predicates
//	PathBuilderNode step(Node ... nodes) {
//	}



//	PathBuilderNode fwd() {
//
//	}
}

class PathBuilderNodeTmp
//	implements PathTraitNode<>
{

}