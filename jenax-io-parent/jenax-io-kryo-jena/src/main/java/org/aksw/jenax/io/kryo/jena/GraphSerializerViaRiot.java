package org.aksw.jenax.io.kryo.jena;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.GraphFactory;

/**
 * Riot-based serializer for Models.
 *
 * @author Claus Stadler
 */
public class GraphSerializerViaRiot
        extends RiotSerializerBase<Graph> {

    public GraphSerializerViaRiot(Lang lang, RDFFormat format) {
        super(lang, format);
    }

    @Override
    protected void writeActual(Graph graph, OutputStream out) {
        RDFDataMgr.write(out, graph, format);
    }

    @Override
    protected Graph readActual(InputStream in) {
        Graph result = GraphFactory.createDefaultGraph();
        RDFDataMgr.read(result, in, lang);
        return result;
    }
}
