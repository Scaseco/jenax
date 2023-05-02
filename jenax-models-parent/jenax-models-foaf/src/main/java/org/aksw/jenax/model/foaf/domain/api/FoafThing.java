package org.aksw.jenax.model.foaf.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface FoafThing
    extends Resource
{
    @Iri(FoafTerms.name)
    String getName();
    FoafThing setName(String name);

    @Iri(FoafTerms.homepage)
    @IriType
    String getHomepage();
    FoafThing setHomepage(String homepage);

    @Iri(FoafTerms.depiction)
    @IriType
    String getDepiction();
    FoafThing setDepiction(String homepage);


    default FoafAgent asFoafAgent() {
        return as(FoafAgent.class);
    }

    default FoafPerson asFoafPerson() {
        return as(FoafPerson.class);
    }

    default FoafOnlineAccount asFoafOnlineAccount() {
        return as(FoafOnlineAccount.class);
    }
}
