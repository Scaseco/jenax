package org.aksw.jenax.dataaccess.sparql.datasource;

import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

public class DecoratedRDFDataSource
    extends RDFDataSourceWrapperBase<RDFDataSource>
{
    public DecoratedRDFDataSource(RDFDataSource delegate) {
        super(delegate);
    }

    public static DecoratedRDFDataSource of(RDFDataSource dataSource) {
//        DecoratedRDFEngine result = engine instanceof DecoratedRDFEngine decoratedEngine
//            ? (DecoratedRDFEngine)decoratedEngine
//            : new DecoratedRDFEngine<>(engine, engine.getDataSource());
        return new DecoratedRDFDataSource(dataSource);
        // return result;
    }

    public DecoratedRDFDataSource decorate(RdfDataSourceTransform transform) {
        RDFDataSource oldDelegate = delegate;
        RDFDataSource newDelegate = transform.apply(oldDelegate);
        this.delegate = newDelegate;
        return this;
    }

    public DecoratedRDFDataSource decorate(RDFLinkTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(tmp);
    }

    public DecoratedRDFDataSource decorate(SparqlStmtTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(tmp);
    }

    public DecoratedRDFDataSource decorate(Rewrite transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(tmp);
    }

    public DecoratedRDFDataSource decorate(ExprTransform transform) {
        RdfDataSourceTransform tmp = RdfDataSourceTransforms.of(transform);
        return decorate(tmp);
    }
}
