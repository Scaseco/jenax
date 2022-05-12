package org.aksw.jenax.arq.util.syntax;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.query.Query;

import com.google.common.hash.HashCode;

public class QueryStringHashCode {
    protected Query query;
    protected HashCode bodyHash;
    protected HashCode projectionHash;
    // protected

    public static <T> BigInteger lehmerValue(
            Collection<T> items,
            Comparator<T> comparator) {
    	int[] lehmerCode = lehmerCode(items, comparator);
    	BigInteger result = valueOfLehmerCode(lehmerCode);
    	return result;
    }

    /**
     * For a given collection of unique items with a defined total ordering (via comparator)
     * compute the natural number that correspends to n-th permutation w.r.t. the lehmer code.
     *
     * This implementation adds the provided items into a LinkedHashSet - so only their first occurrence matters.
     *
     *
     * @param <T>
     * @param items
     * @param comparator
     * @return
     */
    public static <T> int[] lehmerCode(
            Collection<T> items,
            Comparator<T> comparator) {

        Set<T> unique = new LinkedHashSet<>(items);
        int n = unique.size();
        int m = n - 1;

        TreeSet<T> ordered = new TreeSet<>(comparator);
        ordered.addAll(unique);

        Map<T, Integer> itemToIndex = new HashMap<>();
        {
            Iterator<T> it = ordered.iterator();
            for (int i = 0; it.hasNext(); ++i) {
                T item = it.next();
                itemToIndex.put(item, i);
            }
        }

        int[] arr = new int[n];
        {
            Iterator<T> it = unique.iterator();
            for (int i = 0; it.hasNext(); ++i) {
                T item = it.next();
                int orderId = itemToIndex.get(item);
                arr[i] = orderId;
            }
        }


        int[] lehmer = new int[n];
        {
            int remaining = n;

            // 'i' is always the smallest remaining item in 'arr'
            // on each iteration, in 'arr', count the number of items to the left of 'i' that are greater.
            // add the value to the lehmer array, remove i and repeat until all items have been processed.
            for (int i = 0; i < n; ++i) {
                int value = 0;

                for (int j = 0; j < remaining; ++j) {
                    int cmp = arr[j];
                    if (cmp > i) {
                        ++value;
                    } else if (cmp == i) {
                        lehmer[i] = value;
                        ArrayUtils.removeElement(arr, j);
                        --remaining;
                        break;
                    }
                }
            }
        }

        return lehmer;
    }


    public static BigInteger valueOfLehmerCode(int[] lehmer) {
        int n = lehmer.length;
        int m = n - 1;

        BigInteger fac = BigInteger.valueOf(1);
        BigInteger result = BigInteger.valueOf(0);
        {
            for (int i = 0; i < n; ++i) {
                int value = lehmer[m - i];

                BigInteger contrib = BigInteger.valueOf(value).multiply(fac);
                result = result.add(contrib);
                fac = BigInteger.valueOf(i + 1).multiply(fac);
            }
        }

        return result;
    }

    public static void main(String[] args) {
        Collection<String> items = Arrays.asList("d", "c", "b", "a");
        BigInteger value = lehmerValue(items, Comparator.naturalOrder());
        System.out.println(value);
    }

}
