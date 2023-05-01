package org.aksw.jenax.arq.util.tuple;

import java.util.stream.Stream;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;
import org.aksw.commons.tuple.finder.TupleFinder;

import com.google.common.base.Converter;

/**
 * An TupleFinder that delegates to another one by means of converting all tuples back and forth.
 * For example, can provide a Quad view over a backing {@code Tuple<NodeId>}.
 */
public class TupleFinderMapper<ID, IC, OD, OC>
    implements TupleFinder<ID, IC>
{
    protected TupleFinder<OD, OC> backend;
    protected Converter<IC, OC> componentConverter;
    protected TupleBridge<ID, IC> inBridge;

    protected TupleFinderMapper(TupleFinder<OD, OC> backend, Converter<IC, OC> componentConverter,
            TupleBridge<ID, IC> inBridge) {
        super();
        this.backend = backend;
        this.componentConverter = componentConverter;
        this.inBridge = inBridge;
    }

    public static <ID, IC, OD, OC> TupleFinder<ID, IC> wrap(
            TupleFinder<OD, OC> backend,
            Converter<IC, OC> componentConverter,
            TupleBridge<ID, IC> inBridge) {
        return new TupleFinderMapper<>(backend, componentConverter, inBridge);
    }

    @Override
    public TupleBridge<ID, IC> getTupleBridge() {
        return inBridge;
    }

    @Override
    public <X> Stream<ID> find(X tuple, TupleAccessor<? super X, ? extends IC> accessor) {
        OD backendTuple = ConverterTuple.convert(tuple, accessor, componentConverter::convert, backend.getTupleBridge());
        Stream<OD> base = backend.find(backendTuple);
        Stream<ID> result = base.map(t ->
            ConverterTuple.convert(t, backend.getTupleBridge(), componentConverter.reverse()::convert, inBridge));
        return result;
    }
}
