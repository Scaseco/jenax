package org.aksw.jenax.shellgebra.cmd;

import java.util.List;

import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.aksw.shellgebra.shim.core.ArgsBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

public class ArgsBuilderJena
    extends ArgsBuilderRdfConvertBase<ArgsBuilderJena>
{
    public static ArgsBuilderJena newBuilder() {
        return new ArgsBuilderJena();
    }

    @Override
    protected String processSrcLang(String srcLangStr) {
        Lang srcLang = RDFLanguagesEx.findLang(srcLangStr);
        if (srcLang == null) {
            throw new IllegalArgumentException("Could not resolve argument to a jena lang: " + srcLangStr);
        }
        return srcLang.getName(); //.getContentType().getContentTypeStr();
    }

    @Override
    protected String processTgtFormat(String tgtFormatStr) {
        RDFFormat tgtFormat = RDFLanguagesEx.findRdfFormat(tgtFormatStr);
        if (tgtFormat == null) {
            throw new IllegalArgumentException("Could not resolve argument to a jena rdf format: " + tgtFormatStr);
        }
        return tgtFormat.toString();
    }

    @Override
    public List<String> build() {
        List<String> result = ArgsBuilder.newBuilder()
            .opt("-i", srcFmtArg)
            .opt("-o", tgtFmtArg)
            .arg(baseUri)
            .build();
        return result;
    }
}
