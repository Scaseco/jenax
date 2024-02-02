package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import com.uber.h3core.H3Core;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Accumulator;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory;
import org.apache.jena.sparql.expr.aggregate.AggCustom;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.sse.SSE;
import org.apache.sedona.common.Functions;
import org.apache.sedona.common.utils.H3Utils;
import org.locationtech.jts.geom.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class H3ToGeometryAgg {

    static { LogCtl.setLogging(); }

    /**
     * Execution of a custom aggregate is with accumulators. One accumulator is
     * created for the factory for each group in a query execution.
     */
    static AccumulatorFactory h3CellIdAccumulatorFactory = new AccumulatorFactory() {
        @Override
        public Accumulator createAccumulator(AggCustom agg, boolean distinct) { return new H3CellIdAccumulator(agg) ; }
    } ;

    /**
     * H3 cell ID accumulator - collects the cell IDs and makes and outline polygon based on the hexagonal polygons
     * over a group.
     */
    static class H3CellIdAccumulator implements Accumulator {
        List<Long> cellIds = new ArrayList<>();
        private AggCustom agg ;
        H3CellIdAccumulator(AggCustom agg) { this.agg = agg ; }

        /**
         * Function called on each row in a group
         */
        @Override
        public void accumulate(Binding binding, FunctionEnv functionEnv) {
            ExprList exprList = agg.getExprList();
            for (Expr expr : exprList) {
                try {
                    NodeValue nv = expr.eval(binding, functionEnv);
                    // Evaluation succeeded.
                    if (nv.isLiteral()) {
                        cellIds.add(nv.getInteger().longValue());
                    }
                } catch (ExprEvalException ex) {
                }
            }
        }

        /** Function called to retrieve the value for a single group */
        @Override
        public NodeValue getValue() {
            Geometry geom = h3ToGeom(cellIds.stream().mapToLong(Long::longValue).toArray());
            return GeometryWrapperFactory.createGeometry(geom, WKTDatatype.URI).asNodeValue();
        }
    }

    /**
     * get the neighbor cells of the input cell by h3.gridDisk function
     * @param cells: the set of cells
     * @return Multiple Polygons reversed
     */
    public static Geometry h3ToGeom(long[] cells) {
        GeometryFactory geomFactory = CustomGeometryFactory.theInstance();
        Collection<Long> h3 = Arrays.stream(cells).boxed().collect(Collectors.toList());
        return geomFactory.createMultiPolygon(
                H3Utils.h3.cellsToMultiPolygon(h3, true).stream().map(
                        shellHoles -> {
                            List<LinearRing> rings = shellHoles.stream().map(
                                    shell -> geomFactory.createLinearRing(shell.stream().map(latLng -> new Coordinate(latLng.lng, latLng.lat)).toArray(Coordinate[]::new))
                            ).collect(Collectors.toList());
                            LinearRing shell = rings.remove(0);
                            if (rings.isEmpty()) {
                                return geomFactory.createPolygon(shell);
                            } else {
                                return geomFactory.createPolygon(shell, rings.toArray(new LinearRing[0]));
                            }
                        }
                ).toArray(Polygon[]::new)
        );
    }

}