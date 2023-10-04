package org.aksw.jenax.web.provider;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.jena.query.QueryException;

public class UnwrapRuntimeExceptionProvider
    // extends Throwable // Why did this ever extend Throwable?
    implements ExceptionMapper<RuntimeException>
{
    @Override
    public Response toResponse(RuntimeException e) {
        Throwable current = e;
        while ((current instanceof RuntimeException || current instanceof ExecutionException) && current.getCause() != null) {
            current = current.getCause();
        }
        if (current instanceof QueryException) {
            return new QueryExceptionProvider().toResponse((QueryException) current);
        }
        return new UncaughtExceptionProvider().toResponse(current);
    }
}
