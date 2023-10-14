package org.aksw.jena_sparql_api.sparql_path.api;

import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import io.reactivex.rxjava3.core.Single;

/**
 * Common interface for approaches that search for paths between two sets of resources.
 * The sets of resources are described using {@link Fragment1} instances.
 *
 * Different approaches usually require different types of data summaries to drive the path search.
 * Therefore a system provides the {@link #computeDataSummary(SparqlQueryConnection)} method
 * which is supposed to create a suitable RDF model for the given data source.
 *
 * Path finder builder instances can then be configured with such a data summary model.
 */
public interface ConceptPathFinderSystem {
    // TODO Add support to specify a custom base IRI
    // XXX Maybe even return a data summary builder
    Single<Model> computeDataSummary(SparqlQueryConnection dataConnection);

    ConceptPathFinderFactory<?> newPathFinderBuilder();

    // We could add capabilities such as whether the implemented approach supports non-simple paths
    // getCapabilities()
}
