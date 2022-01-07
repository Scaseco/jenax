package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.DcatUtils;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefDcat;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefGit;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.conjure.dataset.engine.TaskContext;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends DataObjectFactory with advanced handling of DataRefUrl using a repository
 *
 * @author raven
 *
 */
public class DataPodFactoryAdvancedImpl
    extends DataPodFactoryImpl {

    private static Logger logger = LoggerFactory.getLogger(DataPodFactoryAdvancedImpl.class);

    protected HttpResourceRepositoryFromFileSystem repo;

    public DataPodFactoryAdvancedImpl(
            OpVisitor<? extends RdfDataPod> opExecutor,
            HttpResourceRepositoryFromFileSystem repo) {
        super(opExecutor);

        this.repo = repo;
    }


    @Override
    public RdfDataPod visit(PlainDataRefUrl dataRef) {
        String url = dataRef.getDataRefUrl();

        TaskContext context = ((OpExecutorDefault)opExecutor).getTaskContext();
        Model m = context == null ? null : context.getCtxModels().get(url);
        RdfDataPod result;
        if(m != null) {
            logger.info("Accessed input model for url " + url);
            result = DataPods.fromModel(m);
        } else {
            result = DataPods.create(url, repo);
        }


//		RdfDataPod result = DataPods.create(url, repo);
        return result;
    }


    @Override
    public RdfDataPod visit(PlainDataRefDcat dataRef) {

        TaskContext context = ((OpExecutorDefault)opExecutor).getTaskContext();

        Resource dcatRecord = dataRef.getDcatRecord();

        Iterator<Entry<String, Model>> it =
                Stream.concat(
                    Stream.<Entry<String, Model>>of(new SimpleEntry<>("provided", dcatRecord.getModel())),
                    context.getCtxModels().entrySet().stream()).iterator();

        //RDFDataMgr.write(System.out, dataRef.getDcatResource().getModel(), RDFFormat.TURTLE_PRETTY);


        DcatDistribution dist = null;
        for (; dist == null && it.hasNext();) {
            Entry<String, Model>  e = it.next();
            Model candidateModel = e.getValue();
            Resource r = dcatRecord.inModel(candidateModel);
            // dist = Txn.calculateRead(candidateModel, () -> DcatUtils.resolveDistribution(r));
            dist = DcatUtils.resolveDistribution(r);
        }

        if (dist == null) {
            throw new RuntimeException("Could not resole distribution; maybe the model with information was not registered with the task context?");
        }

        String url = dist.getDownloadUrl();

        // String url = DcatUtils.getFirstDownloadUrl(dcatRecord);
        if(url == null) {
            throw new RuntimeException("Could not obtain a datasource from " + dcatRecord);
        }

        RdfDataPod result = DataPods.create(url, repo);
        return result;
    }


    @Override
    public RdfDataPod visit(PlainDataRefGit dataRef) {
        return super.visit(dataRef);
    }
};