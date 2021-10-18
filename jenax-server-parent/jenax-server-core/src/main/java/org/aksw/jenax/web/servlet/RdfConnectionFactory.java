package org.aksw.jenax.web.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RdfConnectionFactory {
    RDFConnection getConnection(HttpServletRequest request);
}
