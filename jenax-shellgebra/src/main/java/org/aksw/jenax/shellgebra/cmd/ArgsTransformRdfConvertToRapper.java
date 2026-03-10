package org.aksw.jenax.shellgebra.cmd;

import java.util.List;

import org.aksw.shellgebra.shim.picocli.ArgsParserPicocli;

public class ArgsTransformRdfConvertToRapper {
    /** Convert arguments from the virtual "rdf-convert" command to rapper. */
    public static List<String> map(List<String> args) {
        RapperArgs model = ArgsParserPicocli.of(RapperArgs::new).parse(args);
        String inLangStr = model.getInputFormat();
        String outFormatStr = model.getOutputFormat();
        String baseUrl = model.getBaseUrl();
        // Lang inLang = RDFLanguagesEx.findLang(inLangStr);
        // RDFFormat outFormat = RDFLanguagesEx.findRdfFormat(outFormatStr);
        List<String> result = ArgsBuilderRapper.newBuilder()
            .setSrcLang(inLangStr)
            .setTgtFormat(outFormatStr)
            .setBaseUri(baseUrl)
            .build();
        return result;
    }
}
