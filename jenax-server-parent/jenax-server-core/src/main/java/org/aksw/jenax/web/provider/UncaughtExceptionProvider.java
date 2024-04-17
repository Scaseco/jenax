package org.aksw.jenax.web.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;



@Provider
public class UncaughtExceptionProvider implements ExceptionMapper<Throwable>
{
    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionProvider.class);

    @Override
    public Response toResponse(Throwable exception)
    {
        String str = Throwables.getStackTraceAsString(exception);
        logger.warn(str);
        return Response.status(500).entity(str).type("text/plain").build();
    }
}
