package org.aksw.jena_sparql_api.constraint.api;

import java.util.HashSet;
import java.util.Set;

import org.aksw.jenax.constraint.index.RangeMultimaps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;


public class TestRangeMultimap {
    @Test
    public void test1() {
        RangeMap<Integer, Set<String>> actual = TreeRangeMap.create();

        RangeMultimaps.put(actual, Range.closedOpen(0, 5), "a", HashSet::new);
        RangeMultimaps.put(actual, Range.closedOpen(10, 15), "b", HashSet::new);

        RangeMultimaps.put(actual, Range.closedOpen(0, 10), "c", HashSet::new);
        RangeMultimaps.put(actual, Range.closedOpen(10, 20), "d", HashSet::new);

        RangeMap<Integer, Set<String>> expected = TreeRangeMap.create();
        expected.put(Range.closedOpen(0, 5), Sets.newHashSet("a", "c"));
        expected.put(Range.closedOpen(5, 10), Sets.newHashSet("c"));
        expected.put(Range.closedOpen(10, 15), Sets.newHashSet("b", "d"));
        expected.put(Range.closedOpen(15, 20), Sets.newHashSet("d"));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCoalescing() {
        RangeMap<Integer, Set<String>> actual = TreeRangeMap.create();

        RangeMultimaps.put(actual, Range.closedOpen(0, 5), "a", HashSet::new);
        RangeMultimaps.put(actual, Range.closedOpen(10, 15), "a", HashSet::new);

        RangeMultimaps.put(actual, Range.closedOpen(5, 10), "a", HashSet::new);
        RangeMultimaps.put(actual, Range.closedOpen(10, 20), "a", HashSet::new);

        RangeMap<Integer, Set<String>> expected = TreeRangeMap.create();
        expected.put(Range.closedOpen(0, 20), Sets.newHashSet("a"));

        Assert.assertEquals(expected, actual);
    }

}
