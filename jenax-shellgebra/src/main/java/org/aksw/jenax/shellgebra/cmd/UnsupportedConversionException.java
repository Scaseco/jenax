package org.aksw.jenax.shellgebra.cmd;

public class UnsupportedConversionException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    protected String srcLang;
    protected String tgtFormat;

    public UnsupportedConversionException(String srcLang, String tgtFormat) {
        this("Cannot convert from " + srcLang + " to " + tgtFormat, srcLang, tgtFormat);
    }

    public UnsupportedConversionException(String message, String srcLang, String tgtFormat) {
        super(message);
        this.srcLang = srcLang;
        this.tgtFormat = tgtFormat;
    }

    public String getSrcLang() {
        return srcLang;
    }

    public String getTgtFormat() {
        return tgtFormat;
    }
}
