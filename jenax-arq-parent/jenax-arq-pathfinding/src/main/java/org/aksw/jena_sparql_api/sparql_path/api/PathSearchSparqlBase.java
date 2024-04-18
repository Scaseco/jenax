package org.aksw.jena_sparql_api.sparql_path.api;

import java.util.function.Predicate;

import org.aksw.jena_sparql_api.sparql_path.impl.bidirectional.ConceptPathFinderBidirectionalUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.path.SimplePath;

import io.reactivex.rxjava3.core.Flowable;

public abstract class PathSearchSparqlBase
    extends PathSearchBase<SimplePath>
{
    protected RdfDataSource dataSource;
    protected Fragment1 sourceConcept;
    protected Fragment1 targetConcept;

    public PathSearchSparqlBase(
            RdfDataSource dataSource,
            Fragment1 sourceConcept,
            Fragment1 targetConcept) {
        super();
        this.dataSource = dataSource;
        this.sourceConcept = sourceConcept;
        this.targetConcept = targetConcept;
    }

    public abstract Flowable<SimplePath> execCore();

    @Override
    public Flowable<SimplePath> exec() {
        Predicate<SimplePath> sparqlPathValidator = ConceptPathFinderBidirectionalUtils
                .createSparqlPathValidator(
                        dataSource,
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
