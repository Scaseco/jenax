package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
@RdfType("rpif:RdfAuthBearerToken")
public interface RdfAuthBearerToken
    extends RdfAuth
{
    @Iri("rpif:bearerToken")
    String getBearerToken();
    RdfAuthBearerToken setBearerToken(String bearerToken);
}
