package jenax.engine.qlever.playground;

import java.nio.file.Path;

import org.aksw.jsheller.exec.SysRuntime;
import org.aksw.jsheller.exec.SysRuntimeImpl;

public class MainPlaygroundQlever {
    public static void main(String[] args) throws Exception {
        SysRuntime runtime = SysRuntimeImpl.forCurrentOs();
        runtime.createNamedPipe(Path.of("/tmp/my.pipe"));
    }
}
