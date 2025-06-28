package org.aksw.shellgebra.algebra.stream.op;

/** Byte-level operation. Transform a stream of bytes into another one with a content type conversion applied. */
public class StreamOpContentConvert
    extends StreamOp1
{
    // XXX Enforce mime types?
    protected String sourceFormat;
    protected String targetFormat;

    // XXX Do we need a base IRI to parameterize RDF conversions?
    protected String baseIri;

    public StreamOpContentConvert(String sourceFormat, String targetFormat, String baseIri, StreamOp subOp) {
        super(subOp);
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
        this.baseIri = baseIri;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public String getTargetFormat() {
        return targetFormat;
    }

    public String getBaseIri() {
        return baseIri;
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(convert (" + subOp + " " + sourceFormat + " " + targetFormat + " " + baseIri + "))";
    }
}
