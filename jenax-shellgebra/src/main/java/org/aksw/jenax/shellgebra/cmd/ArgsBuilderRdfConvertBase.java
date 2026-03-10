package org.aksw.jenax.shellgebra.cmd;

import org.aksw.commons.util.obj.HasSelf;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

public abstract class ArgsBuilderRdfConvertBase<X extends ArgsBuilderRdfConvertBase<X>>
    implements ArgsBuilderRdfConvert<X>, HasSelf<X>
{
    protected String baseUri;
    protected String srcFmtArg;
    protected String tgtFmtArg;

    public ArgsBuilderRdfConvertBase() {
        super();
    }

    @Override
    public X setSrcLang(Lang srcLangStr) {
        String str = srcLangStr == null ? null : srcLangStr.getName();
        setSrcLang(str);
        return self();
    }

    @Override
    public X setTgtFormat(RDFFormat tgtFormatStr) {
        String str = tgtFormatStr == null ? null : tgtFormatStr.toString();
        setTgtFormat(str);
        return self();
    }

    @Override
    public X setSrcLang(String srcLangStr) {
        this.srcFmtArg = processSrcLang(srcLangStr);
        return self();
    }

    @Override
    public X setTgtFormat(String tgtFormatStr) {
        this.tgtFmtArg = processTgtFormat(tgtFormatStr);
        return self();
    }

    protected String processSrcLang(String srcLangStr) {
        return srcLangStr;
    }

    protected String processTgtFormat(String tgtFormatStr) {
        return tgtFormatStr;
    }

    @Override
    public X setBaseUri(String baseUri) {
        this.baseUri = baseUri;
        return self();
    }
}

