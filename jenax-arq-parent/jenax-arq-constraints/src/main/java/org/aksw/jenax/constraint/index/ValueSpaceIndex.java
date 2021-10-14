package org.aksw.jenax.constraint.index;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jenax.constraint.util.ValueSpaceBase;

import com.google.common.collect.RangeMap;

/**
 * An index for items based on keys that are value spaces.
 * (Maybe call it a value profile?)
 *
 *
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class ValueSpaceIndex<K extends Comparable<K>, V> {
    protected Map<Object, RangeMap<K, Integer>> spaceToRanges;

//    public void put(ValueSpaceBase<K, ?> key, V) {
//
//    }


//    public Stream<V> find(ValueSpaceBase<K, ?> key request) {
//
//    }

//    public Stream<Entry<ValueSpaceBase<K, ?>, V> find(ValueSpaceBase<K, ?> key request) {
//
//    }
}
