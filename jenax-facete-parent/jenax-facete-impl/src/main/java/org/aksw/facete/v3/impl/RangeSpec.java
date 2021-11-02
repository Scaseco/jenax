package org.aksw.facete.v3.impl;

import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public interface RangeSpec
	extends Resource
{
	RDFNode getMin();
	void setMin(RDFNode min);

	boolean isMinInclusive();
	void setMinInclusive(boolean onOrOff);

	RDFNode getMax();
	void setMax(RDFNode max);

	boolean isMaxInclusive();
	void setMaxInclusive(boolean onOrOff);
	
	default <T> T getMin(Class<T> clazz) {
		return NodeMappers.from(clazz).toJava(getMin().asNode());
	}

	default <T> T getMax(Class<T> clazz) {
		return NodeMappers.from(clazz).toJava(getMax().asNode());
	}
	
	default <T extends Comparable<T>> Range<T> toRange(Class<T> clazz) {
		T min = getMin(clazz);
		boolean isMinInclusive = isMinInclusive(); 
		
		T max = getMax(clazz);
		boolean isMaxInclusive = isMaxInclusive();
		
		Range<T> result = createRange(min, isMinInclusive, max, isMaxInclusive);
		return result;
	}

	
	/**
	 * Utility function for creating ranges with null values.
	 * Null is interpreted as the absence of a boundary, hence
	 * createRange(null, null) yields Range.all().
	 * 
	 * If min or max is null, the corresponding value for inclusiveness is ignored.
	 * 
	 * 
	 * @param min
	 * @param isMinInclusive
	 * @param max
	 * @param isMaxInclusive
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> createRange(T min, boolean isMinInclusive, T max, boolean isMaxInclusive) {
		BoundType minBoundType = isMinInclusive ? BoundType.CLOSED : BoundType.OPEN;
		BoundType maxBoundType = isMaxInclusive ? BoundType.CLOSED : BoundType.OPEN;

		Range<T> result =
				min == null
					? max == null ? Range.all() : Range.upTo(max, maxBoundType)
					: max == null ? Range.downTo(min, minBoundType) : Range.range(min, minBoundType, max, maxBoundType);
		
		return result;
	}

	/**
	 * Utility function for creating ranges with null values.
	 * Null is interpreted as the absence of a boundary, hence
	 * createRange(null, null) yields Range.all().
	 * 
	 * If min or max is null, the corresponding value for inclusiveness is ignored.
	 * 
	 * @param min
	 * @param minBoundType
	 * @param max
	 * @param maxBoundType
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> createRange(T min, BoundType minBoundType, T max, BoundType maxBoundType) {

		Range<T> result =
				min == null
					? max == null ? Range.all() : Range.upTo(max, maxBoundType)
					: max == null ? Range.downTo(min, minBoundType) : Range.range(min, minBoundType, max, maxBoundType);
		
		return result;
	}

}
