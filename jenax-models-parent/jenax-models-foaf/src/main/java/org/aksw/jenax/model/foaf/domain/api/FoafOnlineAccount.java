package org.aksw.jenax.model.foaf.domain.api;

import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface FoafOnlineAccount
    extends FoafThing
{
    @Iri(FoafTerms.accountName)
    String getAccountName();
    FoafOnlineAccount setAccountName(String accountName);

    @Iri(FoafTerms.accountServiceHomepage)
    @IriType
    String getAccountServiceHomepage();
    FoafOnlineAccount setAccountServiceHomepage(String accountServiceHomepage);

    @Iri(FoafTerms.account)
    @Inverse
    FoafAgent getOwner();
    FoafOnlineAccount setOwner(Resource agent);
}
