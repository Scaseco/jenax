package org.aksw.jena_sparql_api.core;

public class UsernamePasswordCredentials {
    protected final String userName;
    protected final String password;

    public UsernamePasswordCredentials(String userName, String password) {
        super();
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
