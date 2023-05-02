package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.util.FmtUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

import java.util.Iterator;
import java.util.stream.IntStream;

public class STDumpPF extends PFuncSimple {

    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {

        if (!subject.isVariable()) {
            throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": subject must be variable.");
        }
        Var s = Var.alloc(subject);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(object);

        if (geometryWrapper.getGeometryType().equals(Geometry.TYPENAME_MULTIPOLYGON)) {
            MultiPolygon mp = (MultiPolygon) geometryWrapper.getParsingGeometry();

            Iterator<Binding> iterator = IntStream.range(0, mp.getNumGeometries())
                    .mapToObj(mp::getGeometryN)
                    .map(g -> GeometryWrapperFactory.createGeometry(g, geometryWrapper.getSrsURI(), geometryWrapper.getGeometryDatatypeURI()))
                    .map(GeometryWrapper::asNode)
                    .map(node -> BindingFactory.binding(s, node))
                    .iterator();
            return QueryIterPlainWrapper.create(iterator, execCxt);
        }

        return QueryIterSingleton.create(BindingFactory.binding(s, object), execCxt);
    }

}
