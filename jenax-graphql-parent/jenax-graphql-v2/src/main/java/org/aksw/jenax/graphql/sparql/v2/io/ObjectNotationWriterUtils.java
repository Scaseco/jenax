package org.aksw.jenax.graphql.sparql.v2.io;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProvider;

public class ObjectNotationWriterUtils {
    /** Send an object to the writer via a provider and value converter.
    /*  If key/value conversion is needed, use {@link ObjectNotationWriterMapperImpl}. */
    public static <K, V> void sendToWriter(
            ObjectNotationWriter<K, V> writer,
            GonProvider<K, V> provider,
            Object obj) throws IOException {
        if (provider.isObject(obj)) {
            writer.beginObject();
            Iterator<Entry<K, Object>> it = provider.listProperties(obj);
            while (it.hasNext()) {
                Entry<K, Object> e = it.next();
                K inKey = e.getKey();
                // KO outKey = keyMapper.apply(inKey);
                Object value = e.getValue();
                writer.name(inKey);
                sendToWriter(writer, provider, value);
            }
            writer.endObject();
        } else if (provider.isArray(obj)) {
            writer.beginArray();
            Iterator<Object> it = provider.listElements(obj);
            while (it.hasNext()) {
                Object element = it.next();
                sendToWriter(writer, provider, element);
            }
            writer.endArray();
        } else if (provider.isLiteral(obj)){
            V inValue = provider.getLiteral(obj);
            // VO outValue = valueMapper.apply(inValue);
            writer.value(inValue);
        } else if (provider.isNull(obj)) {
            writer.nullValue();
        } else {
            throw new IllegalArgumentException("Provider could not handle argument: " + obj);
        }
    }
}
