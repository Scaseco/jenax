package org.aksw.jenax.shellgebra.convert;

import org.aksw.commons.util.docker.Argv;

public interface ContentConvertProvider {
    Argv buildStreamConvert(String srcFormat, String tgtFormat);
}
