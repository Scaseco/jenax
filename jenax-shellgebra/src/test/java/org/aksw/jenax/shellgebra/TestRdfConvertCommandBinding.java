package org.aksw.jenax.shellgebra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.jenax.shellgebra.cmd.ArgsTransformRdfConvertToRapper;
import org.aksw.jenax.shellgebra.cmd.JvmCommandRapper;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessIoWrapper;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.registry.init.InitCommandRegistry;
import org.aksw.shellgebra.shim.core.ArgsTransform;
import org.aksw.vshell.registry.CmdExecSystem;
import org.aksw.vshell.registry.FinalPlacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRdfConvertCommandBinding {
    private static final Logger logger = LoggerFactory.getLogger(TestRdfConvertCommandBinding.class);

    /** This test case should execute via jvm because rapper does not support the jsonld output format. */
    @Test
    public void testRdfConvertViaJena() throws Exception {
        ContainerUtils.setGlobalRetryCountIfAbsent(1);
        // The test case uses an old qlever image which does not include 'rapper'.
        // Newer images may or may not ship with rapper.
        ExecSite preferredExecSite = ExecSites.docker("adfreiburg/qlever:commit-a307781");
        ExecSite expectedExecSite = ExecSites.jvm();
        CmdOp cmdOp = CmdOpExec.ofLiterals("/virt/rdf-convert", "-i", "ntriples", "-o", "jsonld");
        runTest(cmdOp, preferredExecSite, expectedExecSite);
    }

    @Test
    public void testRdfConvertViaRapper() throws Exception {
        ContainerUtils.setGlobalRetryCountIfAbsent(1);
        ExecSite preferredExecSite = ExecSites.docker("semmtech/raptor");
        ExecSite expectedExecSite = preferredExecSite;
        CmdOp cmdOp = CmdOpExec.ofLiterals("/virt/rdf-convert", "-i", "ntriples", "-o", "turtle");
        runTest(cmdOp, preferredExecSite, expectedExecSite);
    }

    public void runTest(CmdOp cmdOp, ExecSite preferredExecSite, ExecSite expectedExecSite) throws IOException, Exception {
        // XXX Make the fileMapper part of the execSystem? Probably yes.
        FileMapper fileMapper = FileMapper.of("/tmp/shared");

        CmdExecSystem cmdExecSystem = CmdExecSystem.newBuilder().build();
        InitCommandRegistry.initJvmCmdRegistry(cmdExecSystem.getJvmCmdRegistry());

        cmdExecSystem.getJvmCmdRegistry().put("/virt/rdf-convert", new JvmCommandRapper());
        cmdExecSystem.getCandidates().put("/virt/rdf-convert", ExecSites.docker("semmtech/raptor"), "rapper", ArgsTransformRdfConvertToRapper::map);
        cmdExecSystem.getCandidates().put("/virt/rdf-convert", ExecSites.jvm(), "/virt/rdf-convert", ArgsTransform.identity());

        FinalPlacement placement = cmdExecSystem.rewrite(cmdOp, preferredExecSite);
        ExecSite actualExecSite = placement.cmdOp().execSite();
        assertEquals(expectedExecSite, actualExecSite);

        try (ProcessRunner context = ProcessRunnerPosix.create()) {
            Process p = cmdExecSystem.exec(context, fileMapper, placement);
            ProcessIoWrapper.builder(p)
                .setOutputLineReaderUtf8(line -> logger.info("Got line: " + line))
                .setErrorLineReaderUtf8(logger::info)
                .setInputWriterUtf8(out -> {
                    out.write("<http://www.example.org/s> <http://www.example.org/p> <http://www.example.org/o> .");
                    out.newLine();
                })
                .exec();
            p.waitFor();
        }
    }
}
