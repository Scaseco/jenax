package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import java.util.List;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefSparqlEndpoint;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfType("rpif:DataRefSparqlEndpoint")
public interface RdfDataRefSparqlEndpoint
    extends DataRefSparqlEndpoint, RdfDataRef
{
    @HashId
    @Iri("rpif:serviceUrl")
    @IriType
    RdfDataRefSparqlEndpoint setServiceUrl(String serviceUrl);

    @HashId
    @Override
    @Iri("rpif:namedGraph")
    @IriType
    List<String> getNamedGraphs();

    @HashId
    @Override
    @Iri("rpif:defaultGraph")
    @IriType
    List<String> getDefaultGraphs();

    @Override
    default <T> T acceptRdf(RdfDataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }


    default RdfDataRefSparqlEndpoint mutateDefaultGraphs(Consumer<? super List<? super String>> defaultGraphsMutator) {
        defaultGraphsMutator.accept(getDefaultGraphs());
        return this;
    }

    default RdfDataRefSparqlEndpoint mutateNamedGraphs(Consumer<? super List<? super String>> namedGraphsMutator) {
        namedGraphsMutator.accept(getNamedGraphs());
        return this;
    }

//	public static DataRefSparqlEndpoint create(String serviceUrl) {
//		DataRefSparqlEndpoint result = ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class)
//				.setServiceUrl(serviceUrl);
//		return result;
//
//	}

    public static RdfDataRefSparqlEndpoint create(Model model, String serviceUrl) {
        RdfDataRefSparqlEndpoint result = model.createResource().as(RdfDataRefSparqlEndpoint.class)
                .setServiceUrl(serviceUrl);
        return result;

    }
}
