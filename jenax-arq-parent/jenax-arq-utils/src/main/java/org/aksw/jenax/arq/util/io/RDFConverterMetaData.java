package org.aksw.jenax.arq.util.io;

import java.util.Collection;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

/** Interface for metadata about Jena-based RDF conversion. */
public interface RDFConverterMetaData {
    Collection<Lang> getSupportedInputLangs();
    Collection<RDFFormat> getSupportedOutputFormats();

    // XXX Use Optional<RDFFormat> to cater for absent default formats - or always return the PLAIN variant as a default?
    RDFFormat getDefaultOutputFormat(Lang outLang);

    public default boolean supportsConversion(Lang inLang, RDFFormat outFormat) {
        boolean isInLangSupported = getSupportedInputLangs().contains(inLang);
        boolean isOutLangSupported = getSupportedOutputFormats().contains(outFormat);
        boolean result = isInLangSupported && isOutLangSupported;
        return result;
    }
}
