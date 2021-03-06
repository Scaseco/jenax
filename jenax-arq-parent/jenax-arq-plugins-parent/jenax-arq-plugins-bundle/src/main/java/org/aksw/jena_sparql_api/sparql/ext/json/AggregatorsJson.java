package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.GeometryWrapperUtils;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;

//public class AggregatorsJson {
//	public static Aggregator<Binding, NodeValue> aggJsonArray(Expr geomExpr, boolean distinct) {
//		return aggJsonArray(geomExpr, distinct).finish(GeometryWrapper::asNodeValue);
//	}
//
//	public static Aggregator<Binding, GeometryWrapper> aggJsonArray(Expr geomExpr, boolean distinct) {
//		return aggJsonArray(geomExpr, distinct, CustomGeometryFactory.theInstance());
//	}
//
//	public static Aggregator<Binding, JsonArray> aggJsonArray(
//			Expr geomExpr,
//			boolean distinct,
//			GeometryFactory geomFactory) {
//
//		// TODO This approach silently ignores invalid input
//		// We should probably instead yield effectively 'null'
//		return
//			AggBuilder.inputTransform(
//				(Binding binding) -> {
//					NodeValue nv = geomExpr.eval(binding, null);
//					return GeometryWrapperUtils.extractGeometryWrapperOrNull(nv);
//				},
//				AggBuilder.inputFilter(input -> input != null,
//					aggGeometryWrapperCollection(distinct, geomFactory)));
//	}
//
//	/**
//	 * Creates an aggregator that collects geometries into a geometry collection
//	 * All geometries must have the same spatial reference system (SRS).
//	 * The resulting geometry will be in the same SRS.
//	 *
//	 * @param distinct Whether to collect geometries in a set or a list
//	 * @param geomFactory The geometry factory. If null then jena's default one is used.
//	 */
//	public static ParallelAggregator<GeometryWrapper, GeometryWrapper, ?> aggGeometryWrapperCollection(boolean distinct, GeometryFactory geomFactory) {
//
//		GeometryFactory gf = geomFactory == null ? CustomGeometryFactory.theInstance() : geomFactory;
//
//		SerializableSupplier<Collection<GeometryWrapper>> collectionSupplier = distinct
//				? LinkedHashSet::new
//				: ArrayList::new; // LinkedList?
//
//		return AggBuilder.outputTransform(
//			AggBuilder.collectionSupplier(collectionSupplier),
//			col -> {
//				GeometryWrapper r;
//				if (col.isEmpty()) {
//					r = GeometryWrapper.getEmptyWKT();
//				} else {
//					Set<String> srsUris = col.stream().map(GeometryWrapper::getSrsURI).collect(Collectors.toSet());
//					Collection<Geometry> geoms = col.stream().map(GeometryWrapper::getParsingGeometry).collect(Collectors.toList());
//					Geometry geom = gf.createGeometryCollection(geoms.toArray(new Geometry[0]));
//
//					// Mixing SRS not allowed here; convert before aggregation
//					String srsUri = Iterables.getOnlyElement(srsUris);
//
//					r = new GeometryWrapper(geom, srsUri, WKTDatatype.URI);
//				}
//				return r;
//			});
//	}
//
//}
