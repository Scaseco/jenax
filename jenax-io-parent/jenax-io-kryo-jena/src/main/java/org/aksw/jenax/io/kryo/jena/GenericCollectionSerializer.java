package org.aksw.jenax.io.kryo.jena;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Riot-based serializer for Models.
 *
 * @author Claus Stadler
 */
public class GenericCollectionSerializer<C, T, D extends C>
    extends Serializer<C>
{
    protected Class<C> collectionClass;
    protected Class<T> itemClass;
    protected Function<C, Stream<T>> colToIter;
    protected Supplier<D> colCtor;
    protected BiConsumer<C, T> addItem;

    public GenericCollectionSerializer(
            Class<C> collectionClass,
            Class<T> itemClass,
            // Function<C, B> toBackend,
            Function<C, Stream<T>> colToIter,
            Supplier<D> colCtor,
            BiConsumer<C, T> addItem) {
        super();
        this.collectionClass = collectionClass;
        this.itemClass = itemClass;
        this.colToIter = colToIter;
        this.colCtor = colCtor;
        this.addItem = addItem;
    }

    public static <C, T, D extends C> GenericCollectionSerializer<C, T, D> create(Class<C> collectionClass, Class<T> itemClass, Function<C, Stream<T>> colToIter, Supplier<D> colCtor, BiConsumer<C, T> addItem) {
        return new GenericCollectionSerializer<>(collectionClass, itemClass, colToIter, colCtor, addItem);
    }

    @Override
    public void write(Kryo kryo, Output output, C col) {
        try (Stream<T> stream = colToIter.apply(col)) {
            stream.forEach(item -> {
                if (item == null) {
                    throw new IllegalStateException("Unexpected null item");
                }
                kryo.writeObjectOrNull(output, item, itemClass);
            });
            kryo.writeObjectOrNull(output, null, itemClass);
        }
    }

    @Override
    public C read(Kryo kryo, Input input, Class<C> objClass) {
        C result = colCtor.get();
        T item;
        while ((item = kryo.readObjectOrNull(input, itemClass)) != null) {
            addItem.accept(result, item);
        }
        return result;
    }
}
