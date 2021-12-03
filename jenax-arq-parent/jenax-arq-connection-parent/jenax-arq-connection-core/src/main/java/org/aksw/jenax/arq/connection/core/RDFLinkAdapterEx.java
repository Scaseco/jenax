package org.aksw.jenax.arq.connection.core;

import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.apache.jena.update.UpdateRequest;

/** Jena 4.3.0-SNAPSHOT did not implement the update methods - this class can be removed once RDFLinkAdapter is fully functional */
public class RDFLinkAdapterEx
    extends RDFLinkAdapter

{
    // Just why are the relevant attributes always private...
    protected RDFConnection conn;

    public RDFLinkAdapterEx(RDFConnection conn) {
        super(conn);
        this.conn = conn;
    }

    @Override
    public void update(UpdateRequest update) { conn.update(update); }

    @Override
    public void update(String update) { conn.update(update); }

    public static RDFLink adapt(RDFConnection conn) {
        if ( conn instanceof RDFConnectionAdapter )
            return ((RDFConnectionAdapter)conn).getLink();

        return new RDFLinkAdapterEx(conn);
    }
}
