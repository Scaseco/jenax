package jenax.engine.qlever;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;

import org.aksw.jsheller.exec.FileWriterTaskBase.PathLifeCycle;
import org.aksw.jsheller.exec.FileWriterTaskFromProcess;
import org.aksw.jsheller.exec.PathLifeCycles;
import org.aksw.jsheller.exec.SysRuntimeImpl;
import org.junit.Test;

public class TestWriterTask {
    @Test
    public void test() throws Exception {
        Path path = Files.createTempFile("test", ".txt");
        Files.delete(path);

        // SysRuntimeImpl.forCurrentOs().createNamedPipe(path);

        path.toFile().deleteOnExit();

        Function<Path, String[]> pathToCmd = p -> {
            return new String[] { "bash", "-c", "echo 'hello world' > " + p };
        };
        String[] cmd = pathToCmd.apply(path);
        cmd = SysRuntimeImpl.forCurrentOs().resolveCommand(cmd);

        System.out.println(Arrays.asList(cmd));
        // String cmd = SysRuntimeImpl.forCurrentOs(). (pathToCmd.apply(path));

        PathLifeCycle lifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());

        try (FileWriterTaskFromProcess task = new FileWriterTaskFromProcess(path, lifeCycle, cmd)) {
            task.start();
            // task.abort();
            // task.waitForCompletion();
            Thread.sleep(10000);
            task.abort();
            System.out.println("Abort called");

            System.out.println("Finished: " + task.getState());
            // String data = Files.readString(path, StandardCharsets.UTF_8);
            // System.out.println(data);
        }
    }
}
