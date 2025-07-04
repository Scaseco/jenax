package org.aksw.jenax.arq.util.prefix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.EltStreamRDF;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.util.PrefixMapping2;

public class PrefixUtils {
    public static  void usedPrefixes(PrefixMapping in, Stream<Node> node, PrefixMapping out) {
        node.forEach(n -> usedPrefixes(n, in, out));
    }

    public static void usedPrefixes(Node node, PrefixMapping in, PrefixMapping out) {
        if(node.isURI()) {
            String iri = node.getURI();
            addPrefix(iri, in, out);
        } else if(node.isLiteral()) {
            String iri = node.getLiteralDatatypeURI();

            // FIXME Ignore the implicit datatypes xsd:string and rdf:langString
            if(iri != null
//                    && !Objects.equals(iri, XSD.xstring.getURI())
//                    && !Objects.equals(iri, RDF.langString.getURI())
                    ) {
                addPrefix(iri, in, out);
            }
        }
    }

    public static void addPrefix(String iri, PrefixMapping in, PrefixMapping out) {
        Entry<String, String> e = findLongestPrefix(in, iri);
        if(e != null) {
            String prefix = e.getKey();
            String uri = e.getValue();
            out.setNsPrefix(prefix, uri);
        }
    }


    /**
     * Finds the longest prefix.
     * Performance depends on the provided prefix mapping type:
     * Lookups on {@link PrefixMapping2} are recursively delegated to the inner local/global prefix mappings.
     * Lookups on {@link PrefixMappingTrie} are optimized - otherwise all entries will be scanned.
     *
     * @param pm
     * @param uri
     * @return
     */
    public static Entry<String, String> findLongestPrefix(PrefixMapping pm, String uri) {
        Entry<String, String> result;
        if (pm == null) {
            result = null;
        } else if (pm instanceof PrefixMappingTrie) {
            PrefixMappingTrie pmt = (PrefixMappingTrie)pm;
            result = pmt.findMapping(uri, true).orElse(null);
        } else if (pm instanceof PrefixMapping2) {
            PrefixMapping2 pm2 = (PrefixMapping2)pm;

            result = findLongestPrefix(pm2.getLocalPrefixMapping(), uri);
            if (result == null) {
                result = findLongestPrefix(pm2.getGlobalPrefixMapping(), uri);
            }
        } else {
            result = findLongestPrefixCore(pm, uri);
        }
        return result;
    }

    /**
     * Linear scan of all prefix mappings to find the longest prefix.
     * null if none found.
     *
     * @param pm
     * @param uri
     * @return
     */
    public static Entry<String, String> findLongestPrefixCore(PrefixMapping pm, String uri) {
        int bestResultLength = -1;
        Entry<String, String> bestResult = null;
        Map<String, String> nsPrefixMap = pm.getNsPrefixMap();
        for (Entry<String, String> e : nsPrefixMap.entrySet()) {
            String ss = e.getValue();
            int l = ss.length();
            if (l > bestResultLength && uri.startsWith(ss) && (l != uri.length())) {
                bestResultLength = l;
                bestResult = e;
            }
        }
        return bestResult;
    }

    public static PrefixMapping usedPrefixes(PrefixMapping pm, Set<Node> nodes) {
        PrefixMapping result = new PrefixMappingImpl();
        Stream<Node> nodeStream = nodes.stream();
        usedPrefixes(pm, nodeStream, result);
        return result;
    }

    /**
     * Convert a prefix mapping to a string
     */
    public static String toString(PrefixMapping prefixMapping, RDFFormat rdfFormat) {
        Model tmp = ModelFactory.createDefaultModel();
        tmp.setNsPrefixes(prefixMapping);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RDFDataMgr.write(baos, tmp, rdfFormat);
        String result = null;
        try {
            result = baos.toString("UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            // Should never happen
            throw new RuntimeException(e);
        }
        return result;
    }

    public static PrefixMapping fromString(String str, Lang lang) {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), lang);
        PrefixMapping result = new PrefixMappingImpl();
        result.setNsPrefixes(model.getNsPrefixMap());
        return result;
    }

    public static PrefixMap readPrefixes(PrefixMap sink, InputStream in, Lang lang) {
        return readPrefixes(sink, in, lang, 1000);
    }

    /**
     * Reads prefixes from the given input stream into the provided sink.
     *
     * @param sink
     * @param in
     * @param lang
     * @param maxNonPrefixEventCount Stop prefix scan upon encountering that many non-prefix parsing events, such as Triples. See {@link EltStreamRDF}.
     * @return If the sink is null then a new prefix map is returned, otherwise the sink is returned.
     */
    public static PrefixMap readPrefixes(PrefixMap sink, InputStream in, Lang lang, long maxNonPrefixEventCount) {
        PrefixMap dst = sink == null
                ? PrefixMapFactory.create()
                : sink;

        try(Stream<EltStreamRDF> stream = AsyncParser.of(in, lang, null)
                // .setPrematureDispatch(elt -> true) // Dispatch each element individually
                .setChunkSize(100)
                .streamElements()) {
            Iterator<EltStreamRDF> it = stream.iterator();
            long nonPrefixEventCount = 0;
            while (it.hasNext() && nonPrefixEventCount < maxNonPrefixEventCount) {
                EltStreamRDF event = it.next();
                if (event.isPrefix()) {
                    String eventPrefix = event.prefix();
                    String eventIri = event.iri();

                    String priorIri = dst.get(eventPrefix);
                    if (priorIri != null && priorIri.equals(eventIri)) {
                        // Repeated prefixes count as non-prefix-events
                        ++nonPrefixEventCount;
                    } else {
                        dst.add(eventPrefix, eventIri);
                        nonPrefixEventCount = 0;
                    }
                } else {
                    ++nonPrefixEventCount;
                }
            }
        }
        return dst;
    }
}
