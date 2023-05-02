package org.aksw.jenax.model.foaf.domain.api;

public class FoafTerms {
    public static final String NS = "http://xmlns.com/foaf/0.1/";

    // Domain owl:Thing
    public static final String homepage = NS + "homepage";
    public static final String name = NS + "name";
    public static final String depiction = NS + "depiction";

    // Domain foaf:Agent
    public static final String gender = NS + "gender";
    public static final String mbox = NS + "mbox";
    public static final String mbox_sha1sum = NS + "mbox_sha1sum";
    public static final String account = NS + "account";

    // Domain foaf:Person
    public static final String familyName = NS + "familyName";
    public static final String firstName = NS + "firstName";
    public static final String lastName = NS + "lastName";

    // Domain foaf:OnlineAccount
    public static final String accountName = NS + "accountName";
    public static final String accountServiceHomepage = NS + "accountServiceHomepage";


}
