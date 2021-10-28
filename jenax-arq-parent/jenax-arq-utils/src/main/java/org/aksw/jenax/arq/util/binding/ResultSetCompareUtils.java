package org.aksw.jenax.arq.util.binding;

import org.aksw.commons.collections.diff.ListDiff;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class ResultSetCompareUtils {

    public static Multiset<Binding> toMultiset(ResultSet rs) {
        Multiset<Binding> result = HashMultiset.create();
        while(rs.hasNext()) {
            Binding original = rs.nextBinding();

            Binding wrapped = original;
            //QuerySolution wrapped = new QuerySolutionWithEquals(original);

            result.add(wrapped);
        }

        return result;
    }

    /**
     * Traverse the resultset in order, and write out the missing items on each side:
     * 1 2
     * ---
     * a a
     * b c
     * d d
     *
     * gives:
     * [c] [b]
     *
     * (1 lacks c, 2 lacks b)
     *
     *
     * @param a
     * @param b
     * @return
     */
    public static ListDiff<Binding> compareOrdered(ResultSet a, ResultSet b) {
        ListDiff<Binding> result = new ListDiff<>();

        Binding x = null;
        Binding y = null;

        while(a.hasNext()) {
            if(!b.hasNext()) {
                while(a.hasNext()) {
                    result.getAdded().add(a.nextBinding());
                }
                return result;
            }

            //if((x == null && y == null) ||  x.equals(y)
            if(x == y || x.equals(y)) {
                x = a.nextBinding();
                y = b.nextBinding();
                continue;
            }

            String sx = x.toString();
            String sy = y.toString();

            if(sx.compareTo(sy) < 0) {
                result.getRemoved().add(x);
                x = a.nextBinding();
            } else {
                result.getAdded().add(y);
                y = b.nextBinding();
            }
        }

        while(b.hasNext()) {
            result.getRemoved().add(b.nextBinding());
        }

        return result;
    }

    public static ListDiff<Binding> compareUnordered(ResultSet a, ResultSet b) {
        ListDiff<Binding> result = new ListDiff<>();

        Multiset<Binding> x = toMultiset(a);
        Multiset<Binding> y = toMultiset(b);

        Multiset<Binding> common = HashMultiset.create(Multisets.intersection(x, y));

        y.removeAll(common);
        x.removeAll(common);

        result.getAdded().addAll(y);
        result.getRemoved().addAll(x);

        return result;
    }


}
