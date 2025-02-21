package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceWrapperBase;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.optimize.Rewrite;

@Deprecated // RdfLinkSourceWrapperWithRewrite
public class RdfDataSourceWrapperWithRewrite<T extends RDFDataSource>
    extends RDFDataSourceWrapperBase<T>
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
