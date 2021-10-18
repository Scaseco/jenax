package org.aksw.jenax.web.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.jena.query.QueryException;



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