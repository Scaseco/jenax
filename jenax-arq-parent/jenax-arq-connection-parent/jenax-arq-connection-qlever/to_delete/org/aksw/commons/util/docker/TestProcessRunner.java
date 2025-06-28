package org.aksw.commons.util.docker;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;
import org.aksw.shellgebra.processbuilder.ProcessBuilderDocker;
import org.aksw.shellgebra.processbuilder.ProcessBuilderGroup;
import org.aksw.shellgebra.processbuilder.ProcessBuilderPipeline;
import org.aksw.shellgebra.shim.cmd.GenericCodecArgs;
import org.aksw.shellgebra.shim.core.ArgsModular;
import org.aksw.vshell.registry.ProcessBuilderJvm;
import org.aksw.vshell.registry.ProcessBuilderNative;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.newsclub.net.unix.FileDescriptorCast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;

public class TestProcessRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestProcessRunner.class);

    @Test
    public void test01() throws Exception {
        FileMapper fileMapper = FileMapper.of("/tmp/shared");
        try (ProcessRunner runner = ProcessRunnerPosix.create()) {
            runner.setOutputLineReaderUtf8(logger::info);
            runner.setErrorLineReaderUtf8(logger::info);
            runner.setInputPrintStreamUtf8(out -> {
                logger.info("Data generation thread started.");
                for (int i = 0; i < 10000; ++i) {
                    out.println("" + i);
                }
                out.flush();
                logger.info("Data generation thread terminated.");
            });

            System.out.println("Process 1");
            ProcessBuilderNative.of("head", "-n 2").start(runner).waitFor();
            Thread.sleep(1000);

            System.out.println("Process 2");
            ProcessBuilderNative.of("head", "-n 4").start(runner).waitFor();

            TestCommandRegistry.initJvmCmdRegistry(runner.getJvmCmdRegistry());

//            System.out.println("Process 3");
//            ProcessBuilderJvm.of("/bin/head", "-n10").start(runner).waitFor();

//            System.out.println("Process 4a");
//            ProcessBuilderDocker.of("echo", "FIRST DOCKER TEST STRING")
//                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper)
//                .redirectInput(new JRedirectJava(Redirect.from(new File("/dev/null"))))
//                .start(runner)
//                .waitFor();
//
//            System.out.println("Process 4b");
//            ProcessBuilderDocker.of("echo", "SECOND DOCKER TEST STRING")
//                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper)
//                .redirectInput(new JRedirectJava(Redirect.from(new File("/dev/null"))))
//                .start(runner)
//                .waitFor();

//            System.out.println("Process 5");
//            ProcessBuilderDocker.of("head", "-n 4") // .of("head", "-n 4")
//                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
//                .waitFor();

            System.out.println("Process 6");
            ProcessBuilderPipeline.of(
                ProcessBuilderJvm.of("/bin/head", "-n10"),
                // ProcessBuilderNative.of("/bin/head", "-n10"),
                ProcessBuilderDocker.of("/usr/bin/lbzip2", "-c")
                    .interactive(true)
                    .imageRef("nestio/lbzip2").fileMapper(fileMapper), // .entrypoint("bash")
                ProcessBuilderJvm.of("/jvm/bzip2", "-d"))
                // ProcessBuilderJvm.of("/bin/cat"))
                .start(runner)
                .waitFor();

            System.out.println("All processes completed.");
        }
    }

    @Test
    public void testGroup() throws Exception {
        FileMapper fileMapper = FileMapper.of("/tmp/shared");
        try (ProcessRunner runner = ProcessRunnerPosix.create()) {
            runner.setOutputLineReaderUtf8(logger::info);
            runner.setErrorLineReaderUtf8(logger::info);
            runner.setInputPrintStreamUtf8(out -> {
                logger.info("Data generation thread started.");
                for (int i = 0; i < 10000; ++i) {
                    out.println("" + i);
                }
                out.flush();
                logger.info("Data generation thread terminated.");
            });

            TestCommandRegistry.initJvmCmdRegistry(runner.getJvmCmdRegistry());

            ProcessBuilderGroup.of(
                ProcessBuilderJvm.of("/bin/echo", "Process 1"),
                ProcessBuilderNative.of("head", "-n 2"),

                ProcessBuilderJvm.of("/bin/echo", "Process 2"),
                ProcessBuilderNative.of("head", "-n 4"),

                ProcessBuilderJvm.of("/bin/echo", "Process 3"),
                ProcessBuilderPipeline.of(
                    ProcessBuilderJvm.of("/bin/head", "-n10"),
                    ProcessBuilderDocker.of("/usr/bin/lbzip2", "-c")
                        .imageRef("nestio/lbzip2").fileMapper(fileMapper), // entrypoint("bash")
                    ProcessBuilderJvm.of("/jvm/bzip2", "-d"))
            ).start(runner).waitFor();
        }
    }


    // @Test
