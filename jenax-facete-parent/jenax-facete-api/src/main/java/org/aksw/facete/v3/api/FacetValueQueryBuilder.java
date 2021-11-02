package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Idea for API improvement to give more control over how to construct the query for facet values
 * 
 * Status quo: facetDirNode.facetValueCounts().exec()
 * Goal of this class: facetDirNode.facetValues().withCounts().includeAbsent().query().exec();
 * 
 * @author Claus Stadler, Dec 29, 2018
 *
 * @param <T>
 */
public interface FacetValueQueryBuilder<T extends RDFNode> {
	// TODO Add a capabilities() method so client code can query for supported features
	
	FacetDirNode parent();
	FacetValueQueryBuilder<FacetValueCount> withCounts(boolean onOrOff);

	default FacetValueQueryBuilder<FacetValueCount> withCounts() {
		return withCounts(true);
	}

	default FacetValueQueryBuilder<FacetValueCount> withoutCounts() {
		return withCounts(false);
	}

	
	FacetValueQueryBuilder<T> includeAbsent(boolean onOrOff);

	default FacetValueQueryBuilder<T> withAbsent() {
		return includeAbsent(true);
	}

	default FacetValueQueryBuilder<T> withoutAbsent() {
		return includeAbsent(false);
	}

	
	
	<X extends RDFNode> FacetValueQueryBuilder<X> itemsAs(Class<X> itemClazz);
	
	
//	default FacetValueQueryBuilder<T> includeAbsent() {
//		return includeAbsent(true);
//	}

	DataQuery<T> query();
	//DataQuery2<T> query2();
}
