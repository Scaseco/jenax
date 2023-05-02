package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.util.Iterator;
import java.util.Objects;
import java.util.Map.Entry;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.DcatUtils;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefDcat;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefGit;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefGraph;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.conjure.dataset.engine.TaskContext;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
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

    /** The dataset against which to resolve DataRefGraph references */
    protected Dataset dataset;

    public DataPodFactoryAdvancedImpl(
            Dataset dataset,
            OpVisitor<? extends RdfDataPod> opExecutor,
            HttpResourceRepositoryFromFileSystem repo) {
        super(opExecutor);

        this.dataset = dataset;
        this.repo = repo;
    }


    @Override
    public RdfDataPod visit(DataRefUrl dataRef) {
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
    public RdfDataPod visit(DataRefGraph dataRef) {
        Objects.requireNonNull(dataset, "Cannot resolve DataRefGraph because no dataset has been set");

        String graphIri = dataRef.getGraphIri();
        Model model = dataset.getNamedModel(graphIri);

        return DataPods.fromModel(model);
    }

    @Override
    public RdfDataPod visit(DataRefDcat dataRef) {

        TaskContext context = ((OpExecutorDefault)opExecutor).getTaskContext();

        // Resource dcatRecord = dataRef.getDcatRecord();
        Node dcatRecord = dataRef.getDcatRecordNode();

//        Iterator<Entry<String, Model>> it =
//                Stream.concat(
//                    Stream.<Entry<String, Model>>of(new SimpleEntry<>("provided", dcatRecord.getModel())),
//                    context.getCtxModels().entrySet().stream()).iterator();

        Iterator<Entry<String, Model>> it = context.getCtxModels().entrySet().stream().iterator();

        //RDFDataMgr.write(System.out, dataRef.getDcatResource().getModel(), RDFFormat.TURTLE_PRETTY);


        DcatDistribution dist = null;
        for (; dist == null && it.hasNext();) {
            Entry<String, Model> e = it.next();
            Model candidateModel = e.getValue();
            Resource r = candidateModel.wrapAsResource(dcatRecord);
            // Resource r = dcatRecord.inModel(candidateModel);
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
    public RdfDataPod visit(DataRefGit dataRef) {
        return super.visit(dataRef);
    }
};