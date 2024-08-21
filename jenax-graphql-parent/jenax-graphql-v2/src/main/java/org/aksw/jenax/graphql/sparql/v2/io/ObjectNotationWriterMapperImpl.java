package org.aksw.jenax.graphql.sparql.v2.io;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProvider;

public class ObjectNotationWriterMapperImpl<KI, KO, VI, VO>
    implements ObjectNotationWriterMapper<KI, KO, VI, VO>
{
    protected ObjectNotationWriter<KO, VO> delegate;
    protected GonProvider<KO, VO> gonProvider;
    protected Function<? super KI, ? extends KO> keyMapper;
    // The value mapper must map to objects that are understood by the gonProvider.
    // This allows literal values in VI to be mapped to JSON objects
    protected Function<? super VI, ? extends Object> valueMapper;

    public ObjectNotationWriterMapperImpl(
            ObjectNotationWriter<KO, VO> delegate,
            GonProvider<KO, VO> gonProvider,
            Function<? super KI, ? extends KO> keyMapper,
            Function<? super VI, ? extends Object> valueMapper) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
        this.gonProvider = Objects.requireNonNull(gonProvider);
        this.keyMapper = Objects.requireNonNull(keyMapper);
        this.valueMapper = Objects.requireNonNull(valueMapper);
    }

    protected ObjectNotationWriter<KO, VO> getDelegate() {
        return delegate;
    }

    @Override public void flush() throws IOException
    { getDelegate().flush(); }

    @Override public ObjectNotationWriter<KI, VI> beginArray() throws IOException
    { getDelegate().beginArray(); return this; }

    @Override public ObjectNotationWriter<KI, VI> endArray() throws IOException
    { getDelegate().endArray(); return this; }

    @Override public ObjectNotationWriter<KI, VI> beginObject() throws IOException
    { getDelegate().beginObject(); return this; }

    @Override public ObjectNotationWriter<KI, VI> endObject() throws IOException
    { getDelegate().endObject(); return this; }

    @Override
    public ObjectNotationWriter<KI, VI> name(KI key) throws IOException {
        KO outKey = keyMapper.apply(key);
        getDelegate().name(outKey);
        return this;
    }

    /** Write a primitive value (should exclude null) */
    @Override
    public ObjectNotationWriter<KI, VI> value(VI value) throws IOException {
        Object outValue = valueMapper.apply(value);
        ObjectNotationWriterUtils.sendToWriter(delegate, gonProvider, outValue);
        // getDelegate().value(outValue);
        return this;
    }

    @Override public ObjectNotationWriter<KI, VI> nullValue() throws IOException
    { getDelegate().nullValue(); return this; }
}
