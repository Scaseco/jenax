package org.aksw.shellgebra.unused.algebra.plan;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class OutputStreamTransformOverInputStreamTransform
    implements OutputStreamTransform
{
    private InputStreamTransform inTransform;

    public OutputStreamTransformOverInputStreamTransform(InputStreamTransform inTransform) {
        super();
        this.inTransform = inTransform;
    }

    @Override
    public boolean isPiped() {
        return true;
    }

    @Override
    public InputStreamTransform asInputStreamTransform() {
        return inTransform;
    }

    @Override
    public OutputStream apply(OutputStream out) throws IOException {
        PipedOutputStream outPipe = new PipedOutputStream();
        PipedInputStream inPipe;
        try {
            inPipe = new PipedInputStream(outPipe, 64 * 1024); // 64 KB buffer
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        InputStream in = inTransform.apply(inPipe);
        Thread converterThread = new Thread(() -> {
            try {
                in.transferTo(outPipe);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        converterThread.start();

        OutputStream r = new FilterOutputStream(outPipe) {
            @Override
            public void close() throws IOException {
                converterThread.interrupt();
                try {
                    converterThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    super.close();
                }
            }
        };

        return r;
    }

    @Override
    public String toString() {
        return "(outputTransform from " + inTransform + ")";
    }
}
