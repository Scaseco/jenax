package org.aksw.jenax.shellgebra.cmd;

import org.aksw.jenax.arq.util.io.RDFConverterMetaData;
import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.shim.core.ArgsParserProvider;
import org.aksw.shellgebra.shim.core.JvmCommandParser;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

public class ArgParserProviderRapper
    implements ArgsParserProvider
{
    @Override
    public JvmCommandParser newParser(SysRuntime runtime, String commandName) {
        ArgsBuilderRapper argsBuilder = new ArgsBuilderRapper();
        RDFConverterMetaData metaData = argsBuilder.getMetaData();
        return null;
    }

    public static void validate(RapperArgs args, RDFConverterMetaData metaData) throws Exception {
        String srcLangStr = args.getInputFormat();
        String tgtFormatStr = args.getOutputFormat();
        Lang srcLang = getSrcLang(srcLangStr);
        RDFFormat tgtFormat = getTgtFormat(tgtFormatStr);
        boolean canConvert = metaData.supportsConversion(srcLang, tgtFormat);

        if (!canConvert) {
            throw new UnsupportedConversionException(srcLangStr, tgtFormatStr);
        }
    }

    public static Lang getSrcLang(String srcLangStr) {
        Lang srcLang = RDFLanguagesEx.findLang(srcLangStr);
//        String tmpSrcArg = inputLangMap.get(srcLang);
        return srcLang;
    }

    public static RDFFormat getTgtFormat(String tgtFormatStr) {
        RDFFormat tgtFormat = RDFLanguagesEx.findRdfFormat(tgtFormatStr);
//        Lang tgtLang = tgtFormat.getLang();
//        String tmpTgtArg = inputLangMap.get(tgtLang);
        return tgtFormat;
    }
}
