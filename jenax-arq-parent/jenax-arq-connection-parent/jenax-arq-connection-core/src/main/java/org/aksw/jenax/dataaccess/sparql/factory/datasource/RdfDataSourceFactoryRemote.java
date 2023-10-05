package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.net.URL;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

/**
 * Use a remote sparql endpoint as a RdfDataSource
 *
 * @author raven
 *
 */
public class RdfDataSourceFactoryRemote
    implements RdfDataSourceFactory
{
    @Override
    public RdfDataSource create(Map<String, Object> config) {
        RdfDataSourceSpecBasic spec = RdfDataSourceSpecBasicFromMap.wrap(config);

        String url = Objects.requireNonNull(spec.getLocation(), "Location not set (key = " + RdfDataSourceSpecTerms.LOCATION_KEY + ")");

        UrlUserInfo userInfo = UrlUserInfo.create(url);

        RdfDataSource result = () -> {
            RDFConnectionRemoteBuilder b = RDFConnectionRemote.newBuilder();
            if (userInfo.hasUserInfo()) {
                b = b.destination(userInfo.getUrlWithoutUserInfo());
                HttpClient httpClient = HttpClient.newBuilder()
                        .authenticator(AuthLib.authenticator(userInfo.getUser(), userInfo.getPass()))
                        .build();
                b = b.httpClient(httpClient);
            } else {
                b = b.destination(url);
            }
            return b.build();
        };

        return result;
    }

    public static class UrlUserInfo {
        protected String originalUrl;
        protected Exception exception;
        protected String urlWithoutUserInfo;
        protected String user;
        protected String pass;

        public UrlUserInfo(String originalUrl, Exception exception, String urlWithoutUserInfo, String user, String pass) {
            super();
            this.originalUrl = originalUrl;
            this.exception = exception;
            this.urlWithoutUserInfo = urlWithoutUserInfo;
            this.user = user;
            this.pass = pass;
        }

        public boolean hasUserInfo() {
            return user != null || pass != null;
        }

        public String getOriginalUrl() {
            return originalUrl;
        }


        public Exception getException() {
            return exception;
        }


        public String getUrlWithoutUserInfo() {
            return urlWithoutUserInfo;
        }

        public String getUser() {
            return user;
        }


        public String getPass() {
            return pass;
        }

        public static UrlUserInfo create(String urlStr) {
            String urlWithoutUserInfo = null;
            String user = null;
            String pass = null;
            Exception x = null;
            try {
                URL url = new URL(urlStr);
                String userInfo = url.getUserInfo();
                if (userInfo != null) {
                    urlWithoutUserInfo = urlStr.replaceFirst(Pattern.quote(userInfo + "@"), "");
                    String[] userPass = userInfo.split(":", 2);
                    user = userPass[0];
                    pass = userPass[1];
                }
            } catch (Exception e) {
                urlWithoutUserInfo = urlStr;
                x = e;
            }
            return new UrlUserInfo(urlStr, x, urlWithoutUserInfo, user, pass);
        }
    }

}
