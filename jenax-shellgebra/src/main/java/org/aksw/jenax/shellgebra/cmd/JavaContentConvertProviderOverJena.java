package org.aksw.jenax.shellgebra.cmd;

import java.util.Optional;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.aksw.jenax.arq.util.io.StreamingRDFConverter;
import org.aksw.shellgebra.shim.cmd.JavaStreamTransform;

public class JavaContentConvertProviderOverJena
    implements JavaContentConvertProvider
{
     @Override
    public Optional<JavaStreamTransform> getConverter(OpSpecContentConvertRdf spec) {
        InputStreamTransform inXform = converter(spec);
        JavaStreamTransform tmp = null;
        if (inXform != null) {
            tmp = new JavaStreamTransform(inXform, null);
        }
        return Optional.ofNullable(tmp);
    }

    public static InputStreamTransform converter(OpSpecContentConvertRdf spec) {
        return StreamingRDFConverter.converter(spec.sourceFormat(), spec.targetFormat(), spec.baseIri());
    }
}
