package org.aksw.jenax.graphql.rdf.adapter;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.jenax.graphql.json.api.GraphQlDataProvider;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlDataProvider;
import org.aksw.jenax.io.json.writer.RdfObjectNotationWriter;
import org.aksw.jenax.io.json.writer.RdfObjectNotationWriterViaJson;
import org.aksw.jenax.ron.RdfElement;
import org.aksw.jenax.ron.RdfElementVisitor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/** JSON adapter over RDF */
public class GraphQlDataProviderOverRdf
    implements GraphQlDataProvider
{
    protected RdfGraphQlDataProvider delegate;
    protected RdfElementVisitor<JsonElement> converter;

//    public GraphQlDataProviderOverRdf(RdfGraphQlDataProvider delegate) {
//        this(delegate, new RdfElementVisitorRdfToJson());
//    }

    public GraphQlDataProviderOverRdf(RdfGraphQlDataProvider delegate, RdfElementVisitor<JsonElement> converter) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
        this.converter = Objects.requireNonNull(converter);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public JsonObject getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public Stream<JsonElement> openStream() {
        Stream<RdfElement> base = delegate.openStream();
        return base.map(elt -> elt.accept(converter));
    }

    @Override
    public boolean isSingle() {
        return delegate.isSingle();
    }

    @Override
    public void write(JsonWriter writer, Gson gson) throws IOException {
        RdfObjectNotationWriter w = new RdfObjectNotationWriterViaJson(gson, writer);
        delegate.write(w);
    }
}
