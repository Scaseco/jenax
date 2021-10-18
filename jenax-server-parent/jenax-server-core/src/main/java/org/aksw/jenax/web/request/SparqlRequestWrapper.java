package org.aksw.jenax.web.request;

import javax.servlet.http.HttpServletRequest;

import org.aksw.jenax.web.util.AuthenticatorUtils;
import org.aksw.jenax.web.util.DatasetDescriptionRequestUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.jena.sparql.core.DatasetDescription;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;

/**
 * Domain wrapper for a {@link HttpServletRequest}. Adds methods
 * to access sparql-relevant attributes.
 *
 * @author raven
 *
 */
public class SparqlRequestWrapper
{
    protected HttpServletRequest req;

    public SparqlRequestWrapper(HttpServletRequest req) {
        super();
        this.req = req;
    }

    public static SparqlRequestWrapper wrap(HttpServletRequest req) {
        return new SparqlRequestWrapper(req);
    }

    public HttpServletRequest getHttpServletRequest() {
        return req;
    }

    /*
     * Important: If no SPARQL service is specified, null is returned. This means,
     * that it is up to the SparqlServiceFactory to - use a default service - reject
     * invalid service requests
     *
     *
     * @return
     */
    public String getServiceUri() {
        String result;
        try {
            result = ServletRequestUtils.getStringParameter(req, "service-uri");
        } catch (ServletRequestBindingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public UsernamePasswordCredentials getCredentials() {
        UsernamePasswordCredentials result = AuthenticatorUtils.parseCredentials(req);
        return result;
    }

    public DatasetDescription getDatasetDescription() {
       DatasetDescription result = DatasetDescriptionRequestUtils.extractDatasetDescriptionAny(req);
       return result;
    }
}