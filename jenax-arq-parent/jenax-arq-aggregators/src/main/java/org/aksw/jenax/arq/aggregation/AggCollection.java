package org.aksw.jenax.arq.aggregation;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;

public class AggCollection<T, E, COLLECTION, ITEM>
    implements Aggregator<T, E, COLLECTION>
{

    protected Supplier<COLLECTION> collectionSupplier;
    protected Function<T, ? extends ITEM> bindingToItem;
    protected BiConsumer<? super COLLECTION, ? super ITEM> addToCollection;

    public AggCollection(
            Supplier<COLLECTION> collector,
            Function<T, ? extends ITEM> bindingToItem,
            BiConsumer<? super COLLECTION, ? super ITEM> addToCollection
            ) {
        super();
        this.collectionSupplier = collector;
        this.bindingToItem = bindingToItem;
        this.addToCollection = addToCollection;
    }

    @Override
    public Accumulator<T, E, COLLECTION> createAccumulator() {
        COLLECTION collection = collectionSupplier.get();
        return new AccCollection(collection);
    }

    public class AccCollection
        implements Accumulator<T, E, COLLECTION>
    {
        protected COLLECTION collection;

        public AccCollection(COLLECTION collection) {
            super();
            this.collection = collection;
        }

        @Override
        public void accumulate(T binding, E env) {
            ITEM item = bindingToItem.apply(binding);
            addToCollection.accept(collection, item);
        }

        @Override
        public COLLECTION getValue() {
            return collection;
        }
    }
}
