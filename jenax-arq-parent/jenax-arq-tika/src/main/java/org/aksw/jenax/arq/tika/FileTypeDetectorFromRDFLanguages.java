package org.aksw.jenax.arq.tika;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sys.JenaSystem;

/**
 * Simple SPI implementation that delegates content type probing to
 * RDFLanguages.guessContentType
 *
 * @author raven
 *
 */
public class FileTypeDetectorFromRDFLanguages
    extends FileTypeDetector
{
    static {
        // Ensure that Jena plugins are loaded - otherwise we may miss
        // some extensions such as HDT
        JenaSystem.init();
    }

    @Override
    public String probeContentType(Path path) throws IOException {
        String result;

        // TODO Upgrade to probe using RDFDataMgrEx - but it needs to be disentangled from rxjava
        // (by now it can rely only on jena's AsyncParser)
        boolean useLegacyApproach = false;
        if (useLegacyApproach) {
            ContentType contentType = RDFLanguages.guessContentType(path.toString());
            result = contentType == null
                    ? null
                    : contentType.getContentTypeStr();
        } else {
            try (InputStream in = Files.newInputStream(path)) {
                RdfEntityInfo info = RDFDataMgrEx.probeEntityInfo(in, RDFDataMgrEx.DEFAULT_PROBE_LANGS);
                result = info.getContentType();
            }
        }
        return result;
    }

}
