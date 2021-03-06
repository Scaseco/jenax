package org.aksw.jenax.sparql.rx.op;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import org.aksw.commons.lambda.serializable.SerializableConsumer;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.commons.rx.function.RxFunction;
import org.aksw.jena_sparql_api.rx.io.resultset.SPARQLResultExProcessor;
import org.aksw.jena_sparql_api.rx.io.resultset.SPARQLResultExProcessorBuilder;
import org.aksw.jena_sparql_api.rx.io.resultset.SparqlMappers;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

import io.reactivex.rxjava3.core.Flowable;

public class FlowOfDatasetOps {

    public static RxFunction<Dataset, Dataset> mapWithSparql(Query query) {
        return mapWithSparql(Collections.singleton(new SparqlStmtQuery(query)));
    }

    public static RxFunction<Dataset, Dataset> mapWithSparql(Collection<? extends SparqlStmt> stmts) {
        return mapWithSparql(stmts, DatasetGraphFactory::create, cxt -> {});
    }

    public static Flowable<Quad> flatMapToQuads(Flowable<Dataset> in) {
        return in.flatMap(ds -> Flowable.fromIterable(() -> ds.asDatasetGraph().find()));
    }


    /**
     * Map a dataset to a new dataset by running a sequence of sparql statements.
     *
     *
     * @param sparqlStmts
     * @param datasetGraphSupplier
     * @param contextHandler
     * @return
     */
    public static RxFunction<Dataset, Dataset> mapWithSparql(
            Collection<? extends SparqlStmt> sparqlStmts,
            SerializableSupplier<? extends DatasetGraph> datasetGraphSupplier,
            SerializableConsumer<Context> contextMutator) {

        // FIXME wrap the connection with the context mutator

        RxFunction<Dataset, Dataset> mapper = upstream -> {
            SPARQLResultExProcessor resultProcessor = SPARQLResultExProcessorBuilder.createForQuadOutput().build();

            Function<RDFConnection, Iterable<Dataset>> connectionBasedMapper = SparqlMappers.createMapperDataset(
                    sparqlStmts, resultProcessor, datasetGraphSupplier);
            Function<Dataset, Iterable<Dataset>> datasetBasedMapper = SparqlMappers.mapDatasetToConnection(connectionBasedMapper);

            return upstream.flatMap(dataset -> {
                Iterable<Dataset> datasets = datasetBasedMapper.apply(dataset);
                return Flowable.fromIterable(datasets);
            });
        };


        return mapper;
    }

    
    public static Flowable<DatasetOneNg> namedGraphs(Dataset dataset) {
    	return Flowable.just(dataset).compose(flatMapNamedGraphs());
    }
    
    /**
     * For any non-DatasetOnNg input map its named graphs to individual
     * DatasetOnNg instances.
     * Does not copy triples - the new datasets are views over the respective graphs of the input dataset
     *
     * @return
     */
    public static RxFunction<Dataset, DatasetOneNg> flatMapNamedGraphs() {
        return upstream -> upstream.flatMap(ds ->
            ds instanceof DatasetOneNg
                ? Flowable.just((DatasetOneNg)ds)
                : Flowable.fromIterable(() -> ds.listNames())
                    .map(iri -> DatasetOneNgImpl.create(iri, ds.getNamedModel(iri).getGraph())));
    }

}
