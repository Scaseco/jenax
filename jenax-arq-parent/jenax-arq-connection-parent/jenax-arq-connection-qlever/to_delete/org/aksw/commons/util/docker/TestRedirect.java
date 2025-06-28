package org.aksw.commons.util.docker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.op.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.transform.CmdOpVisitorToCmdString;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.exec.CmdStrOpsBash;
import org.aksw.shellgebra.exec.SysRuntimeCore;
import org.aksw.shellgebra.exec.SysRuntimeCoreHost;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import junit.framework.Assert;

public class TestRedirect {

    @Test
    public void test01() throws IOException, InterruptedException {
        Path pipePath = Path.of("/tmp/my-pipe");
        Files.deleteIfExists(pipePath);
        try {
            SysRuntimeImpl.forCurrentOs().createNamedPipe(pipePath);
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "echo hi");

            Thread thread = new Thread(() -> {
                System.out.println("Thread started");
                try (InputStream in = Files.newInputStream(pipePath)) {
                    String str = IOUtils.toString(in, StandardCharsets.UTF_8);
                    System.out.println(str);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Thread ended.");
            });
            thread.start();

            boolean useRedirect = true;
            if (useRedirect) {
                pb.redirectOutput(Redirect.to(pipePath.toFile()));
            }

            Process process = pb.start();
            if (!useRedirect) {
                try (OutputStream out = Files.newOutputStream(pipePath)) {
                    process.getInputStream().transferTo(out);
                }
            }
            process.waitFor();
            System.out.println("Process completed: " + process.exitValue());

            thread.join();
            // String str = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
            // System.out.println(str);
        } finally {
            Files.deleteIfExists(pipePath);
        }

        Pipe pipe = Pipe.open();
        // pipe.source().read(null)
        pipe.sink();
    }

