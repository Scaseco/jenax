package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import org.aksw.jenax.arq.service.vfs.ServiceExecutorFactoryVfsUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class E_ProbeRdf
    extends FunctionBase1
{
    // File is considered RDF if it is non-empty and at least 'n' triples could be parsed
    protected int n = 100;

    // Because of the way some parser in jena works, it seams reasonable to limit the input used for
    // probing. Note that this may fail if an RDF file contains e.g. a large polygon WKT string
    // as its first triple.
    // protected int probeBytes = 4096;

    @Override
    public NodeValue exec(NodeValue nv) {
        NodeValue result = NodeValue.FALSE;
        try {
            if(nv.isIRI()) {
                Node node = nv.asNode();
                String iri = node.getURI();

                Lang lang = RDFDataMgr.determineLang(iri, null, null);
                if(lang != null) {
                    Path path = ServiceExecutorFactoryVfsUtils.toPath(node);

                    // try(InputStream in = new BoundedInputStream(Files.newInputStream(path), probeBytes)) {
                    try (Stream<?> stream = streamQuads(Files.newInputStream(path), null, iri)) {
                        Iterator<?> it = stream.iterator();
                        int i;
                        for(i = 0; i < n && it.hasNext(); ++i) {
                            it.next();
                        }

                        if(i > 0) {
                            result = NodeValue.TRUE;
                        }
                    }
                }
            }
        } catch(Exception e) {
            //throw new ExprEvalException(e);
            result = NodeValue.FALSE;
        }

        return result;
    }

    // Machinery copied from jena in order to uniformly parse triples and quads

    public static Stream<Quad> streamQuads(InputStream input, Lang lang, String baseIRI) {
        return AsyncParser.of(input, lang, baseIRI).setChunkSize(10).streamQuads()
                .map(q -> q.getGraph() != null ? q : Quad.create(Quad.defaultGraphNodeGenerated, q.asTriple()));

//        // Special case N-Triples, because the RIOT reader has a pull interface
//        // Special case N-Quads, because the RIOT reader has a pull interface
//
//
//        if ( RDFLanguages.sameLang(RDFLanguages.NTRIPLES, lang) || RDFLanguages.sameLang(RDFLanguages.NQUADS, lang)) {
//            return Iter.onCloseIO(
//                    RiotParsers.createIteratorNQuads(input, null, RiotLib.dftProfile()),
//                    input);
//        }
//
//        // Otherwise, we have to spin up a thread to deal with it
//        PipedRDFIterator<Quad> it = new PipedRDFIterator<>();
//        PipedQuadsStream out = new PipedQuadsStream(it) {
//            @Override
//            public void triple(Triple triple) {
//                quad(new Quad(Quad.defaultGraphNodeGenerated, triple));
//            }
//        };
//
//        Thread t = new Thread(()->parseFromInputStream(out, input, baseIRI, lang, null)) ;
//        t.start();
//        return it;
    }

//    public static void parseFromInputStream(StreamRDF destination, InputStream in, String baseUri, Lang lang, Context context) {
//        RDFParser.create()
//            .source(in)
//            .base(baseUri)
//            .lang(lang)
//            .context(context)
//            .parse(destination);
//    }

}
