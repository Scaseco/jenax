package org.aksw.jenax.shellgebra.cmd;

/**
 * RDF content conversion may require a base IRI.
 */
public record OpSpecContentConvertRdf(String sourceFormat, String targetFormat, String baseIri)
    // implements OpSpec
{
}
