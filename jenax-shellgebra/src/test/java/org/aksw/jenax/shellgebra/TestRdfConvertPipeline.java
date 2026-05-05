package org.aksw.jenax.shellgebra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.jenax.shellgebra.cmd.ArgsTransformRdfConvertToRapper;
import org.aksw.jenax.shellgebra.cmd.JvmCommandRapper;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessIoWrapper;
import org.aksw.shellgebra.exec.graph.ProcessIoWrapper.ExecResult;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.registry.init.InitCommandRegistry;
import org.aksw.shellgebra.shim.core.ArgsTransform;
import org.aksw.vshell.registry.CmdExecSystem;
import org.aksw.vshell.registry.FinalPlacement;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.AsyncParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRdfConvertPipeline {
    private static final Logger logger = LoggerFactory.getLogger(TestRdfConvertCommandBinding.class);

    /** This test case should execute via jvm because rapper does not support the jsonld output format. */
    @Test
    public void testRdfConvertViaJena() throws Exception {
        ContainerUtils.setGlobalRetryCountIfAbsent(1);
        // The test case uses an old qlever image which does not include 'rapper'.
        // Newer images may or may not ship with rapper.
        ExecSite preferredExecSite = ExecSites.docker("adfreiburg/qlever:commit-a307781");
        ExecSite expectedExecSite = ExecSites.jvm();

        // Argv decodeBzip2 = CodecRegistry.get().getDecoders("bzip2");

        CmdOp decompress = CmdOps.execArgv("/virt/bzip2", "-dc");
        CmdOp convert = CmdOps.execArgv("/virt/rdf-convert", "-i", "ntriples", "-o", "jsonld");
        CmdOp pipeline = CmdOps.pipeline(decompress, convert);
        runTest(pipeline, preferredExecSite, expectedExecSite, Lang.JSONLD);
    }

    @Test
    public void testRdfConvertViaRapper() throws Exception {
        ContainerUtils.setGlobalRetryCountIfAbsent(1);
        // The test case uses an old qlever image which does not include 'rapper'.
        // Newer images may or may not ship with rapper.
        ExecSite preferredExecSite = ExecSites.docker("adfreiburg/qlever:commit-a307781");
        ExecSite expectedExecSite = ExecSites.jvm();

        // Argv decodeBzip2 = CodecRegistry.get().getDecoders("bzip2");

        CmdOp decompress = CmdOps.execArgv("/virt/bzip2", "-dc");
        CmdOp convert = CmdOps.execArgv("/virt/rdf-convert", "-i", "ntriples", "-o", "turtle");
        CmdOp pipeline = CmdOps.pipeline(decompress, convert);
        runTest(pipeline, preferredExecSite, expectedExecSite, Lang.TURTLE);
    }

//    @Test
//    public void testRdfConvertViaRapper() throws Exception {
//        ContainerUtils.setGlobalRetryCountIfAbsent(1);
//        ExecSite preferredExecSite = ExecSites.docker("semmtech/raptor");
//        ExecSite expectedExecSite = preferredExecSite;
//        CmdOp cmdOp = CmdOpExec.ofLiterals("/virt/rdf-convert", "-i", "ntriples", "-o", "turtle");
//        runTest(cmdOp, preferredExecSite, expectedExecSite);
//    }

    public void runTest(CmdOp cmdOp, ExecSite preferredExecSite, ExecSite expectedExecSite, Lang expectedOutLang) throws IOException, Exception {
        String dataStr = "<http://www.example.org/s> <http://www.example.org/p> <http://www.example.org/o> .";
        Set<Triple> expectedGraph = AsyncParser.of(RDFParser.fromString(dataStr, Lang.TURTLE)).streamTriples().collect(Collectors.toSet());


        // XXX Make the fileMapper part of the execSystem? Probably yes.
        FileMapper fileMapper = FileMapper.of("/tmp/shared");

        CmdExecSystem cmdExecSystem = CmdExecSystem.newBuilder().build();
        InitCommandRegistry.initJvmCmdRegistry(cmdExecSystem.getJvmCmdRegistry());

        cmdExecSystem.getJvmCmdRegistry().put("/virt/rdf-convert", new JvmCommandRapper());
        cmdExecSystem.getCandidates().put("/virt/rdf-convert", ExecSites.docker("semmtech/raptor"), "rapper", ArgsTransformRdfConvertToRapper::map);
        cmdExecSystem.getCandidates().put("/virt/rdf-convert", ExecSites.jvm(), "/virt/rdf-convert", ArgsTransform.identity());

        Process process;
        try (ProcessRunner context = ProcessRunnerPosix.create()) {
            FinalPlacement placement = cmdExecSystem.rewrite(cmdOp, preferredExecSite);
            ExecSite actualExecSite = placement.cmdOp().execSite();
            // assertEquals(expectedExecSite, actualExecSite);

            process = cmdExecSystem.exec(context, fileMapper, placement);
        }

            ExecResult execResult = ProcessIoWrapper.builder(process)
//                .setOutputReader(in -> {
//                    try {
//                        readString[0] = IOUtils.toString(in, StandardCharsets.UTF_8);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                // context.setOutputLineReaderUtf8(line -> logger.info("Got line: " + line));
//                .setErrorLineReaderUtf8(logger::info)
                .setInputGenerator(out -> {
                    try (OutputStream out2 = new BZip2CompressorOutputStream(out)) {
                        out2.write(dataStr.getBytes(StandardCharsets.UTF_8));
                        out2.flush();
                        // out2.close(); // It seems the stream must be closed for final bytes to be written.
                        // out.flush();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .consume();


            // Process p = cmdExecSystem.exec(context, fileMapper, cmdOp, qleverExecSite);
            // process.waitFor();
            // context.shutdown();

            // TODO Add a reliable wait mechanism to ensure all process output has been processed.
            //      Alternatively, make the pipe-mechanism work so that we can use
            //      IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8).
            Thread.sleep(500);

            RDFParserBuilder parserBuilder = RDFParser.fromString(execResult.out(), expectedOutLang);
            Set<Triple> actualGraph = AsyncParser.of(parserBuilder).streamTriples().collect(Collectors.toSet());
            assertEquals(expectedGraph, actualGraph);
    }
}
