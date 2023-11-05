package org.aksw.jena_sparql_api.sparql.ext.hash;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.function.FunctionEnv;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

// Unfinished
public class HashCodeAggregators {

    public static HashCode combineUnordered(HashCode a, HashCode b) {
        // Hashing.combineUnordered(null)
        return null;
    }

//    public static Aggregator<Binding, FunctionEnv, GeometryWrapper> aggIntersectionGeometryWrapperCollection(Expr hashCodeExpr, boolean distinct) {
//        return
//            AggBuilder.errorHandler(
//                AggBuilder.inputTransform2(
//                    (Binding b, FunctionEnv env) -> {
//                        try {
//                            NodeValue nv = hashCodeExpr.eval(b, env);
//                            HashCode r = null;
//                            return r;
//                            // RDFDatatypeHashCode.
//                            // return GeometryWrapper.extract(nv);
//                        } catch (VariableNotBoundException e) {}
//                        return null;
//                    },
//                    AggBuilder.inputFilter(input -> input != null,
//                        aggGeometryWrapperCollection(distinct, finisher))),
//                false,
//                ex -> logger.warn("Error while aggregating a collection of geometries", ex),
//                null);
//    }

}
