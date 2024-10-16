package org.aksw.jenax.io.rdf.json;

import java.util.Map;

import org.aksw.commons.collections.maps.MapFromKeyConverter;
import org.aksw.commons.collections.maps.MapFromValueConverter;
import org.aksw.jenax.ron.RdfElement;
import org.aksw.jenax.ron.RdfObject;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;

/** A mutable sub view of all JSON compatible values of an RdfObject */
public class JsonObjectViewImpl
    implements JsonObject
{
    protected RdfObject delegate;

    @Override
    public Map<String, JsonElement> getElements() {
        Map<P_Path0, RdfElement> base = delegate.getMembers();

        Converter<P_Path0, String> keyConverter = Converter.from(
            path -> path.isForward() ? path.getNode().getLiteralLexicalForm() : null,
            str -> new P_Link(NodeFactory.createLiteralString(str))
        );

        // TODO Implement converter
        Converter<RdfElement, JsonElement> valueConverter = null;

        Map<String, RdfElement> a = new MapFromKeyConverter<>(base, keyConverter);
        Map<String, JsonElement> b = new MapFromValueConverter<>(a, valueConverter);
        return b;
    }

}
