package org.aksw.jenax.arq.datasource;

import org.aksw.jenax.arq.connection.core.RDFConnectionUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.datasource.RdfDataSourceDelegateBase;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;

public class RdfDataSourceWithLocalCache
    extends RdfDataSourceDelegateBase
{
    public static final String REMOTE_IRI = "env://REMOTE";
    public static final Node REMOTE_NODE = NodeFactory.createURI(REMOTE_IRI);

    /** Requests go to this dataset which is configured to delegate SERVICE >env://REMOTE> requests to the delegate data source */
    public Dataset proxyDataset;

    public RdfDataSourceWithLocalCache(RdfDataSource delegate) {
        super(delegate);
        proxyDataset = createProxyDataset(delegate);
    }

    public static Dataset createProxyDataset(RdfDataSource delegate) {
        Dataset result = DatasetFactory.create();
        ServiceExecutorRegistry registry = new ServiceExecutorRegistry();
        registry.addBulkLink(new ChainingServiceExecutorBulkServiceEnhancer());
        registry.addSingleLink((opExec, opOrig, binding, execCxt, chain) -> {
            QueryIterator r;
            if (opExec.getService().equals(REMOTE_NODE)) {
                RDFConnection base = delegate.getConnection();
                r = RDFConnectionUtils.execService(opExec, base);
            } else {
                r = chain.createExecution(opExec, opOrig, binding, execCxt);
            }
            return r;
        });
        ServiceExecutorRegistry.set(result.getContext(), registry);
        return result;
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection base = RDFConnection.connect(proxyDataset);

        RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(base, query -> {
            Query r = QueryUtils.applyOpTransform(query, op -> {
                Node serviceNode = NodeFactory.createURI("cache:" + REMOTE_IRI);
                return new OpService(serviceNode, op, false);
            });
            return r;
        });

        return result;
    }
}
