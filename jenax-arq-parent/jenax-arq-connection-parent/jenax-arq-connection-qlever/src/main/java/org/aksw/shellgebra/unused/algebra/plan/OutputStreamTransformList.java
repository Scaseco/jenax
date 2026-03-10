package org.aksw.shellgebra.unused.algebra.plan;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

public class OutputStreamTransformList
    implements OutputStreamTransform
{
    private List<OutputStreamTransform> transforms;

    public OutputStreamTransformList() {
        this(List.of());
    }

    public OutputStreamTransformList(List<OutputStreamTransform> transforms) {
        super();
        this.transforms = transforms;
    }

    @Override
    public OutputStream apply(OutputStream out) throws IOException {
        Objects.requireNonNull(out);
        OutputStream r = out;
        for (OutputStreamTransform xform : transforms) {
            try {
                OutputStream tmp = xform.apply(r);
                Objects.requireNonNull(tmp);
                r = tmp;
            } catch (RuntimeException | IOException t) {
                t.addSuppressed(new RuntimeException("Error applying stream transform"));
                r.close();
                throw t;
            }
        }
        return r;
    }
}
