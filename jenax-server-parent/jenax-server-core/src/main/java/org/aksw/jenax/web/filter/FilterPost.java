package org.aksw.jenax.web.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * Source: https://github.com/Atmosphere/atmosphere/wiki/Enabling-CORS
 *
 */
@WebFilter
public class FilterPost
    implements Filter
{
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;

        // This call causes the post data to become available via subsequent calls
        // to getParameterMap()

        // If we don't do it here, jersey will consume the data and we won't be able to access it here anymore
        if(req.getMethod().equals("POST")) {
            req.getParameterMap();
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() { }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
