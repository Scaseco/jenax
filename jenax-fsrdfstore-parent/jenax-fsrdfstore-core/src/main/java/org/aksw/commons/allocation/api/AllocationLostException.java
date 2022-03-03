package org.aksw.commons.allocation.api;

public class AllocationLostException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    public AllocationLostException(String message, Throwable cause) {
        super(message, cause);
    }

    public AllocationLostException(Throwable cause) {
        super(cause);
    }
}
