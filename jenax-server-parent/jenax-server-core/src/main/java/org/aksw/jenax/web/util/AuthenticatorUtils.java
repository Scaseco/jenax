package org.aksw.jenax.web.util;

// Broke with Jena5 - code kept for reference for the time being

//public class AuthenticatorUtils {
//    /**
//     * Create a http client with username / password authentication.
//     * If the argument is null, an http client without authentication is returned.
//     *
//     * @param credentials
//     * @return
//     */
//    public static HttpClientBuilder prepareHttpClientBuilder(UsernamePasswordCredentials credentials) {
//        HttpClientBuilder result = HttpClientBuilder.create();
//        if(credentials != null) {
//            CredentialsProvider provider = new BasicCredentialsProvider();
//            provider.setCredentials(AuthScope.ANY, credentials);
//            result.setDefaultCredentialsProvider(provider);
//        }
//        return result;
//    }
//
//    public static UsernamePasswordCredentials parseCredentials(HttpServletRequest req) {
//        UsernamePasswordCredentials result = null;
//        /*
//        Enumeration<String> e = req.getHeaderNames();
//        while(e.hasMoreElements()) {
//            String name = e.nextElement();
//            System.out.println(name + ": " + req.getHeader(name));
//        }*/
//
//        String authStr = ObjectUtils.firstNonNull(
//                req.getHeader("Authorization"),
//                req.getHeader("WWW-Authenticate"));
//
//        if(authStr != null && authStr.startsWith("Basic")) {
//            // authStr: Basic base64credentials
//            String base64Credentials = authStr.substring("Basic".length()).trim();
//
//            // credentials = username:password
//            String credentials = new String(DatatypeConverter.parseBase64Binary(base64Credentials));
//            final String[] values = credentials.split(":", 2);
//
//            if(values.length == 2) {
//                String username = values[0];
//                String password = values[1];
//                //char[] password = values[1].toCharArray();
//
//                result = new UsernamePasswordCredentials(username, password);
//            } else {
//                throw new RuntimeException("Invalid header - got: " + Arrays.asList(values));
//            }
//        }
//
//        return result;
//    }
//}
