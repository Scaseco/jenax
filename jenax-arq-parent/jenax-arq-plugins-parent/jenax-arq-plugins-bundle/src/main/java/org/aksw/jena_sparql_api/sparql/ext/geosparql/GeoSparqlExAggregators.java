package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.jenax.arq.util.binding.BindingEnv;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory;
import org.apache.jena.sparql.function.FunctionEnv;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

public class GeoSparqlExAggregators {

    private static final Logger logger = LoggerFactory.getLogger(GeoSparqlExAggregators.class);

    public static AccumulatorFactory wrap1(BiFunction<? super Expr, ? super Boolean, ? extends Aggregator<BindingEnv, GeometryWrapper>> ctor) {
        return (aggCustom, distinct) -> {
            Expr expr = aggCustom.getExpr();
            Aggregator<BindingEnv, NodeValue> coreAgg = ctor.apply(expr, distinct)
                    .finish(geometryWrapper -> {
                        NodeValue r = geometryWrapper == null
                                ? null
                                : geometryWrapper.asNodeValue();
                        return r;
                    });

            return new AccAdapterJena(coreAgg.createAccumulator());
        };
    }


    /**
     * Return the common item type.
     * If there is no (non-null) item in the input then the result is the given emptyFallback (may be null),
     * otherwise the result will be either Object or a more specific class.
     * Null values in the input are ignored.
     */
    public static Class<?> getCommonItemType(Iterator<?> it, Class<?> emptyFallback) {
        Class<?> result = null;

        while (it.hasNext()) {
            Object obj = it.next();
            if (obj != null) {
                Class<?> clz = obj.getClass();

                if (result == null) {
                    result = clz;
                } else if (clz.isAssignableFrom(result)) {
                    result = clz;
                } else {
                    result = Object.class;
                    break;
                }
            }
        }

        if (result == null) {
            result = emptyFallback;
        }

        return result;
    }

    public static Geometry mostSpecificGeometry(Collection<Geometry> geoms, GeometryFactory geomFactory) {
        Geometry result;
        Class<?> type = getCommonItemType(geoms.iterator(), null);

        if (geoms.isEmpty() || type == null) {
            result = geomFactory.createGeometryCollection();
        } else if (geoms.size() == 1) {
            result = geoms.iterator().next();
        } else if (Polygon.class.isAssignableFrom(type)) {
            result = geomFactory.createMultiPolygon(geoms.toArray(new Polygon[0]));
        } else if (LineString.class.isAssignableFrom(type)) {
            result = geomFactory.createMultiLineString(geoms.toArray(new LineString[0]));
        } else if (Point.class.isAssignableFrom(type)) {
            result = geomFactory.createMultiPoint(geoms.toArray(new Point[0]));
        } else {
            result = geomFactory.createGeometryCollection(geoms.toArray(new Geometry[0]));
        }

        return result;
    }

    public static Aggregator<BindingEnv, GeometryWrapper> aggUnionGeometryWrapperCollection(Expr geomExpr, boolean distinct) {
        GeometryFactory geomFactory = CustomGeometryFactory.theInstance();
        Function<Collection<Geometry>, Geometry> finisher = geoms -> UnaryUnionOp.union(geoms, geomFactory);

        return aggGeometryWrapperCollection(geomExpr, distinct, finisher);
    }

    public static Aggregator<BindingEnv, GeometryWrapper> aggIntersectionGeometryWrapperCollection(Expr geomExpr, boolean distinct) {
        GeometryFactory geomFactory = CustomGeometryFactory.theInstance();
        Function<Collection<Geometry>, Geometry> finisher = geoms -> {
            Iterator<Geometry> it = geoms.iterator();
            if(it.hasNext()) {
                Geometry intersection = it.next(); // take first
                while (it.hasNext()) {
                    intersection = intersection.intersection(it.next());
                }
                return intersection;
            } else {
                return geomFactory.createGeometryCollection();
            }
        };

        return aggGeometryWrapperCollection(geomExpr, distinct, finisher);
    }

//
//    public static Aggregator<Binding, NodeValue> aggGeometryCollection(Expr geomExpr, boolean distinct) {
//        return aggGeometryWrapperCollection(geomExpr, distinct).finish(GeometryWrapper::asNodeValue);
//    }

    public static Aggregator<BindingEnv, GeometryWrapper> aggGeometryWrapperCollection(Expr geomExpr, boolean distinct) {
        GeometryFactory geomFactory = CustomGeometryFactory.theInstance();
        Function<Collection<Geometry>, Geometry> finisher = geoms -> geomFactory.createGeometryCollection(geoms.toArray(new Geometry[0]));


        return aggGeometryWrapperCollection(geomExpr, distinct, finisher);
    }

    public static Aggregator<BindingEnv, GeometryWrapper> aggGeometryWrapperCollection(
            Expr geomExpr,
            boolean distinct,
            Function<Collection<Geometry>, Geometry> finisher) {

        return
            AggBuilder.errorHandler(
                AggBuilder.inputTransform(
                    (BindingEnv benv) -> {
                        try {
                            Binding b = benv.getBinding();
                            FunctionEnv env = benv.getFunctionEnv();
                            NodeValue nv = geomExpr.eval(b, env);
                            return GeometryWrapper.extract(nv);
                        } catch (VariableNotBoundException e) {}
                        return null;
                    },
                    AggBuilder.inputFilter(input -> input != null,
                        aggGeometryWrapperCollection(distinct, finisher))),
                false,
                ex -> logger.warn("Error while aggregating a collection of geometries", ex),
                null);
    }

    /**
     * Creates an aggregator that collects geometries into a geometry collection
     * All geometries must have the same spatial reference system (SRS).
     * The resulting geometry will be in the same SRS.
     *
     * @param distinct Whether to collect geometries in a set or a list
     * @param geomFactory The geometry factory. If null then jena's default one is used.
     */
    public static ParallelAggregator<GeometryWrapper, GeometryWrapper, ?> aggGeometryWrapperCollection(
            boolean distinct,
            Function<Collection<Geometry>, Geometry> finisher
    ) {
        SerializableSupplier<Collection<GeometryWrapper>> collectionSupplier = distinct
                ? LinkedHashSet::new
                : ArrayList::new; // LinkedList?

        return AggBuilder.outputTransform(
            AggBuilder.collectionSupplier(collectionSupplier),
            col -> {
                GeometryWrapper r;
//                if (col.isEmpty()) {
//                    r = GeometryWrapper.getEmptyWKT();
//                } else {
                    Set<String> srsUris = col.stream().map(GeometryWrapper::getSrsURI).collect(Collectors.toSet());
                    Collection<Geometry> geoms = col.stream().map(GeometryWrapper::getParsingGeometry).collect(Collectors.toList());
                    Geometry geom = finisher.apply(geoms);


                    // Mixing SRS not allowed here; convert before aggregation
                    String srsUri = Iterables.getOnlyElement(srsUris, SRS_URI.DEFAULT_WKT_CRS84);

                    r = new GeometryWrapper(geom, srsUri, WKTDatatype.URI);
//                }
                return r;
            });
    }

}
