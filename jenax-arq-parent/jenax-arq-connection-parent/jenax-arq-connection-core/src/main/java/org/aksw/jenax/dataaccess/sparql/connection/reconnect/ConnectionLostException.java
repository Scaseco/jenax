package org.aksw.jenax.dataaccess.sparql.connection.reconnect;

import org.apache.jena.query.QueryException;

public class ConnectionLostException
    extends QueryException
{
    private static final long serialVersionUID = 1L;

    public ConnectionLostException(String msg) {
        super(msg);
    }

    public ConnectionLostException(Throwable cause) {
        super(cause);
    }

    public ConnectionLostException(String message, Throwable cause) {
        super(message, cause);
    }
}
