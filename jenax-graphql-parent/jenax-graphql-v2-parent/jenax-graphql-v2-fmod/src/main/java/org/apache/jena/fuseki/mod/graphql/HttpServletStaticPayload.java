package org.apache.jena.fuseki.mod.graphql;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** A simple servlet for serving a static sequence of bytes with a specified content type. */
public class HttpServletStaticPayload
    extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private String contentType;
    private byte[] payload;

    public HttpServletStaticPayload(String contentType, byte[] payload) {
        super();
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(payload);
        this.contentType = contentType;
        this.payload = payload;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType(contentType);
        resp.getOutputStream().write(payload);
    }
}
