package org.aksw.jenax.dataaccess.sparql.creator.source;

import java.util.List;

public record SourceDescriptor(String contentType, List<String> encodings) {}
