package org.aksw.jenax.io.out.config;

import java.util.List;

import org.aksw.commons.io.util.OutputConfig;

public interface RdfOutputConfig
    extends OutputConfig
{
    Long getPrefixOutputDeferCount();
    List<String> getPrefixSources();
}
