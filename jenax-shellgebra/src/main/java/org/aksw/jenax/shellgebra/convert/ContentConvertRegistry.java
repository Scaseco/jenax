package org.aksw.jenax.shellgebra.convert;

import java.util.Collection;

import org.aksw.commons.util.docker.Argv;

public interface ContentConvertRegistry {
    Collection<Argv> listCandidates(String srcFormat, String tgtFormbat, String baseIRI);
}
