package org.aksw.jenax.graphql.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Assemble a result from a collection of async contributions.
 * A small wrapper around guava's Futures API.
 * <p />
 * Example:
 * <pre>
 * {@code
 *   ListenableFuture<Y> future =
 *    AsyncCombiner.of(executorService, combineXsIntoY)
 *     .addTask(a).addTask(b)
 *     .exec() }
 * </pre>
 */
public class AsyncCombiner<I, O> {
    protected ListeningExecutorService executorService;
    protected Function<List<I>, O> combiner;
    protected List<Callable<I>> tasks = new ArrayList<>();

    protected AsyncCombiner(ListeningExecutorService executorService, Function<List<I>, O> combiner) {
        super();
        this.executorService = Objects.requireNonNull(executorService, "executorService must not be null");
        this.combiner = Objects.requireNonNull(combiner, "combiner must not be null");
    }

    public static <I, O> AsyncCombiner<I, O> of(ExecutorService executorService, Function<List<I>, O> combiner) {
        return of (MoreExecutors.listeningDecorator(executorService), combiner);
    }

    public static <I, O> AsyncCombiner<I, O> of(ListeningExecutorService executorService, Function<List<I>, O> combiner) {
        return new AsyncCombiner<>(executorService, combiner);
    }

    public AsyncCombiner<I, O> addTask(Callable<I> task) {
        tasks.add(task);
        return this;
    }

    public ListenableFuture<O> exec() {
        List<ListenableFuture<I>> futures = tasks.stream()
                .map(executorService::submit).collect(Collectors.toList());
        ListenableFuture<O> result = Futures.transform(
                Futures.allAsList(futures), combiner::apply, executorService);
        return result;
    }
}
