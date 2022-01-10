package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;

import org.aksw.commons.util.convert.ConvertFunction;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public abstract class MapFromResourceBase<K extends RDFNode, V extends RDFNode>
    extends AbstractMap<K, V>
{
    protected final Resource subject;

    protected final ConvertFunction<? super RDFNode, K> keyConverter;
    protected final ConvertFunction<? super RDFNode, V> valueConverter;

    public MapFromResourceBase(Resource subject,
            ConvertFunction<? super RDFNode, K> keyConverter,
            ConvertFunction<? super RDFNode, V> valueConverter) {
        super();
        this.subject = subject;
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    public ConvertFunction<? super RDFNode, K> getKeyConverter() {
        return keyConverter;
    }

    public ConvertFunction<? super RDFNode, V> getValueConverter() {
        return valueConverter;
    }
}
