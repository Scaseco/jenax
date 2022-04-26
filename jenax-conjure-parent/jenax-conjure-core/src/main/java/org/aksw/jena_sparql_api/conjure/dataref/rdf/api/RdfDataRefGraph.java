package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefGraph;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * A reference to a named graph within the active dataset.
 * Used to disambiguate a DataRefUrl which refers to an external resource
 * (even if the active dataset may have a named graph with that URL).
 *
 * @author raven
 *
 */
@ResourceView
@RdfType("rpif:DataRefGraph")
@HashId
public interface RdfDataRefGraph
    extends DataRefGraph, RdfDataRef
{
    @IriNs("rpif")
    @IriType
    @HashId
    RdfDataRefGraph setGraphIri(String graphIri);

    @Override
    default <T> T acceptRdf(RdfDataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

//	public static DataRefUrl create(String url) {
//		DataRefUrl result = create(ModelFactory.createDefaultModel(), url);
//		return result;
//	}

    public static RdfDataRefGraph create(Model model, String graphIri) {
        RdfDataRefGraph result = model.createResource().as(RdfDataRefGraph.class)
                .setGraphIri(graphIri);
        return result;
    }
}
