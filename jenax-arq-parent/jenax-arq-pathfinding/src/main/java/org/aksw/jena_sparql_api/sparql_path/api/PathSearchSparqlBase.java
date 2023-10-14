package org.aksw.jena_sparql_api.sparql_path.api;

import java.util.function.Predicate;

import org.aksw.jena_sparql_api.sparql_path.impl.bidirectional.ConceptPathFinderBidirectionalUtils;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.path.SimplePath;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import io.reactivex.rxjava3.core.Flowable;

public abstract class PathSearchSparqlBase
    extends PathSearchBase<SimplePath>
{
    protected SparqlQueryConnection dataConnection;
    protected Fragment1 sourceConcept;
    protected Fragment1 targetConcept;

    public PathSearchSparqlBase(
            SparqlQueryConnection dataConnection,
            Fragment1 sourceConcept,
            Fragment1 targetConcept) {
        super();
        this.dataConnection = dataConnection;
        this.sourceConcept = sourceConcept;
        this.targetConcept = targetConcept;
    }

    public abstract Flowable<SimplePath> execCore();

    @Override
    public Flowable<SimplePath> exec() {
        Predicate<SimplePath> sparqlPathValidator = ConceptPathFinderBidirectionalUtils
                .createSparqlPathValidator(
                        dataConnection,
                        sourceConcept,
                        targetConcept);

        Flowable<SimplePath> result = execCore();

        if(filter != null) {
            result = filter.apply(result);
        }

        result = result.filter(sparqlPathValidator::test);

        return result;
    };
}
