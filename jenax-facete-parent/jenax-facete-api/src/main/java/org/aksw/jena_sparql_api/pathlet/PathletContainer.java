package org.aksw.jena_sparql_api.pathlet;

/**
 * A pathlet is a relationlet with designated source and target variables plus
 * operators for concatenation of pathlets
 * 
 * @author raven
 *
 */
interface PathletContainer
	extends Pathlet
{
	/**
	 * Add a left-join
	 * 
	 * { // ElementGroup
	 *   lhs
	 *   OPTIONAL {
	 *     rhs
	 *   }
	 * }
	 * 
	 * @return
	 */
//	Pathlet optional(Pathlet rhs, boolean createIfNotExists);
	
	// get or create an optional block with the given label
	//Pathlet optional(String label, boolean createIfNotExists);
	
	// get or create an optional block with a null label
//    default Pathlet optional(boolean createIfNotExists) {
//    	return optional((String)null, createIfNotExists);
//    }
//
//    default Pathlet optional() {
//    	return optional(true);
//    }
	
    static Pathlet as(String alias) {
	    return null;
    }
}