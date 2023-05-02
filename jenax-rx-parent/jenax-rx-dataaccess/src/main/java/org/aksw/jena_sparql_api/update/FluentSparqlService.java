package org.aksw.jena_sparql_api.update;

import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.aksw.jena_sparql_api.core.FluentBase;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryHttp;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactoryOverSparqlQueryConnection;
import org.aksw.jenax.arq.connection.core.UpdateExecutionFactory;
import org.aksw.jenax.arq.connection.core.UpdateExecutionFactorySparqlUpdateConnection;
import org.aksw.jenax.connectionless.SparqlService;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.Context;

import com.google.common.base.Supplier;

public class FluentSparqlService<P>
    extends FluentBase<SparqlService, P>
{

    //private FluentQueryExecutionFactory fluentQef =
    //private FluentUpdateExecutionFactory fluentUef;
//    protected SparqlService sparqlService;

//    private FluentQueryExecutionFactoryEndable fluentQef;
//    private FluentUpdateExecutionFactoryEndable fluentUef;
//
    public FluentSparqlService(SparqlService sparqlService) {
        this.fn = sparqlService;
//        this.fluentQef = new FluentQueryExecutionFactoryEndable(this);
//        this.fluentUef = new FluentUpdateExecutionFactoryEndable(this);
    }
//
//    public FluentQueryExecutionFactoryEndable configureQuery() {
//        return fluentQef;
//    }
//
//    public FluentUpdateExecutionFactoryEndable configureUpdate() {
//        return fluentUef;
//    }
//
//    public SparqlService create() {
//        //QueryExecutionFactory qef = fluentQef.create();
//        //UpdateExecutionFactory uef = fluentUef.create();
//        return sparqlService;
//    }
//
//    public <T extends UpdateExecutionFactory & DatasetListenable> FluentSparqlService withUpdateListeners(Function<SparqlService, T> updateStrategy, Collection<DatasetListener> listeners) {
//
//        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
//        T uef = updateStrategy.apply(sparqlService);
//        uef.getDatasetListeners().addAll(listeners);
//        //UpdateContext updateContext = new UpdateContext(sparqlService, batchSize, containmentChecker);
//
//        //UpdateExecutionFactory uef = new UpdateExecutionFactoryEventSource(updateContext);
//        sparqlService = new SparqlServiceImpl(qef, uef);
//
//        return this;
//    }
//

    public FluentSparqlServiceFn<FluentSparqlService<P>> config() {
        final FluentSparqlService<P> self = this;

        final FluentSparqlServiceFn<FluentSparqlService<P>> result = new FluentSparqlServiceFn<FluentSparqlService<P>>();
        result.setParentSupplier(new Supplier<FluentSparqlService<P>>() {
                @Override
                public FluentSparqlService<P> get() {
                    Function<SparqlService, SparqlService> transform = result.value();
                    fn = transform.apply(fn);

                    return self;
                }
            });

        return result;
    }

    public static FluentSparqlService<?> forModel() {
        Model model = ModelFactory.createDefaultModel();
        FluentSparqlService<?> result = from(model);
        return result;
    }

    public static FluentSparqlService<?> from(Model model) {
        FluentSparqlService<?> result = from(model, null);

        return result;
    }

    public static FluentSparqlService<?> from(Model model, Context context) {
        QueryExecutionFactory qef = FluentQueryExecutionFactory.model(model, context).create();
        UpdateExecutionFactory uef = FluentUpdateExecutionFactory.from(model, context).create();

        FluentSparqlService<?> result = from(qef, uef);

        return result;
    }


    public static FluentSparqlService<?> forDataset() {
        Dataset dataset = DatasetFactory.createGeneral();
        FluentSparqlService<?> result = from(dataset);
        return result;
    }

    public static FluentSparqlService<?> from(Dataset dataset) {
        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(dataset).create();
        UpdateExecutionFactory uef = FluentUpdateExecutionFactory.from(dataset).create();

        FluentSparqlService<?> result = from(qef, uef);

        return result;
    }

    public static FluentSparqlService<?> forDatasetGraph() {
        DatasetGraph datasetGraph = DatasetGraphFactory.createGeneral();
        FluentSparqlService<?> result = from(datasetGraph);
        return result;
    }

    public static FluentSparqlService<?> from(DatasetGraph datasetGraph) {
        FluentSparqlService<?> result = from(datasetGraph, null);
        return result;
    }

    public static FluentSparqlService<?> from(DatasetGraph datasetGraph, Context context) {
        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(datasetGraph, context).create();
        UpdateExecutionFactory uef = FluentUpdateExecutionFactory.from(datasetGraph, context).create();

        FluentSparqlService<?> result = from(qef, uef);

        return result;
    }


    public static FluentSparqlService<?> http(String service, String ... defaultGraphs) {
        return http(service, Arrays.asList(defaultGraphs));
    }

    public static FluentSparqlService<?> http(String service, String defaultGraph, HttpClient httpClient) {
        return http(service, new DatasetDescription(Collections.singletonList(defaultGraph), Collections.<String>emptyList()), httpClient);
    }

    public static FluentSparqlService<?> http(SparqlServiceReference sparqlService) {
        return http(sparqlService.getServiceURL(), sparqlService.getDatasetDescription());
    }

    public static FluentSparqlService<?> http(String service, List<String> defaultGraphs) {
        DatasetDescription datasetDescription = new DatasetDescription(defaultGraphs, Collections.<String>emptyList());

        return http(service, datasetDescription, null);
    }

    public static FluentSparqlService<?> http(String service, DatasetDescription datasetDescription) {
        return http(service, datasetDescription, null);
    }

    public static FluentSparqlService<?> http(String service, DatasetDescription datasetDescription, HttpClient httpClient) {
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp(service, datasetDescription, httpClient);
        UpdateExecutionFactory uef = new UpdateExecutionFactoryHttp(service, datasetDescription, httpClient);
        return from(service, datasetDescription, qef, uef);
    }

    public static FluentSparqlService<?> from(QueryExecutionFactory qef, UpdateExecutionFactory uef) {
        FluentSparqlService<?> result = from(null, null, qef, uef);
        return result;
    }


    public static FluentSparqlService<?> from(String serviceUri, DatasetDescription datasetDescription, QueryExecutionFactory qef, UpdateExecutionFactory uef) {
        SparqlService sparqlService = new SparqlServiceImpl(serviceUri, datasetDescription, qef, uef);
        FluentSparqlService<?> result = from(sparqlService);
        return result;
    }

    public static FluentSparqlService<?> from(SparqlService sparqlService) {
        FluentSparqlService<?> result = new FluentSparqlService<Object>(sparqlService);
        return result;
    }


    public static FluentSparqlService<?> from(RDFConnection conn) {
        SparqlService sparqlService = new SparqlServiceImpl(
                null,
                null,
                new QueryExecutionFactoryOverSparqlQueryConnection(conn),
                new UpdateExecutionFactorySparqlUpdateConnection(conn));

        FluentSparqlService<?> result = from(sparqlService);
        return result;
    }
}
