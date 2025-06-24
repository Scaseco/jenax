package org.aksw.jenax.dataaccess.sparql.engine;

import java.util.function.Consumer;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderHTTP;

public class RDFLinkSourceHTTPSimple
    implements RDFLinkSourceHTTP
{
    protected Consumer<RDFLinkBuilderHTTP<?>> configurator;

    public RDFLinkSourceHTTPSimple(Consumer<RDFLinkBuilderHTTP<?>> configurator) {
        super();
        this.configurator = configurator;
    }

    public static RDFLinkSourceHTTPSimple of(Consumer<RDFLinkBuilderHTTP<?>> configurator) {
        return new RDFLinkSourceHTTPSimple(configurator);
    }

    @Override
    public RDFLinkBuilderHTTP<?> newLinkBuilder() {
        RDFLinkBuilderHTTP<?> result = new RDFLinkBuilderHTTP<>();
        if (configurator != null) {
            configurator.accept(result);
        }
        return result;
    }
}
