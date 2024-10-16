package org.aksw.jenax.arq.picocli;

import java.util.List;

import org.aksw.jenax.io.out.config.RdfOutputConfig;

import picocli.CommandLine;

public class CmdMixinRdfOutput
    extends CmdMixinOutput
    implements RdfOutputConfig
{
    @CommandLine.Option(names = { "--op", "--out-prefixes" },
            description = "Prefix sources for output. Subject to used prefix analysis. Default: ${DEFAULT-VALUE}",
            defaultValue = "rdf-prefixes/prefix.cc.2019-12-17.ttl")
    public List<String> outPrefixes = null;

    @CommandLine.Option(names = { "--oup", "--out-used-prefixes" },
            description = "Only for streaming to STDOUT. Number of records by which to defer RDF output for collecting used prefixes. Negative value emits all known prefixes. Default: ${DEFAULT-VALUE}",
            defaultValue = "100")
    public long deferOutputForUsedPrefixes;

    @Override
    public Long getPrefixOutputDeferCount() {
        return deferOutputForUsedPrefixes;
    }

    @Override
    public List<String> getPrefixSources() {
        return outPrefixes;
    }
}
