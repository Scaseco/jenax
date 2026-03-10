package org.aksw.jenax.shellgebra.cmd;

import org.aksw.shellgebra.registry.content.ArgsBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

public interface ArgsBuilderRdfConvert<X extends ArgsBuilderRdfConvert<X>>
    extends ArgsBuilder
{
    X setSrcLang(String srcLangStr);
    X setTgtFormat(String tgtFormatStr);

    // XXX Move jena-specific methods to a sub-type such as ArgsBuilderRdfConvertJena?
    X setSrcLang(Lang srcLangStr);
    X setTgtFormat(RDFFormat tgtFormatStr);

    X setBaseUri(String baseUri);
}
