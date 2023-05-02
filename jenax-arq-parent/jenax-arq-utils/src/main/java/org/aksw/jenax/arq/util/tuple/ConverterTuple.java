package org.aksw.jenax.arq.util.tuple;

import java.util.function.Function;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;

import com.google.common.base.Converter;

/**
 * A converter for mapping between two tuple types (e.g. Quad and {@code Tuple<NodeId>})
 * based on mapping their components
 */
public class ConverterTuple<ID, IC, OD, OC>
    extends Converter<ID, OD>
{
    protected TupleBridge<ID, IC> inBridge;
    protected TupleBridge<OD, OC> outBridge;
    protected TupleAccessor<ID, OC> toOutAccessor;
    protected TupleAccessor<OD, IC> toInAccessor;

    public ConverterTuple(TupleBridge<ID, IC> inBridge, TupleBridge<OD, OC> outBridge,
            TupleAccessor<ID, OC> toOutAccessor, TupleAccessor<OD, IC> toInAccessor) {
        super();
        this.inBridge = inBridge;
        this.outBridge = outBridge;
        this.toOutAccessor = toOutAccessor;
        this.toInAccessor = toInAccessor;
    }

    @Override
    protected OD doForward(ID a) {
        OD result = outBridge.build(a, toOutAccessor);
        return result;
    }

    @Override
    protected ID doBackward(OD b) {
        ID result = inBridge.build(b, toInAccessor);
        return result;
    }

    public static <ID, IC, OD, OC> Converter<ID, OD> create(
        TupleBridge<ID, IC> inBridge,
        TupleBridge<OD, OC> outBridge,
        Converter<IC, OC> componentConverter)
    {
        TupleAccessor<ID, OC> toOutAccessor = inBridge.map(componentConverter::convert);
        TupleAccessor<OD, IC> toInAccessor = outBridge.map(componentConverter.reverse()::convert);
        return new ConverterTuple<>(inBridge, outBridge, toOutAccessor, toInAccessor);
    }

    /** Convert an input tuple to an output one via a mapping of its components */
    public static <ID, IC, OD, OC> OD convert(ID inTuple,
            TupleAccessor<ID, IC> inAccessor, Function<IC, OC> inToOut, TupleBridge<OD, OC> outBridge) {
        TupleAccessor<ID, OC> toOutAccessor = inAccessor.map(inToOut);
        OD result = outBridge.build(inTuple, toOutAccessor);
        return result;
    }
}
