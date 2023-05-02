package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefUrl;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
// @RdfTypeNs("rpif")
@RdfType("http://www.w3.org/ns/dcat#Distribution")
@HashId
public interface RdfDataRefUrl
    extends DataRefUrl, RdfDataRef
{
    @Iri("http://www.w3.org/ns/dcat#downloadURL")
    @IriType
    @HashId
    RdfDataRefUrl setDataRefUrl(String url);

    // Experimental feature adding hdt header as a first class modifier for a dataset reference
    @IriNs("rpif")
    RdfDataRefUrl hdtHeader(Boolean value);

    @Override
    default <T> T acceptRdf(RdfDataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

//	public static DataRefUrl create(String url) {
//		DataRefUrl result = create(ModelFactory.createDefaultModel(), url);
//		return result;
//	}

    public static RdfDataRefUrl create(Model model, String url) {
        RdfDataRefUrl result = model.createResource().as(RdfDataRefUrl.class)
                .setDataRefUrl(url);
        return result;
    }
}
