package org.aksw.jenax.arq.connection.core;

import org.apache.jena.query.QueryException;

public class ConnectionReestablishedException
    extends QueryException
{
    private static final long serialVersionUID = 1L;

    public ConnectionReestablishedException(String message, Throwable cause) {
        super(message, cause);
    }
}