    /**
     * Test case where there is an input stream of data which gets passed to a process which does NOT
     * read the data. Therefore, the data should remain available in the input stream. */
    // Lesson learnt: Trying to reuse the same pipe for multiple processes is brittle.
    // A shell (Bash) should handle this - the shell can better handle setting such things up due to its
    // 'set up the fd table and fork' mechanism.
    // @Test
    public void testStdin() throws IOException, InterruptedException {
        try (SysRuntimeCore runtime = new SysRuntimeCoreHost()) {
            Path fifoPath = Path.of("/tmp/my-test-fifo");
            Files.deleteIfExists(fifoPath);
            runtime.runCmd("mkfifo", fifoPath.toString());

            String expectedStr = "test";
            Thread thread = new Thread(() -> {
                byte buffer[] = new byte[4096];
                try (PushbackInputStream in = new PushbackInputStream(new ByteArrayInputStream(expectedStr.getBytes(StandardCharsets.UTF_8)), buffer.length)) {
                    // Note: Only opening the output stream already blocks if there is no reader connected to the pipe!
                    try (OutputStream fifoOut = Files.newOutputStream(fifoPath)) {
                        while (true) { // Could use Thread.interrupted() but we can just rely on blocking io to handle this.
                            int n = -1;
                            try {
                                System.out.println("Starting writing to fifo.");
                                n = in.read(buffer);
                                if (n > 0) {
                                    fifoOut.write(buffer, 0, n);
                                    fifoOut.flush();
                                } else {
                                    return;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                if (n > 0) {
                                    System.out.println("Unreading " + n + " bytes.");
                                    in.unread(buffer, 0, n);
                                }
                                n = -1;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                // System.out.println("Done writing to fifo.");
            });
            thread.start();

            ProcessBuilder pb = new ProcessBuilder("bash", "-c", "echo hello < /tmp/my-test-fifo");
            // ProcessBuilder pb = new ProcessBuilder("echo", "hello");
            // Trying to use redirect input blocks process creation!
            // pb.redirectInput(fifoPath.toFile());
            Process process = pb.start();
            System.out.println("got echo ouput: " + IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8));

            int exitValue = process.waitFor();
            System.out.println("Exit Value: " + exitValue);


            // ProcessBuilder pb2 = new ProcessBuilder("cat");
            ProcessBuilder pb2 = new ProcessBuilder("bash", "-c", "cat < /tmp/my-test-fifo");
            // pb2.redirectInput(fifoPath.toFile());
            Process cat = pb2.start();
            String actualStr = IOUtils.toString(cat.getInputStream(), StandardCharsets.UTF_8);

            thread.interrupt();
            thread.join();
            System.out.println("Thread stopped.");

            Assert.assertEquals(expectedStr, actualStr);

            Files.deleteIfExists(fifoPath);
        }
    }

    // @Test - TODO Perhaps restore this test case - it fails indeterministically.
    public void testStdin2() throws Exception {
        try (SysRuntimeCore runtime = new SysRuntimeCoreHost()) {
            Path procCtlPath = Path.of("/tmp/proc_ctl");
            Files.deleteIfExists(procCtlPath);
            runtime.runCmd("mkfifo", procCtlPath.toString());

            // FileInputStream x;
            // x.getFD()
            Path fifoInPath = Path.of("/tmp/in");
            Path fifoOutPath = Path.of("/tmp/out");
            Files.deleteIfExists(fifoInPath);
            Files.deleteIfExists(fifoOutPath);
            runtime.runCmd("mkfifo", fifoInPath.toString());
            runtime.runCmd("mkfifo", fifoOutPath.toString());

            Path javaFifoPath = Path.of("/tmp/java");
            Files.deleteIfExists(javaFifoPath);
            runtime.runCmd("mkfifo", javaFifoPath.toString());


            Thread threadCtlThread = new Thread(() -> {
                System.out.println("ThreadCtl: Start");
                try (BufferedReader br = Files.newBufferedReader(procCtlPath, StandardCharsets.UTF_8)) {
                    System.out.println("ThreadCtl: Listening");
                    br.lines().forEach(line -> {
                        System.out.println(line);

                        String prefix = "do: ";
                        if (line.startsWith(prefix)) {
                            System.out.println("Handling action.");
                            String fd = line.substring(prefix.length());

                            try (InputStream xin = Files.newInputStream(Path.of(fd))) {
                                try (OutputStream xout = Files.newOutputStream(javaFifoPath)) {
                                    xin.transferTo(xout);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    System.out.println("ThreadCtl: Done");
                }
            });
            threadCtlThread.start();

            Thread outputReaderThread = new Thread(() -> {
                try (BufferedReader br = Files.newBufferedReader(fifoOutPath)) {
                    br.lines().forEach(line -> System.out.println("OutReaderThread: Read line: " + line));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    System.out.println("OutReaderThread: Done");
                }
            });
            outputReaderThread.start();
//
//            String scriptStr = """
//                {
//                  XIN=<(cat "$1") ;
//                  PID="$$" ;
//                  FD="${XIN#/dev/fd/}" ;
//                  # IN="/proc/$PID/fd/$FD" ;
//                  # ls -lrtha "$IN" > OUT ;
//                  # echo "Proc info: $IN" ;
//                  {
//                    IN="/proc/$$/fd/0"
//                    echo hello > OUT ;
//                    echo "do: $IN" > PROC_CTL ;
//                    cat JAVA_FIFO;
//                    cat "$IN" ;
//                  } < "$XIN"
//                }
//                """
//                    .replace("OUT", fifoOutPath.toString())
//                    .replace("PROC_CTL", procCtlPath.toString())
//                    .replace("JAVA_FIFO", javaFifoPath.toString());
        CmdOp scriptOp = CmdOpGroup.of(List.<CmdOp>of(
            CmdOpGroup.of(List.<CmdOp>of(
                    CmdOpExec.assign("IN", "/proc/$$/fd/0"),
                    CmdOps.appendRedirect(CmdOpExec.ofLiterals("echo", "hello"), CmdRedirect.out("OUT")),
                    CmdOps.appendRedirect(CmdOpExec.ofLiterals("echo", "do"), CmdRedirect.out("PROC_CTL")),
                    CmdOpExec.ofLiterals("cat", "JAVA_FIFO")
                ),
                List.of()
            )),
            List.of(CmdRedirect.in(CmdOpExec.ofLiterals("cat", "$1")))
        );

        CmdString cmdOpStr = scriptOp.accept(new CmdOpVisitorToCmdString(CmdStrOpsBash.get()));
        System.out.println(cmdOpStr);

        String scriptStr = """
            {
              {
                IN="/proc/$$/fd/0" ;
                echo hello > OUT ;
                { bash -c 'IFS= read -r line; printf "x %s\n" "$line"' ; } ;

                # tell process control to start another process and wait for its output.
                echo "do: $IN" > PROC_CTL ; cat JAVA_FIFO ;

                cat "$IN" ;
              } < <(cat "$1")
              # } < "FIFO_IN"
            }
            """
//            {
//              cat "$1" | {
//                IN="/proc/$$/fd/0"
//                echo hello > OUT ;
//                echo "do: $IN" > PROC_CTL ;
//                { bash -c 'IFS= read -r line; printf "x %s\n" "$line"' ; } ;
//                cat JAVA_FIFO;
//                cat "$IN" ;
//              } # < <(cat "$1")
//            }
//            """
                .replace("OUT", fifoOutPath.toString())
                .replace("FIFO_IN", fifoInPath.toString())
                .replace("PROC_CTL", procCtlPath.toString())
                .replace("JAVA_FIFO", javaFifoPath.toString());

            System.out.println(scriptStr);

            ProcessBuilder pb = new ProcessBuilder("bash", "-c", scriptStr,
                "dummy-name",
                fifoInPath.toString()
            );

            pb.redirectError(Redirect.INHERIT);
            pb.redirectOutput(Redirect.INHERIT);
            // ProcessBuilder pb = new ProcessBuilder("echo", "hello");
            // Trying to use redirect input blocks process creation!
            // pb.redirectInput(fifoPath.toFile());
            Process process = pb.start();
            System.out.println("Process " + process.pid() + " running.");

            // Write something into stdin
            try (PrintWriter out = new PrintWriter(Files.newOutputStream(fifoInPath), true, StandardCharsets.UTF_8)) {
                for (int i = 1; i < 10; ++i) {
                    out.println("" + i);
                }
                out.flush();
            } finally {
                System.out.println("Data generator done.");
            }
//            Stages.host(new CmdOpExec("seq", CmdArg.ofLiteral("1000"),
//                    CmdArg.redirect(new RedirectFile(fifoInPath.toString(), OpenMode.WRITE_TRUNCATE, 1))));

            // System.out.println("got echo ouput: " + IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8));

            int exitValue = process.waitFor();
            System.out.println("Process " + process.pid() + " terminated with value: " + exitValue);

//            // ProcessBuilder pb2 = new ProcessBuilder("cat");
//            ProcessBuilder pb2 = new ProcessBuilder("bash", "-c", "cat < /tmp/my-test-fifo");
//            // pb2.redirectInput(fifoPath.toFile());
//            Process cat = pb2.start();
//            String actualStr = IOUtils.toString(cat.getInputStream(), StandardCharsets.UTF_8);
//
//            thread.interrupt();
//            thread.join();
//            System.out.println("Thread stopped.");
//
//            Assert.assertEquals(expectedStr, actualStr);

            Files.deleteIfExists(fifoInPath);
            Files.deleteIfExists(procCtlPath);
        }
    }
//    public void testJNA() {
//        LibC INSTANCE = Native.load("c", LibC.class);
//        int pipe(int[] fds);
//    }

}
