package org.aksw.jenax.arq.picocli;

import org.aksw.commons.io.util.OutputConfig;

import picocli.CommandLine;

public class CmdMixinOutput
    implements OutputConfig
{
    @CommandLine.Option(names = { "-o", "--out-format" },
            description = "Output format. Default depends on workload. Typically: trig/blocks (quads), turtle/blocks (triples), and tsv (result sets)")
    public String outFormat = null;

    // XXX Add options to control output format per result data model (triples, quads, result sets)?

    @CommandLine.Option(names = { "--out-file" },
            description = "Output file; Merge of files created in out-folder")
    public String outFile = null;

    @CommandLine.Option(names = { "--out-overwrite" }, arity = "0",
            description = "Overwrite existing output files and/or folders")
    public boolean outOverwrite = false;

    @Override
    public String getOutputFormat() {
        return outFormat;
    }

    @Override
    public String getTargetFile() {
        return outFile;
    }

    @Override
    public boolean isOverwriteAllowed() { return outOverwrite; }
}
