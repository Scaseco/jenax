package org.aksw.jenax.shellgebra.cmd;

import java.util.Optional;

import org.aksw.shellgebra.registry.content.Tool;

public interface ContentConvertProvider {
    // Optional<Tool> getConverter(String srcFormat, String tgtFormat, String base);
    Optional<Tool> getConverter(OpSpecContentConvertRdf spec);
}
