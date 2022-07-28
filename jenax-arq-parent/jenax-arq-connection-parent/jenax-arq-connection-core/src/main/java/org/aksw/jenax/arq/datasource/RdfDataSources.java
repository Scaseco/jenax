package org.aksw.jenax.arq.datasource;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.aksw.jenax.arq.connection.core.RDFConnectionUtils;
import org.aksw.jenax.connection.dataengine.RdfDataEngine;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.datasource.RdfDataSourceDelegateBase;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.system.Txn;

public class RdfDataSources {

    /** Execute a query and invoke a function on the response.
     * Upon returning the internally freshly obtained connection and query execution are closed so
     * the result must be detached from those resources.
     */
    public static <T> T exec(RdfDataSource dataSource, Query query, Function<? super QueryExecution, T> qeToResult) {
        Object[] tmp = new Object[] { null };
        try (RDFConnection conn = dataSource.getConnection()) {
            Txn.executeRead(conn, () -> {
                try (QueryExecution qe = conn.query(query)) {
                    tmp[0] = qeToResult.apply(qe);
                }
            });
        }
        @SuppressWarnings("unchecked")
        T result = (T)tmp[0];
        return result;
    }

    /** Reads the 'engine' attribute from the options (if absent defaults to 'mem')
     *  and instantiates the appropriate data source - if possible */
    public static RdfDataEngine setupRdfDataSource(Map<String, Object> options) throws Exception {
        RdfDataSourceSpecBasicFromMap spec = RdfDataSourceSpecBasicFromMap.wrap(options);

        String sourceType = Optional.ofNullable(spec.getEngine()).orElse("mem");

        RdfDataEngineFactory factory = RdfDataEngineFactoryRegistry.get().getFactory(sourceType);
        if (factory == null) {
            throw new RuntimeException("No RdfDataSourceFactory registered under name " + sourceType);
        }


        RdfDataEngine result = factory.create(options);
        return result;
    }

    public static RdfDataSource applyLinkDecorator(RdfDataSource rdfDataSource, Function<? super RDFLink, ? extends RDFLink> linkXform) {
        return new RdfDataSourceDelegateBase(rdfDataSource) {
            @Override
            public RDFConnection getConnection() {
                RDFConnection base = super.getConnection();
                RDFConnection r = RDFConnectionUtils.wrapWithLinkDecorator(base, linkXform);
                return r;
            }
        };
    }
}
