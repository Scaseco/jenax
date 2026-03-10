package org.aksw.commons.util.docker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import org.aksw.shellgebra.io.pipe.NamedPipe;
import org.aksw.shellgebra.io.pipe.PosixPipe;


//class PipeReader {
//    public static Path create(Path path) throws IOException {
//        String[] cmdArr = new String[] {"bash", "-c", "{ while true; do sleep 1; done ; } < <(cat " + path + ")"};
//        Process helper = new ProcessBuilder(cmdArr)
//            .redirectOutput(Redirect.INHERIT)
//            .start();
//        Path fd = Path.of("/proc", Long.toString(helper.pid()), "fd", "0");
//    }
//}


public class TestFileDescriptors {

    @Test
    public void posixPipeTest() throws Exception {
        PosixPipe pipe = PosixPipe.open();

        CompletableFuture.runAsync(() -> {
            try (PrintStream out = new PrintStream(pipe.getOutputStream())) {
                for (int i = 0; i < 1000; ++i) {
                    out.println(i);
                    out.flush();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("Writer done.");
            }
        });

        System.out.println(pipe.getReadEndProcPath() + " -> " + pipe.getReadFd());

        // Run this in a terminal to test reading from the same pipe end:
        System.out.println("head -n2 " + pipe.getReadEndProcPath());

        try (InputStream in = pipe.getInputStream()) {
            // Thread.sleep(60000);
            System.out.println("Transferring data.");
            long amount = in.transferTo(System.out);
            System.out.println("Transferred: " + amount);
        }
    }


    // @Test
    public void testWorkingHackWithAnonymousPipeOverNamedPipe() throws IOException, InterruptedException {
        Path path = Path.of("my-pipe-" + System.nanoTime());
        NamedPipe.create(path); // Calls mkfifo path
        File file = path.toFile();

        CompletableFuture.runAsync(() -> {
            try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
                for (int i = 0; i < 10000; ++i) {
                    out.println(i);
                    out.flush();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("Writer done.");
            }
        });

        // String[] cmdArr = new String[] {"bash", "-c", "{ sleep 30; } < <(cat " + path + ")"};
        String[] cmdArr = new String[] {"bash", "-c", "{ while true; do sleep 1; done ; } < <(cat " + path + ")"};
        Process helper = new ProcessBuilder(cmdArr)
            .redirectOutput(Redirect.INHERIT)
            .start();

        Thread.sleep(1000);
        System.out.println("Process running: " + helper.isAlive());

        Path fd = Path.of("/proc", Long.toString(helper.pid()), "fd", "0");
        System.out.println(path + " -> " + fd);
        System.out.println("head -n2 " + fd);

        try (InputStream in = Files.newInputStream(fd)) {
            Thread.sleep(10000);
            System.out.println("Transferring data.");
            long amount = in.transferTo(System.out);
            System.out.println("Transferred: " + amount);
        }

        System.out.println("Destroying process");
        helper.destroy();
        helper.destroyForcibly();
        helper.waitFor();
    }
}

// String[] cmdArr = new String[] {"bash", "-c", "{ sleep 30; } < <(cat " + path + ")"};
// String[] cmdArr = new String[] {"bash", "-c", "sleep 30;", "name", "<(cat " + path + ")"};
// String[] cmdArr = new String[] {"bash", "-c", "sleep 30 < <(cat " + path + ")"};

// String[] cmdArr = new String[] {"bash", "-c", "{ echo $$; sleep 30; } < <(cat " + path + ")"};

// String[] cmdArr = new String[] {"bash", "-c", "cat < <(cat " + path + ")"};
