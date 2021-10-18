package org.aksw.jenax.web.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.jena.ext.com.google.common.base.Throwables;



@Provider
public class UncaughtExceptionProvider extends Throwable implements ExceptionMapper<Throwable>
{
    private static final long serialVersionUID = 1L;

    @Override
    public Response toResponse(Throwable exception)
    {
        String str = Throwables.getStackTraceAsString(exception);
        return Response.status(500).entity(str).type("text/plain").build();
    }
}