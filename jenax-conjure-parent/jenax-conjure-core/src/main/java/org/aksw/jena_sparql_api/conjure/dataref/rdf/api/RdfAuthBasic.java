package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
@RdfType("rpif:RdfAuthBasic")
public interface RdfAuthBasic
    extends RdfAuth
{
    @Iri("rpif:username")
    String getUsername();
    RdfAuthBasic setUsername(String username);

    @Iri("rpif:password")
    String getPassword();
    RdfAuthBasic setPassword(String password);
}
