package org.aksw.jenax.web.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aksw.jenax.arq.util.fmt.SparqlQueryFmtOverResultFmt;
import org.aksw.jenax.arq.util.fmt.SparqlQueryFmts;
import org.aksw.jenax.arq.util.fmt.SparqlQueryFmtsUtils;
import org.aksw.jenax.arq.util.fmt.SparqlResultFmtsImpl;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.WebContent;


/** Adjust wildcard accept header with a specific one based on the sparql query / update type */
@WebFilter
public class SparqlStmtTypeAcceptHeaderFilter
    implements Filter
{
    /** The parser defaults to jena's arq parser */
    protected SparqlStmtParser sparqlStmtParser;

    public SparqlStmtTypeAcceptHeaderFilter(SparqlStmtParser sparqlStmtParser) {
		super();
		this.sparqlStmtParser = sparqlStmtParser;
	}

//    public SparqlStmtParser getSparqlStmtParser() {
//    	return sparqlStmtParser == null ? SparqlEndpointBase;
//    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;

        String payload = null;

        // This call causes the post data to become available via subsequent calls
        // to getParameterMap()

        // If we don't do it here, jersey will consume the data and we won't be able to access it here anymore
        Enumeration<String> it = req.getHeaders("Accept");
        boolean isWildcard = true;
        while (it.hasMoreElements()) {
        	String item = it.nextElement();
        	if (!item.equals("*/*")) {
        		isWildcard = false;
        	}
        }

        // HttpServletRequest subReq = req;
        if (isWildcard) {
        	List<String> strs = new ArrayList<>();

        	String contentTypeStr = req.getContentType();
        	boolean isSparqlPayload = WebContent.contentTypeSPARQLQuery.equals(contentTypeStr) || WebContent.contentTypeSPARQLUpdate.equals(contentTypeStr);
        	if (isSparqlPayload) {
        		// req = new ContentCachingRequestWrapper(req);
        		try (InputStream in = req.getInputStream()) {
        			payload = IOUtils.toString(in, StandardCharsets.UTF_8);
        		}
            	strs.add(payload);
            } else {
	        	Map<String, String[]> params = req.getParameterMap();
	        	strs.addAll(Optional.ofNullable(params.get("query")).map(Arrays::asList).orElse(Collections.emptyList()));
	        	strs.addAll(Optional.ofNullable(params.get("update")).map(Arrays::asList).orElse(Collections.emptyList()));
            }

        	if (strs.size() == 1) {
        		String str = strs.iterator().next();

        		SparqlStmt stmt = sparqlStmtParser.apply(str);
        		if (stmt.isParsed()) {
        			if (stmt.isQuery()) {

        				SparqlQueryFmts fmts = new SparqlQueryFmtOverResultFmt(SparqlResultFmtsImpl.DEFAULT);
        				Lang lang = SparqlQueryFmtsUtils.getLang(fmts, stmt.getQuery());

        				String acceptTypeStr = lang.getContentType().getContentTypeStr();

        				HeaderMapRequestWrapper tmp = new HeaderMapRequestWrapper(req, true);
        				tmp.addHeader("Accept", acceptTypeStr);
        				req = tmp;
        			}
        		}
        	}
        }

        // If the payload has been read then re-set it via a wrapper
		if (payload != null) {
			byte[] bytes = payload.getBytes();
			req = new RepeatablePayloadReadWrapper(req, StandardCharsets.UTF_8, () -> new ByteArrayInputStream(bytes));
		}


        chain.doFilter(req, res);
    }

    @Override
    public void destroy() { }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}