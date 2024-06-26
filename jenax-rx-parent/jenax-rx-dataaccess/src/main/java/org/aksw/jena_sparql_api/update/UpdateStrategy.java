package org.aksw.jena_sparql_api.update;

import java.util.function.Function;

import org.aksw.jenax.connectionless.SparqlService;
import org.aksw.jenax.dataaccess.sparql.factory.execution.update.UpdateExecutionFactory;

/**
 * An update strategy is a function that yields an UpdateExecutionFactory
 * for a given sparqlService.
 *
 *
 * @author raven
 *
 */
public interface UpdateStrategy<T extends UpdateExecutionFactory>
    extends Function<SparqlService, T>
{
    //public void createFor(SparqlService sparqlService);
}
