package org.aksw.shellgebra.unused.algebra.plan;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class InputStreamTransformOverOutputStreamTransform
    implements InputStreamTransform
{
    private OutputStreamTransform outTransform;

    public InputStreamTransformOverOutputStreamTransform(OutputStreamTransform outTransform) {
        super();
        this.outTransform = outTransform;
    }

    @Override
    public boolean isPiped() {
        return true;
    }

    @Override
    public OutputStreamTransform asOutputStreamTransform() {
        return outTransform;
    }

    @Override
    public InputStream apply(InputStream in) {
        PipedOutputStream outPipe = new PipedOutputStream();
        PipedInputStream inPipe;
        try {
            inPipe = new PipedInputStream(outPipe, 64 * 1024); // 64 KB buffer
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread converterThread = new Thread(() -> {
            try (OutputStream out = outTransform.apply(outPipe)) {
                in.transferTo(out);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        converterThread.start();

        InputStream r = new FilterInputStream(inPipe) {
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
        return "(inputTransform from " + outTransform + ")";
    }
}
