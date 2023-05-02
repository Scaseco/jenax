package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.spatial.property_functions.box.WithinBoxGeomPF;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.util.FmtUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WithinBoxMultipolygonPF extends WithinBoxGeomPF {
    private static final int GEOM_POS = 0;
    private static final int LIMIT_POS = 1;

    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        List<Node> objectArgs = argObject.getArgList();
        if (objectArgs.size() < 1) {
            throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": Minimum of 1 arguments.");
        } else if (objectArgs.size() > 2) {
            throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": Maximum of 2 arguments.");
        }
        Node geomLit = argObject.getArg(GEOM_POS);

        Node limitNode = objectArgs.size() > LIMIT_POS ? objectArgs.get(LIMIT_POS) : null;

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geomLit);

        if (geometryWrapper.getGeometryType().equals(Geometry.TYPENAME_MULTIPOLYGON)) {
            MultiPolygon mp = (MultiPolygon) geometryWrapper.getParsingGeometry();

            Iterator<Binding> iterator = IntStream.range(0, mp.getNumGeometries())
                    .mapToObj(mp::getGeometryN)
                    .map(g -> GeometryWrapperFactory.createGeometry(g, geometryWrapper.getSrsURI(), geometryWrapper.getGeometryDatatypeURI()))
                    .map(w -> new PropFuncArg(limitNode == null ? List.of(w.asNode()) : List.of(w.asNode(), limitNode)))
                    .map(argObjectTmp -> super.execEvaluated(binding, argSubject, predicate, argObjectTmp, execCxt))
                    .flatMap(qIt -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(qIt, Spliterator.ORDERED), false))
                    .iterator();
            return QueryIterPlainWrapper.create(iterator, execCxt);
        }

        return super.execEvaluated(binding, argSubject, predicate, argObject, execCxt);
    }

    public static void main(String[] args) {
        Iterator<Integer>
                it = Arrays.asList(1, 2, 3, 4, 5)
                .iterator();
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false).forEach(System.out::println);
//        Stream.generate(it::next).forEach(System.out::println);

    }
}
