package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
@HashId
public interface DataRefUrl
    extends PlainDataRefUrl, DataRef
{
    @IriNs("rpif")
    @IriType
    @HashId
    DataRefUrl setDataRefUrl(String url);

    // Experimental feature adding hdt header as a first class modifier for a dataset reference
    @IriNs("rpif")
    DataRefUrl hdtHeader(Boolean value);

    @Override
    default <T> T accept2(DataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

//	public static DataRefUrl create(String url) {
//		DataRefUrl result = create(ModelFactory.createDefaultModel(), url);
//		return result;
//	}

    public static DataRefUrl create(Model model, String url) {
        DataRefUrl result = model.createResource().as(DataRefUrl.class)
                .setDataRefUrl(url);
        return result;
    }
}
