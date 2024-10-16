package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.optimize.Rewrite;

public class RdfDataSourceWrapperWithRewrite<T extends RdfDataSource>
    extends RdfDataSourceWrapperBase<T>
{
    protected Rewrite rewrite;

    public RdfDataSourceWrapperWithRewrite(T delegate, Rewrite rewrite) {
        super(delegate);
        this.rewrite = Objects.requireNonNull(rewrite);
    }

    public Rewrite getRewrite() {
        return rewrite;
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection conn = super.getConnection();
        RDFConnection result = RDFConnectionUtils.wrapWithStmtTransform(conn, stmt -> SparqlStmtUtils.applyOpTransform(stmt, rewrite::rewrite));
        return result;
    }
}
