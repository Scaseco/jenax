package org.aksw.jenax.shellgebra.cmd;

import java.util.Optional;

import org.aksw.shellgebra.shim.cmd.JavaStreamTransform;

public interface JavaContentConvertProvider {
    Optional<JavaStreamTransform> getConverter(OpSpecContentConvertRdf spec);

    // Optional<JavaStreamTransform> getConverter(String srcFormat, String
    // tgtFormat, String base);
}
