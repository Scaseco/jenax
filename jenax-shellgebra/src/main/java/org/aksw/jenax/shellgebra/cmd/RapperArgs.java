package org.aksw.jenax.shellgebra.cmd;

import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.shim.core.ArgsModular;
import org.aksw.shellgebra.shim.core.ArgumentList;
import org.aksw.shellgebra.shim.core.ArgumentListBuilder;
import org.aksw.shellgebra.shim.picocli.ArgsParserPicocli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Unmatched;

/**
 * Picocli model that mimics rapper args.
 */
public class RapperArgs {
    @Option(names = "-i", description = "Input format")
    String inputFormat;

    @Option(names = "-o", description = "Output format")
    String outputFormat;

    @Parameters(index = "0", arity = "0..1", description = "Input file (use '-' for stdin)")
    String inputFile;

    @Parameters(index = "1", arity = "0..1", description = "Base URL")
    String baseUrl;

    @Unmatched
    List<String> unmatchedArgs = new ArrayList<>();

    public String getInputFormat() {
        return inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public List<String> getUnmatchedArgs() {
        return unmatchedArgs;
    }

//    @Override
//    public ArgumentList toArgList() {
//        return renderArgList(this);
//    }

    public static ArgumentList renderArgList(RapperArgs model) {
        String inputFile = isStdInFile(model.inputFile) ? null : model.inputFile;

        ArgumentList result = ArgumentListBuilder.newBuilder()
            .opt("-i", model.getInputFormat())
            .opt("-o", model.getOutputFormat())
            .fileOrLiteral(inputFile, "-")
            .arg(model.getBaseUrl())
            .args(model.getUnmatchedArgs())
            .build();
        return result;
    }

    public static boolean readsStdin(RapperArgs args) {
        String inputFile = args.getInputFile();
        return isStdInFile(inputFile);
    }

    public static boolean isStdInFile(String inputFile) {
        return inputFile == null || inputFile.equals("-");
    }

    public static ArgsModular<RapperArgs> parse(String... args) {
        RapperArgs model = ArgsParserPicocli.of(RapperArgs::new).parse(args);
        return new ArgsModular<>(model, RapperArgs::renderArgList, RapperArgs::readsStdin);
    }

    @Override
    public String toString() {
        return "inputFormat=" + inputFormat + ", outputFormat=" + outputFormat + ", inputFile=" + inputFile
                + ", baseUrl=" + baseUrl + ", unmatchedArgs=" + unmatchedArgs ;
    }
}
