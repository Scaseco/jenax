package org.aksw.jenax.model.foaf.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
public interface FoafPerson
    extends FoafAgent
{
    @Iri(FoafTerms.firstName)
    String getFirstName();
    FoafPerson setFirstName(String firstName);

    @Iri(FoafTerms.lastName)
    String getLastName();
    FoafPerson setLastName(String lastName);

    @Iri(FoafTerms.familyName)
    String getFamilyName();
    FoafPerson setFamilyName(String familyName);
}
