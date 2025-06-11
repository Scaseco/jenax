package org.aksw.jenax.fuseki.mod.graphql;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.apache.jena.atlas.web.TypedInputStream;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** A simple servlet for serving a static sequence of bytes with a specified content type. */
public class HttpServletOverResource
    extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private ResourceSource resource;

    public HttpServletOverResource(ResourceSource resource) {
        super();
        Objects.requireNonNull(resource);
        this.resource = resource;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try (TypedInputStream tin = resource.open()) {
            String contentType = tin.getContentType();
            if (contentType != null) {
                resp.setContentType(contentType);
            }

            // Close of out handled by the server framework.
            OutputStream out = resp.getOutputStream();
            tin.transferTo(out);
        }
    }

    public static HttpServletOverResource of(ResourceSource resource) {
        return new HttpServletOverResource(resource);
    }

    public static HttpServletOverResource of(String contentType, byte[] payload) {
        return of(new ResourceSourceStatic(contentType, payload));
    }

    public static HttpServletOverResource ofNamedResource(String contentType, String resourceName) {
        return of(new ResourceSourceFromStreamManager(contentType, resourceName));
    }
}
