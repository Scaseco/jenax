package org.aksw.jena_sparql_api.sparql_path.core.algorithm;

import java.util.List;

import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderFactorySummaryBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearchSparqlBase;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactories;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.path.SimplePath;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ConceptPathFinderSystemBasic
    implements ConceptPathFinderSystem
{
    @Override
    public Single<Model> computeDataSummary(SparqlQueryConnection dataConnection) {
        return Single.fromCallable(() -> org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder.createDefaultJoinSummaryModel(
                QueryExecutionFactories.of(dataConnection)));
    }

    @Override
    public ConceptPathFinderFactoryBidirectional<?> newPathFinderBuilder() {
        return new ConceptPathFinderFactoryBidirectional<>();
    }

    public static class ConceptPathFinderFactoryBidirectional<T extends ConceptPathFinderFactoryBidirectional<T>>
        extends ConceptPathFinderFactorySummaryBase<T>
    {
        // NOTE We could add more specific attributes here if we wanted
        @Override
        public ConceptPathFinder build() {
            return new ConceptPathFinderBase(dataSummary.getGraph(), dataConnection) {
                @Override
                public PathSearch<SimplePath> createSearch(Fragment1 sourceConcept, Fragment1 targetConcept) {
                    return new PathSearchSparqlBase(dataConnection, sourceConcept, targetConcept) {
                        @Override
                        public Flowable<SimplePath> execCore() {
                            List<SimplePath> flow = org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder
                            .findPaths(QueryExecutionFactories.of(dataConnection), sourceConcept, targetConcept,
                                    maxLength, maxResults);
                            return Flowable.fromIterable(flow);
                        }
                    };
                }
            };
        }
    }
}
