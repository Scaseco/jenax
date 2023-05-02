package org.aksw.jenax.model.foaf.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
public interface FoafAgent
    extends FoafThing
{
    // gender yahooChatID account birthday icqChatID aimChatID jabberID made mbox interest tipjar skypeID topic_interest age mbox_sha1sum status msnChatID openid holdsAccount weblog

    @Iri(FoafTerms.mbox)
    String getMbox();
    FoafAgent setMbox(String mbox);

    @Iri(FoafTerms.mbox_sha1sum)
    String getMboxSha1sum();
    FoafAgent getMoxSha1sum();

    // Actual range: rdf:Literal
    @Iri(FoafTerms.gender)
    String getGender();
    FoafAgent setGender(String gender);

    @Iri(FoafTerms.account)
    Set<FoafOnlineAccount> getAccounts();
}