//    public void test02() throws Exception {
//        FileMapper fileMapper = FileMapper.of("/tmp/shared");
//
//        try (ProcessRunner runner = ProcessRunnerPosix.create()) {
//            runner.setOutputLineReaderUtf8(logger::info);
//            runner.setErrorLineReaderUtf8(logger::info);
//            runner.setInputPrintStreamUtf8(out -> {
//                logger.info("Data generation thread started.");
//                for (int i = 0; i < 1000; ++i) {
//                    out.println("" + i);
//                }
//                out.flush();
//                logger.info("Data generation thread terminated.");
//            });
//
//            // XXX Low-level ProcessBuilder.start(runner) is perhaps the better direction?
//            // The process builder can take the pipes and e.g. start the docker container.
//            // This is not the responsibility of the runner. Although, could the runner provide default setups for
//            // docker containers?
//            // Also, the purpose of command line parsing is to auto-detect which files need to be
//            // bind mounted.
//            // So the idea is that given parsed command line, the process builder can auto-wire the arguments.
//
//            System.out.println("stdin source: " + SysRuntime.getFdPath(((FileInputStream)runner.internalIn()).getFD()));
//
//            ProcessBuilder pb1 = new ProcessBuilder("head", "-n 2");
//            runner.start(pb1).waitFor();
//            Thread.sleep(1000);
//            // System.out.println("Available: " + runner.internalIn().available() + " on " + ContainerUtils.getFdPath(((FileInputStream)runner.internalIn()).getFD()));
//            if (false) {
//                // Issue: It seems that SOMETIMES (not reliable) data is delivered to the read end of any open
//                // reader.
//                runner.internalIn().transferTo(System.out);
//            }
//            System.out.println("Process 2: Starting.");
//            runner.start(new ProcessBuilder("head", "-n 2")).waitFor();
//            System.out.println("Process 2: Terminated.");
//
//            if (false) {
//                TestCommandRegistry.initJvmCmdRegistry(runner.getJvmCmdRegistry());
//
//                // ProcessBuilderJvm jvmProcessBuilder = ProcessBuilderJvm.of("/bin/cat");
//                // Process jvmProcess = jvmProcessBuilder.start(runner);
//                // runner.startJvm(ProcessBuilderJvm.of("/bin/cat")).waitFor();
//
//
//                // runner.start(ProcessBuilderDocker.of("head", "-n 2").entrypoint("bash"));
//                ProcessBuilderDocker.of("echo", "DOCKERTESTMSG")
//                    .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
//                    .waitFor();
//    //            ProcessBuilderDocker.of("head", "-n 2")
//    //                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
//    //                .waitFor();
//
//                // System.out.println("exit code: " + jvmProcess.exitValue());
//            }
//        }
//    }

    // @Test
    public void testDocker() throws Exception {
        FileMapper fileMapper = FileMapper.of("/tmp/shared");

        try (ProcessRunner runner = ProcessRunnerPosix.create()) {
            runner.setOutputLineReaderUtf8(logger::info);
            runner.setErrorLineReaderUtf8(logger::info);
            runner.setInputPrintStreamUtf8(out -> {
                logger.info("Data generation thread started.");
                for (int i = 0; i < 1000; ++i) {
                    out.println("" + i);
                }
                out.flush();
                logger.info("Data generation thread terminated.");
            });

        // long pid = ProcessHandle.current().pid()
        try (FileInputStream in = new FileInputStream(runner.inputPipe().toFile())) {
//             System.out.println("fd is " + ContainerUtils.extractFD(in.getFD()));
             int fdVal = FileDescriptorCast.using(in.getFD()).as(Integer.class);
             Path procPath = Paths.get("/proc/self/fd/" + fdVal);
             System.out.println("fd = " + fdVal);
             System.out.println("proc path = " + procPath);
        }


        // SharedSecrets.getJavaIOFileDescriptorAccess();

        ProcessBuilderDocker.of("echo", "DOCKERTESTMSG")
            .imageRef("ubuntu:24.04").fileMapper(fileMapper).start(runner) // .entrypoint("bash")
            .waitFor();
//            ProcessBuilderDocker.of("head", "-n 2")
//                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
//                .waitFor();

        }
    }

    @Test
    public void testProcessBuilderLbzip2() throws Exception {
        String expectedStr = "Hello World";
        Path in = Files.createTempFile("data-", ".bz2");
        try {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                BZip2CompressorOutputStream bzip = new BZip2CompressorOutputStream(out)) {
                bzip.write(expectedStr.getBytes(StandardCharsets.UTF_8));
                bzip.finish();
                bzip.flush();
                Files.write(in, out.toByteArray());
            }

            // TODO FileMapper could be part of the process runner
            FileMapper fileMapper = FileMapper.of("/tmp/shared");
            try (ProcessRunner runner = ProcessRunnerPosix.create()) {
                String[] args = new String[] {"/usr/bin/lbzip2", "-cd", in.toString()};
                ArgsModular<GenericCodecArgs> model = GenericCodecArgs.parse(args);
                model.toArgList().args();

                ProcessBuilderDocker
                    .of("/usr/bin/lbzip2", "-cd", in.toString())
                    .imageRef("nestio/lbzip2")
                    .commandParser(GenericCodecArgs::parse)
                    .fileMapper(fileMapper)
                    .start(runner);

                // Close the process-facing pipes so that any remaining data can be consumed without
                // causing a deadlock while waiting for new data.
                runner.shutdown();

                String actualStr = IOUtils.toString(runner.getInputStream(), StandardCharsets.UTF_8);
                Assert.assertEquals(expectedStr, actualStr);
            }
        } finally {
            Files.deleteIfExists(in);
        }
    }
}
