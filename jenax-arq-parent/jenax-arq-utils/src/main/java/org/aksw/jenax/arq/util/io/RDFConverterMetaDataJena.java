package org.aksw.jenax.arq.util.io;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.system.StreamRDFWriter;

/** Singleton implementation of RDFConverterMetaData based on Jena's registries. */
public class RDFConverterMetaDataJena
    implements RDFConverterMetaData
{
    private static final RDFConverterMetaDataJena INSTANCE = new RDFConverterMetaDataJena();

    public static RDFConverterMetaData get() {
        return INSTANCE;
    }

    /** Singleton. Use .get() to obtain the instance. */
    private RDFConverterMetaDataJena() {}

    /**
     * Returns a fresh snapshot of the currently supported input langs registered with the Jena system.
     */
    @Override
    public Collection<Lang> getSupportedInputLangs() {
        List<Lang> result = Stream.concat(
            RDFParserRegistry.registeredLangTriples().stream(),
            RDFParserRegistry.registeredLangQuads().stream()
        ).toList();
        return result;
    }

    @Override
    public Collection<RDFFormat> getSupportedOutputFormats() {
        return List.copyOf(StreamRDFWriter.registered());
    }

    @Override
    public RDFFormat getDefaultOutputFormat(Lang outLang) {
        RDFFormat result = StreamRDFWriter.defaultSerialization(outLang);
        return result;
    }

    @Override
    public boolean supportsConversion(Lang inLang, RDFFormat outFormat) {
        boolean isInLangSupported = RDFLanguages.isRegistered(inLang);
        boolean isOutLangSupported = StreamRDFWriter.registered(outFormat);
        boolean result = isInLangSupported && isOutLangSupported;
        return result;
    }
}
