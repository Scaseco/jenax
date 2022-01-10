package org.aksw.jena_sparql_api.utils.views.map;

import org.aksw.commons.util.convert.ConvertFunction;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

/**
 * An extension of RdfEntry where the key and value can be cast to a given RDFNode sub class.
 *
 * @author raven
 *
 */
public class RdfEntryWithCast<K extends RDFNode, V extends RDFNode>
    extends RdfEntryDelegateBase<K, V>
{
    protected ConvertFunction<? super RDFNode, K> keyConverter;
    protected ConvertFunction<? super RDFNode, V> valueConverter;

    public RdfEntryWithCast(RdfEntry<?, ?> delegate,
            ConvertFunction<? super RDFNode, K> keyConverter,
            ConvertFunction<? super RDFNode, V> valueConverter) {
        super(delegate);
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    public ConvertFunction<? super RDFNode, ? extends RDFNode> getKeyConverter() {
        return keyConverter;
    }

    public ConvertFunction<? super RDFNode, ? extends RDFNode> getValueConverter() {
        return valueConverter;
    }

    @Override
    public K getKey() {
        RDFNode r = getDelegate().getKey();
        if (r != null && keyConverter != null) {
            r = keyConverter.convert(r);
        }
        return (K)r;
    }

    @Override
    public V getValue() {
        RDFNode r = getDelegate().getValue();
        if (r != null && valueConverter != null) {
            r = valueConverter.convert(r);
        }
        return (V)r;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("setValue not implemented");
    }

    @Override
    public RdfEntry<K, V> inModel(Model m) {
        RdfEntry<?, ?> newDelegate = getDelegate().inModel(m);
        return new RdfEntryWithCast<>(newDelegate, keyConverter, valueConverter);
    }
}
