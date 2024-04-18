package org.aksw.jenax.web.servlet;

import org.apache.jena.rdfconnection.RDFConnection;

import jakarta.servlet.http.HttpServletRequest;

public interface RdfConnectionFactory {
    RDFConnection getConnection(HttpServletRequest request);
}
