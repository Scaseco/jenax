package org.aksw.jenax.arq.util.fmt;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

/** Interface that bundles result langs and formats for the different query types. */
public interface SparqlResultFmts {

    @SuppressWarnings("unchecked")
    default <T> T get(SparqlResultType slot) {
        T result;
        switch (slot) {
            case Bindings:
                result = (T)forBindings();
                break;
            case AskResult:
                result = (T)forAskResult();
                break;
            case Quads:
                result = (T)forQuads();
                break;
            case Triples:
                result = (T)forTriples();
                break;
            case Unknown:
                result = (T)forUnknown();
                break;
            default:
                result = null;
        }
        return result;
    }

    Lang forAskResult();
    Lang forBindings();
    RDFFormat forTriples();
    RDFFormat forQuads();
    Lang forUnknown();
}
