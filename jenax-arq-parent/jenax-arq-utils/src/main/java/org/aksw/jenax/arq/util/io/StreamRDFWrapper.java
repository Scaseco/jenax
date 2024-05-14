package org.aksw.jenax.arq.util.io;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;

/**
 * A wrapper around another {@link StreamRDF}. Differs from Jena's {@link StreamRDFWriter} that its an interface and
 * all methods use delegate to getDelegate().
 *
 */
public interface StreamRDFWrapper extends StreamRDF
{
    StreamRDF getDelegate();

    @Override
    default void start()
    { getDelegate().start() ; }

    @Override
    default void triple(Triple triple)
    { getDelegate().triple(triple) ; }

    @Override
    default void quad(Quad quad)
    { getDelegate().quad(quad) ; }

    @Override
    default void base(String base)
    { getDelegate().base(base) ; }

    @Override
    default void prefix(String prefix, String iri)
    { getDelegate().prefix(prefix, iri) ; }

    @Override
    default void finish()
    { getDelegate().finish() ; }
}
