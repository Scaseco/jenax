/**
 *
 */
package org.aksw.jena_sparql_api.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.aksw.jena_sparql_api.core.utils.GraphResource;
import org.aksw.jena_sparql_api.fail.QueryExecutionFactoryAlwaysFail;
import org.aksw.jena_sparql_api.fallback.QueryExecutionFactoryFallback;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.connection.query.QueryExecutionFactoryDataset;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.util.Context;

import com.google.common.base.Supplier;

/**
 * A fluent API for modifying query execution creation by several means such as
 * query rewriting, setting context attributes, or rerouting requests (e.g. to a cache).
 *
 * @author Claus Stadler
 * @author Lorenz Buehmann
 *
 */
public class FluentQueryExecutionFactory<P>
    extends FluentBase<QueryExecutionFactory, P>
{

    //private QueryExecutionFactory qef;

    public FluentQueryExecutionFactory(QueryExecutionFactory qef) {
        this.fn = qef;
    }

    /**
     * Use from instead
     *
     * @param model
     * @return
     */
    @Deprecated
    public static FluentQueryExecutionFactory<?> model(Model model) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryModel(model));
    }

    /**
     * Use from instead
     *
     * @param model
     * @return
     */
    @Deprecated
    public static FluentQueryExecutionFactory<?> model(Model model, Context context) {
        Dataset dataset = DatasetFactory.create(model);
        return from(dataset, context);
    }

    public static FluentQueryExecutionFactory<?> alwaysFail(String serviceUrl) {
        return alwaysFail(serviceUrl, new DatasetDescription());
    }

    public static FluentQueryExecutionFactory<?> alwaysFail(String serviceUrl, String ... defaultGraphs) {
        DatasetDescription dd = new DatasetDescription();
        dd.addAllDefaultGraphURIs(Arrays.asList(defaultGraphs));
        return alwaysFail(serviceUrl, dd);
    }

    public static FluentQueryExecutionFactory<?> alwaysFail(String serviceUrl, DatasetDescription datasetDescription) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryAlwaysFail(serviceUrl, datasetDescription));
    }

    public static FluentQueryExecutionFactory<?> from(Model model) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryModel(model));
    }

    public static FluentQueryExecutionFactory<?> from(QueryExecutionFactory qef) {
        return new FluentQueryExecutionFactory<Object>(qef);
    }

    public static FluentQueryExecutionFactory<?> from(Model model, Context context) {
        Dataset dataset = DatasetFactory.create(model);
        return from(dataset, context);
    }

    public static FluentQueryExecutionFactory<?> from(Graph graph) {
        return from(ModelFactory.createModelForGraph(graph));
    }

    public static FluentQueryExecutionFactory<?> fromFileNameOrUrl(String fileNameOrUrl) {
        Graph graph = new GraphResource(fileNameOrUrl);
        return from(graph);
    }


//    public static FluentQueryExecutionFactory<?> dataset(Dataset dataset) {
//        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryDataset(dataset));
//    }
//
//    public static FluentQueryExecutionFactory<?> dataset(Dataset dataset, Context context) {
//        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryDataset(dataset, context));
//    }

    public static FluentQueryExecutionFactory<?> from(Dataset dataset) {
        return from(dataset, null);
    }

    public static FluentQueryExecutionFactory<?> from(Dataset dataset, Context context) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryDataset(dataset, context));
    }

    public static FluentQueryExecutionFactory<?> from(DatasetGraph datasetGraph, Context context) {
        Dataset dataset = DatasetImpl.wrap(datasetGraph);
        return from(dataset, context);
    }

    public static FluentQueryExecutionFactory<?> defaultDatasetGraph() {
        return FluentQueryExecutionFactory.from(DatasetGraphFactory.createGeneral());
    }

    public static FluentQueryExecutionFactory<?> from(DatasetGraph datasetGraph){
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryDatasetGraph(datasetGraph, false));
    }

    /** @since 4.5.0 */
    public static FluentQueryExecutionFactory<?> from(QueryExecutionFactoryQuery qefq) {
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryBase() {

            @Override
            public QueryExecution createQueryExecution(Query query) {
                return qefq.createQueryExecution(query);
            }

            @Override
            public QueryExecution createQueryExecution(String queryString) {
                throw new RuntimeException("Cannot handle query as string. Wrap with a parser.");
            }

            @Override
            public String getState() {
                return "";
            }

            @Override
            public String getId() {
                return "";
            }
        });
    }

    public static FluentQueryExecutionFactory<?> http(String service, String ... defaultGraphs){
        return http(service, Arrays.asList(defaultGraphs));
    }

    public static FluentQueryExecutionFactory<?> http(String service, Collection<String> defaultGraphs){
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryHttp(service, defaultGraphs));
    }

    public static FluentQueryExecutionFactory<?> http(SparqlServiceReference sparqlService){
        return http(sparqlService.getServiceURL(), sparqlService.getDefaultGraphURIs());
    }

    public static FluentQueryExecutionFactory<?> http(Collection<SparqlServiceReference> sparqlServices){
        if(sparqlServices.size() == 1){
            return http(sparqlServices.iterator().next());
        }
        List<QueryExecutionFactory> decoratees = new ArrayList<QueryExecutionFactory>(sparqlServices.size());
        for (SparqlServiceReference sparqlService : sparqlServices) {
            decoratees.add(new QueryExecutionFactoryHttp(sparqlService.getServiceURL(), sparqlService.getDefaultGraphURIs()));
        }
        return new FluentQueryExecutionFactory<Object>(new QueryExecutionFactoryFallback(decoratees));
    }



    public FluentQueryExecutionFactoryFn<FluentQueryExecutionFactory<P>> config() {
        final FluentQueryExecutionFactory<P> self = this;

        final FluentQueryExecutionFactoryFn<FluentQueryExecutionFactory<P>> result = new FluentQueryExecutionFactoryFn<FluentQueryExecutionFactory<P>>();
        result.setParentSupplier(new Supplier<FluentQueryExecutionFactory<P>>() {
                @Override
                public FluentQueryExecutionFactory<P> get() {
                    // Apply the collection transformations
                    Function<QueryExecutionFactory, QueryExecutionFactory> transform = result.value();
                    fn = transform.apply(fn);

                    return self;
                }
            });

        return result;
    }



/*

    public FluentQueryExecutionFactory<T> withDelay(long delayDuration, TimeUnit delayTimeUnit){
        qef = new QueryExecutionFactoryDelay(qef, delayDuration, delayTimeUnit);
        return this;
    }

    public FluentQueryExecutionFactory<T> withPagination(long pageSize){
        qef = new QueryExecutionFactoryPaginated(qef, pageSize);
        return this;
    }

    public FluentQueryExecutionFactory<T> withRetry(int retryCount, long retryDelayDuration, TimeUnit retryDelayTimeUnit){
        qef = new QueryExecutionFactoryRetry(qef, retryCount, retryDelayDuration, retryDelayTimeUnit);
        return this;
    }

    public FluentQueryExecutionFactory<T> withCache(CacheFrontend cache){
        qef = new QueryExecutionFactoryCacheEx(qef, cache);
        return this;
    }

    public FluentQueryExecutionFactory<T> withDefaultLimit(long limit, boolean doCloneQuery){
        qef = new QueryExecutionFactoryLimit(qef, doCloneQuery, limit);
        return this;
    }
*/
    /**
     * Return the final query execution factory.
     * @return
     */
//    public QueryExecutionFactory create() {
//        return qef;
//    }
//
//    public T end() {
//        throw new RuntimeException("A call to .end() is invalid here");
//    }



}
