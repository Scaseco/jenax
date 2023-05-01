package org.aksw.jenax.arq.util.tuple;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorConcat;

public class IterUtils {
    /** Wrap a stream as an Iter */
    public static <T> Iter<T> iter(Stream<T> stream) {
        return Iter.iter(Iter.onClose(stream.iterator(), stream::close));
    }

    public static <T> Iterator<T> getOrConcat(Iterator<T> base, Iterator<T> toAdd) {
        Iterator<T> result;
        if (toAdd == null) {
            result = base;
        } else {
            if (base == null) {
                result = toAdd;
            } else {
                IteratorConcat<T> it;
                if (base instanceof IteratorConcat) {
                    it = (IteratorConcat<T>)base;
                } else {
                    it = new IteratorConcat<>();
                    it.add(base);
                }
                it.add(toAdd);
                result = it;
            }
        }
        return result;
    }
}
