package org.aksw.jenax.stmt.resultset;

public interface SPARQLResultSink
    extends SPARQLResultVisitor, AutoCloseable
{
    void flush() ;
}
