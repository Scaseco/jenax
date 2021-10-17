package org.aksw.jena_sparql_api.rx.io.resultset;

import org.aksw.jenax.stmt.resultset.SPARQLResultEx;

public interface SPARQLResultExProcessor
    extends SinkStreaming<SPARQLResultEx>, SPARQLResultExVisitor<Void>
{

}
