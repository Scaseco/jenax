package org.aksw.jenax.web.provider;

import org.apache.jena.query.QueryException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;



@Provider
public class QueryExceptionProvider extends Throwable implements ExceptionMapper<QueryException>
{
    private static final long serialVersionUID = 1L;

    @Override
    public Response toResponse(QueryException exception)
    {
        String str = exception.getMessage();
        return Response.status(400).entity(str).type("text/plain").build();
    }
}
