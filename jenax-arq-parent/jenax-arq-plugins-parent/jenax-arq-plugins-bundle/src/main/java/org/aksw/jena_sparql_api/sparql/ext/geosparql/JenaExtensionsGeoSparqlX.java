package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.jena_sparql_api.sparql.ext.util.JenaExtensionUtil;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionGenerator;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.locationtech.jts.geom.Geometry;

public class JenaExtensionsGeoSparqlX {

    public static void register() {
        loadDefs(FunctionRegistry.get());

        AggregateRegistry.register(
                GeoSPARQL_URI.GEOF_URI + "collect",
                GeoSparqlExAggregators.wrap1(GeoSparqlExAggregators::aggGeometryWrapperCollection));

        AggregateRegistry.register(
                GeoSPARQL_URI.GEOF_URI + "aggUnion",
                GeoSparqlExAggregators.wrap1(GeoSparqlExAggregators::aggUnionGeometryWrapperCollection));

        AggregateRegistry.register(
                GeoSPARQL_URI.GEOF_URI + "aggIntersection",
                GeoSparqlExAggregators.wrap1(GeoSparqlExAggregators::aggIntersectionGeometryWrapperCollection));

    }

    public static void loadDefs(FunctionRegistry registry) {
        TypeMapper.getInstance().registerDatatype(RDFDatatypeWkbLiteral.INSTANCE);
        TypeMapper.getInstance().registerDatatype(RDFDatatypeGeoJSON.INSTANCE);

        registry.put(GeoSPARQL_URI.GEOF_URI + "wkb2wkt", F_Wkb2Wkt.class);
        registry.put(GeoSPARQL_URI.GEOF_URI + "parsePolyline", F_ParsePolyline.class);
        registry.put(GeoSPARQL_URI.GEOF_URI + "asGeoJSON", F_AsGeoJSON.class);

        // Ensure GeoSPARQL datatypes are available
        // TODO Our plugin should be loaded after geosparql; but I couldn't find whether the geosparql module
        //   is loaded with JenaSubsystemLifecycle and if so what level it uses.
        WKTDatatype.registerDatatypes();



        FunctionBinder binder = JenaExtensionUtil.getDefaultFunctionBinder();
        FunctionGenerator generator = binder.getFunctionGenerator();

        // Define two-way Geometry - GeometryWrapper coercions
        generator.getConverterRegistry()
            .register(Geometry.class, GeometryWrapper.class,
                    geometry -> new GeometryWrapper(geometry, WKTDatatype.URI),
                    GeometryWrapper::getParsingGeometry)
            ;

        // Declare that the conversion of Geometry to GeometryWrapper
        // yields an RDF-compatible java object w.r.t. Jena's TypeMapper
        binder.getFunctionGenerator().getJavaToRdfTypeMap().put(Geometry.class, GeometryWrapper.class);

        // Map GeometryWrapper to the IRI of the WKT datatype
        // WKTDatatype.getJavaClass() in Jena4 incorrectly returns null instead of GeometryWrapper.class
        generator.getTypeByClassOverrides().put(GeometryWrapper.class, WKTDatatype.URI);

        binder.registerAll(GeoSparqlExFunctions.class);
//			binder.register(GeoFunctionsJena.class.getMethod("simplifyDp", Geometry.class, double.class, boolean.class));
//			binder.register(GeoFunctionsJena.class.getMethod("centroid", Geometry.class));

        // FunctionRegistry.get().put(ns + "nearestPoints", uri -> new E_ST_NearestPoints());

        PropertyFunctionRegistry.get().put(GeoSPARQL_URI.SPATIAL_URI + "withinBoxMultipolygonGeom", WithinBoxMultipolygonPF.class);
        PropertyFunctionRegistry.get().put(GeoSPARQL_URI.SPATIAL_URI + "st_dump", STDumpPF.class);
        registry.put(GeoSPARQL_URI.SPATIAL_URI + "st_voronoi_polygons", F_ST_VoronoiPolygons.class);
    }
}
