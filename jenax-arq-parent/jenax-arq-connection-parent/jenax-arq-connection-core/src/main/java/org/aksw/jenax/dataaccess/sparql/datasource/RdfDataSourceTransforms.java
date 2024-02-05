package org.aksw.jenax.dataaccess.sparql.datasource;

import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;

public class RdfDataSourceTransforms {
    public static RdfDataSourceTransform of(SparqlStmtTransform stmtTransform) {
        return dataSource -> RdfDataSources.wrapWithStmtTransform(dataSource, stmtTransform);
    }
}
