package org.aksw.jena_sparql_api.sparql.ext.url;

import java.util.Map;

public class UrlFetchSpec {
    protected String url;
    protected String method;
    protected Map<String, String> headers;
    protected String body;
    protected Integer connectTimeout;
    protected Integer readTimeout;

    public String getUrl() {
        return url;
    }
    public String getMethod() {
        return method;
    }
    public Map<String, String> getHeaders() {
        return headers;
    }
    public String getBody() {
        return body;
    }
    public Integer getConnectTimeout() {
        return connectTimeout;
    }
    public Integer getReadTimeout() {
        return readTimeout;
    }
    @Override
    public String toString() {
        return "UrlFetchSpec [url=" + url + ", method=" + method + ", headers=" + headers + ", body=" + body
                + ", connectTimeout=" + connectTimeout + ", readTimeout=" + readTimeout + "]";
    }
}