package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.util.Objects;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefDcat;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefGit;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefGraph;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefOp;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataPodFactoryImpl
    implements DataRefVisitor<RdfDataPod>
{
    private static Logger logger = LoggerFactory.getLogger(DataPodFactoryImpl.class);

    protected OpVisitor<? extends RdfDataPod> opExecutor;

    public DataPodFactoryImpl(OpVisitor<? extends RdfDataPod> opExecutor) {
        super();
        this.opExecutor = Objects.requireNonNull(opExecutor);
    }

    @Override
    public RdfDataPod visit(DataRefUrl dataRef) {
        throw new RuntimeException("no user handler");

//		// Check the static datasets of the executor first
//		// TODO HACK - Add an interface to access an executor's task context
//		String url = dataRef.getDataRefUrl();
//
//		TaskContext context = ((OpExecutorDefault)opExecutor).getTaskContext();
//		Model m = context.getCtxModels().get(url);
//		RdfDataPod result;
//		if(m != null) {
//			logger.info("Accessed input model");
//			result = DataPods.fromModel(m);
//		} else {
//			result = DataPods.fromUrl(dataRef);
//		}
//
//		return result;
    }

    @Override
    public RdfDataPod visit(DataRefSparqlEndpoint dataRef) {
        RdfDataPod result = DataPods.fromSparqlEndpoint(dataRef);
        return result;
    }

    @Override
    public RdfDataPod visit(DataRefExt dataRef) {
        throw new RuntimeException("No override with custom handler");
    }

    @Override
    public RdfDataPod visit(DataRefCatalog dataRef) {
        throw new RuntimeException("To be done");
    }

    @Override
    public RdfDataPod visit(DataRefOp dataRef) {
        // We assume the Op type here
        Op op = (Op)Objects.requireNonNull(dataRef.getOp());
        RdfDataPod result = op.accept(opExecutor);

        return result;
    }

    @Override
    public RdfDataPod visit(DataRefDcat dataRef) {
        throw new RuntimeException("No override with custom handler");
    }

    @Override
    public RdfDataPod visit(DataRefGit dataRef) {
        throw new RuntimeException("No override with custom handler");
    }

    @Override
    public RdfDataPod visit(DataRefGraph dataRef) {
        throw new RuntimeException("No override with custom handler");
    }

//	@Override
//	public RdfDataObject visit(DataRefEmpty dataRef) {
//		RdfDataObject result = DataObjects.empty();
//		return result;
//	}

}
