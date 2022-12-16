package org.aksw.jena_sparql_api.sparql.ext.url;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.aksw.commons.beans.model.PropertyUtils;
import org.aksw.commons.util.gson.GsonUtils;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaJsonUtils;
import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * url:fetch(?url, [option1 [, ... [, optionN]]])
 *
 * If the first argument is a string then it is considered to be the URL.
 *
 * If the next option to process
 *   - is a string then it is interpreted as a json key with the following option its value.
 *   - is a json object then it is merged into the current one
 *
 *
 *
 * @author raven
 *
 */
public class E_UrlFetch
    extends FunctionBase
{
    private static final Logger logger = LoggerFactory.getLogger(E_UrlFetch.class);

    public static final Pattern jsonContentTypePattern = Pattern.compile("^application/(.+\\+)?json$");

    @Override
    public NodeValue exec(List<NodeValue> args) {
        JsonObject obj = assemble(args);
        UrlFetchSpec conf = RDFDatatypeJson.get().getGson().fromJson(obj, UrlFetchSpec.class);
        NodeValue result;

        try {
            URLConnection conn = configure(conf);

            String body = conf.getBody();
            if (body != null) {
                conn.setDoOutput(true);
                try (Writer out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
                    // OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    out.write(body);
                    out.flush();
                }
            }

            String contentType = conn.getContentType();

            try (InputStream in = conn.getInputStream()) {
                if (in != null) {
                    String str = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
                    if (jsonContentTypePattern.matcher(contentType).find()) {
                        result = JenaJsonUtils.fromString(str);
                    } else {
                        result = NodeValue.makeString(str);
                    }
                } else {
                    throw new ExprEvalException("Failed to execute request");
                }
            }
        } catch (Exception e) {
            logger.warn("Exception during evaluation of url:fetch with effective spec " + obj, e);
            throw new ExprEvalException(e);
        }
        return result;
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
    }

    public static JsonObject assemble(List<NodeValue> args) {
        int n = args.size();
        JsonObject result = new JsonObject();
        for (int i = 0; i < n; ++i) {
            NodeValue k = args.get(i);
            NodeValue v = i + 1 < n ? args.get(i + 1) : null;

            if (k.isString()) {
                if (i == 0) {
                    // Url
                    result.addProperty("url", k.asString());
                } else {
                    String keyStr = k.getString();
                    List<String> key = GsonUtils.parsePathSegments(keyStr);
                    expandShortcuts(key);

                    if (v != null) {
                        JsonElement elt = JenaJsonUtils.enforceJsonElement(v);
                        GsonUtils.setElement(result, key, elt);
                    }
                    ++i;
                }
            } else if (JenaJsonUtils.isJsonElement(k)) {
                JsonElement elt = JenaJsonUtils.extractJsonElement(k);
                GsonUtils.merge(result, elt);
            } else {
                throw new ExprTypeException("Either JSON or string type expected for argument " + i);
            }
        }

        return result;
    }

    public static void expandShortcuts(List<String> key) {
        int n = key.size();
        if (n >= 1) {
            String first = key.get(0);
            if (n > 1) {
                if (first.equals("h")) {
                    key.set(0, "headers");
                }
            } else if (n == 1) {
                if (first.equals("m")) {
                    key.set(0, "method");
                } else if (first.equals("b")) {
                    key.set(0, "body");
                } else if (first.equals("cto")) {
                    key.set(0, "connectTimeout");
                } else if (first.equals("rto")) {
                    key.set(0, "readTimeout");
                }
            }
        }
    }

    public static URLConnection configure(UrlFetchSpec conf) throws IOException {
        String urlStr = conf.getUrl();
        URL url = new URL(urlStr);
        URLConnection result = url.openConnection();
        configure(result, conf);
        return result;
    }

    public static void configure(URLConnection conn, UrlFetchSpec conf) {
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection c = (HttpURLConnection)conn;

            PropertyUtils.applyIfPresent(c::setRequestMethod, conf::getMethod);
            PropertyUtils.applyIfPresent(c::setConnectTimeout, conf::getConnectTimeout);
            PropertyUtils.applyIfPresent(c::setReadTimeout, conf::getReadTimeout);

            for (Entry<String, String> e : conf.getHeaders().entrySet()) {
                c.setRequestProperty(e.getKey(), e.getValue());
            }
        }
    }
}
