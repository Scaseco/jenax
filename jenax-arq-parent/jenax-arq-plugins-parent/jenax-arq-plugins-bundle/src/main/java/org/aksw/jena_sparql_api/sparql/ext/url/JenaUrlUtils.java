package org.aksw.jena_sparql_api.sparql.ext.url;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.irix.IRIx;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.irix.IRIxResolver.Builder;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

public class JenaUrlUtils {
	private static final Logger logger = LoggerFactory.getLogger(JenaUrlUtils.class);

	/** Base IRI to use to form URLs for resolving content */
	public static final Symbol symContentBaseIriX = SystemARQ.allocSymbol("contentBaseIriX");


	public static Prologue getCurrentPrologue(Context cxt) {
		Prologue result = cxt.get(ARQConstants.sysCurrentQuery);
		return result;
	}

	public static IRIx getContextBaseIriX(Context cxt) {
		IRIx result = cxt.get(symContentBaseIriX);

		if (result == null) {
			result = Optional.ofNullable(getCurrentPrologue(cxt)).map(Prologue::getBase).orElse(null);
		}

		return result;
	}

	public static IRIx extractBaseIriX(FunctionEnv env) {
		IRIx result = Optional.ofNullable(env)
			.map(FunctionEnv::getContext)
			.map(JenaUrlUtils::getContextBaseIriX)
			.orElse(null);
		return result;
	}

	public static IRIx createIriX(NodeValue nv, FunctionEnv env) {
		IRIx result = null;

        String url;
        if(nv.isString()) {
            url = nv.getString();
        } else if(nv.isIRI()) {
            Node node = nv.asNode();
            url = node.getURI();
        } else {
            url = null;
        }

        if(url != null) {
            Builder builder = IRIxResolver.create().allowRelative(true);

            IRIx base = extractBaseIriX(env);
            // if (base != null) {
            	builder.base(base);
            // }

            IRIxResolver resolver = builder.build();
            result = resolver.resolve(url);
        }

        return result;
	}

	// public static final Pattern URI_SCHEME_PATTERN = Pattern.compile("^\\p{Alpha}(\\p{Alnum}|+|-|\\.)*");

    public static InputStream openInputStream(NodeValue nv, FunctionEnv env) throws Exception {
    	InputStream result = null;
    	IRIx irix = createIriX(nv, env);
    	if (irix != null) {
    		String urlStr = irix.str();

        	URI uri = new URI(urlStr);

        	// If there is no scheme then default to file://
        	String scheme = uri.getScheme();
        	if (scheme == null) {
        		// If the urlStr after resolution against the function env is still a
        		// relative IRI then resolve it against the current working directory
        		if (!urlStr.startsWith("/")) {
        			Path cwd = Path.of("");
        			uri = cwd.resolve(urlStr).toUri();
        		} else {
        			String fileUrl = "file://" + urlStr;
        			uri = new URI(fileUrl);
        		}
        	}

        	URL u;
        	try {
        		u = uri.toURL();
        	} catch (Exception e) {
        		String msg = "Failed to create URL from " + uri;
        		logger.warn(msg, e);
        		// throw new RuntimeException("Failed to create URL from " + uri, e);
        		throw new RuntimeException(msg, e);
        		// throw new ExprEvalException(msg);
        	}
        	URLConnection conn = u.openConnection();
        	// String contentType = conn.getContentType();

        	// TODO Add support for content types, e.g. parsing json

        	result = conn.getInputStream();
        }

        return result;
    }

    public static NodeValue resolve(NodeValue nv, FunctionEnv env) throws Exception {
    	NodeValue result;
    	try (InputStream in = JenaUrlUtils.openInputStream(nv, env)) {
	    	if (in != null) {
	    		String str = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));

	    		result = NodeValue.makeString(str);
	    	} else {
	        	throw new ExprEvalException("Failed to obtain text from node " + nv);
	        }
    	}

        return result;
    }

}
