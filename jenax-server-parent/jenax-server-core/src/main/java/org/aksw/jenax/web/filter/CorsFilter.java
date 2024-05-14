package org.aksw.jenax.web.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class CorsFilter
    implements Filter
{
    private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;

        if(req.getHeader("Origin") != null){
            res.addHeader("Access-Control-Allow-Origin", "*");
            res.addHeader("Access-Control-Expose-Headers", "X-Cache-Date, X-Atmosphere-tracking-id");
        }

        if("OPTIONS".equals(req.getMethod())){
            res.addHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST");
            res.addHeader("Access-Control-Allow-Headers", "Origin, Content-Type, X-Atmosphere-Framework, X-Cache-Date, X-Atmosphere-tracking-id, X-Atmosphere-Transport");
            res.addHeader("Access-Control-Max-Age", "-1");
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() { }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

        logger.info("CORS filter initialized");

    }
}
