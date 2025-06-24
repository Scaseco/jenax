package org.aksw.jenax.dataaccess.sparql.creator;

import java.util.Map;

import org.aksw.commons.util.obj.HasSelf;

public interface HasProperties<X>
    extends HasSelf<X>
{
    X setProperty(String key, Object value);
    <T> T getProperty(String key);

    default X setProperties(Map<String, ?> values) {
        values.forEach(this::setProperty);
        return self();
    }
}
