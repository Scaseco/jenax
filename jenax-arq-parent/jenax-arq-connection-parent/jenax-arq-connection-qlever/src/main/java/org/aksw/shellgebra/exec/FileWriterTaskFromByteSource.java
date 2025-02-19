package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.common.io.ByteSource;

public class FileWriterTaskFromByteSource
    extends FileWriterTaskViaExecutor
{
    protected ByteSource byteSource;

    public FileWriterTaskFromByteSource(Path path, PathLifeCycle pathLifeCycle, ByteSource byteSource) {
        super(path, pathLifeCycle);
        this.byteSource = byteSource;
    }

    @Override
    protected void abortActual() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void prepareWriteFile() throws IOException {
        // Nothing
    }

    @Override
    protected void runWriteFile() throws IOException {
        try (OutputStream out = Files.newOutputStream(outputPath, StandardOpenOption.WRITE)) {
            try (InputStream in = byteSource.openStream()) {
                in.transferTo(out);
            }
            out.flush();
        }
    }
}
