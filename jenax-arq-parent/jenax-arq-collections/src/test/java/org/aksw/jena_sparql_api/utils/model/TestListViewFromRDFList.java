package org.aksw.jena_sparql_api.utils.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.commons.collections.ConvertingList;
import org.aksw.commons.collections.lists.ReverseListIterator;
import org.aksw.jena_sparql_api.rdf.collections.ListFromRDFList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Converter;

public class TestListViewFromRDFList {

    /** Creates a copy of the given list. Performs the mutation on both copy and the given list and asserts equality of the outcome. */
    public static <T> void assertListMutation(List<T> items, Consumer<List<T>> mutator) {
        List<T> reference = new ArrayList<>(items);
        mutator.accept(reference);
        mutator.accept(items);
        Assert.assertEquals(reference, items);
    }

    public static <T> ListIterator<T> iterator(List<T> list, boolean isForward) {
        ListIterator<T> result;
        if (isForward) {
            result = list.listIterator();
        } else {
            int size = list.size();
            result = ReverseListIterator.of(list.listIterator(size));
        }
        return result;
    }

    private static final void debugPrint(Object obj) {
        // System.out.println(str);
    }

    /**
     * Simply compare the list operations of the view with that of a reference
     *
     * - we can use the proxy util from the subgraphisomorphism package
     *
     */
    @Test
    public void testListViewFromRDFList() {
        // FIXME Turn into unit test
        for (boolean isForward : Arrays.asList(true, false)) {

            Model x = ModelFactory.createDefaultModel();
            Resource r = x.createResource();
            List<RDFNode> javaList = new ListFromRDFList(r, RDF.type);

            List<RDFNode> items = Arrays.asList(
                    x.asRDFNode(NodeValue.makeInteger(0).asNode()),
                    x.createLiteral("middle"),
                    x.createResource("http://last.org/"));

            // Test add method (in contrast to addAll)
            for (RDFNode item : items) {
                javaList.add(item);
            }

            Assert.assertEquals(items, javaList);

            debugPrint(javaList);

            debugPrint(javaList.get(1));

            // Remove middle element
            {
                assertListMutation(javaList, list -> {
                    Iterator<?> it = iterator(list, isForward);
                    it.next();
                    it.next();
                    it.remove();
                    debugPrint("middle: " + list);
                });
            }


            // Remove first element
            {
                assertListMutation(javaList, list -> {
                    Iterator<?> it = iterator(list, isForward);
                    it.next();
                    it.remove();
                    debugPrint(list);
                });
            }

            // Prepend element
            {
                assertListMutation(javaList, list -> {
                    debugPrint("Prepend element to " + list);
                    list.add(0, x.createLiteral("first"));
                    debugPrint("After prepend: " + list);
                });
            }


            // Append element
            {
                assertListMutation(javaList, list -> {
                    list.add(2, x.createLiteral("last"));
                    debugPrint("Appended last: " + list);
                });
            }


            // Remove last
            {
                assertListMutation(javaList, list -> {
                    Iterator<?> it = iterator(list, isForward);
                    it.next();
                    it.next();
                    it.next();
                    it.remove();
                    debugPrint("Removed last: " + list);
                });
            }

            // Clear remaining
            {
                assertListMutation(javaList, list -> {
                    Iterator<?> it = list.iterator();
                    it.next();
                    it.remove();
                    it.next();
                    it.remove();
                    debugPrint(list);
                });
            }

            debugPrint("RESULT");
            // RDFDataMgr.write(System.out, x, RDFFormat.TURTLE_PRETTY);
        }
    }

    @Test
    public void testAddAll() {
        // Number of items to generate and pass to list.addAll()
        int n = 10; //000;

        Model x = ModelFactory.createDefaultModel();
        Resource r = x.createResource();
        List<RDFNode> javaList = new ListFromRDFList(r, RDF.type);
        RDFNode first = x.createLiteral("first");
        RDFNode last = x.createLiteral("last");
        List<RDFNode> values = IntStream.range(0, n)
                .mapToObj(i -> x.asRDFNode(NodeValue.makeInteger(i).asNode()))
                        .collect(Collectors.toList());
        boolean useAddAll = true;
        if (useAddAll) {
            javaList.add(first);
            javaList.add(last);
            javaList.addAll(1, values);
        } else {
            // This should have O(n^2) complexity and thus take very long
            javaList.add(first);
            for (RDFNode value : values) {
                javaList.add(value);
            }
            javaList.add(last);
        }

        debugPrint(javaList);

        int actualSize = javaList.size();
        Assert.assertEquals(n + 2, actualSize);
        Assert.assertEquals(first, javaList.get(0));
        Assert.assertEquals(last, javaList.get(javaList.size() - 1));
    }

    @Test
    public void testReverseListIterator() {
        List<Integer> values = Arrays.asList(1, 2, 3, 4, 5);

        Model x = ModelFactory.createDefaultModel();
        Resource r = x.createResource();
        List<RDFNode> backend = new ListFromRDFList(r, RDF.type);

        Converter<RDFNode, Integer> converter = Converter.from(
                rdfNode -> NodeValue.makeNode(rdfNode.asNode()).getInteger().intValue(),
                i -> x.asRDFNode(NodeValue.makeInteger(i).asNode()));

        List<Integer> intView = new ConvertingList<>(backend, converter);
        intView.addAll(values);

        assertListMutation(intView, list -> {
            ListIterator<Integer> it = ReverseListIterator.of(list.listIterator(list.size()));
            it.add(6);
            Assert.assertEquals(6, (int)it.previous());
            it.add(7);
            Assert.assertEquals(7, (int)it.previous());
            it.remove();
            Assert.assertEquals(6, (int)it.next());
            Assert.assertEquals(5, (int)it.next());
            Assert.assertEquals(5, (int)it.previous());
            Assert.assertEquals(6, (int)it.previous());
            // it.remove();

            debugPrint(list);
        });
    }

    @Test
    public void testReverseListIterator_AddAndPrevious() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        ListIterator<Integer> it = ReverseListIterator.of(list.listIterator(list.size()));
        it.add(6);
        Assert.assertEquals(6, (int)it.previous());
    }

    @Test
    public void testReverseListIterator_AddAndNext() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        ListIterator<Integer> it = ReverseListIterator.of(list.listIterator(list.size()));
        it.add(6);
        Assert.assertEquals(5, (int)it.next());
    }
}
