package org.aksw.jena_sparql_api.arq.service.vfs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter1;
import org.apache.jena.sparql.engine.iterator.QueryIterConcat;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

class PrefetchTask<T, I extends Iterator<T>>
    implements Runnable
{
    protected I iterator;
    protected List<T> bufferedItems;

    protected volatile boolean isAbortRequested;

    public PrefetchTask(I iterator) {
        super();
        this.iterator = iterator;
        this.bufferedItems = new ArrayList<>(1024);
    }

    public List<T> getBufferedItems() {
        return bufferedItems;
    }

    public I getIterator() {
        return iterator;
    }

    // TODO Warn when read too much
    // TODO Consider stop when read too much
    @Override
    public void run() {
        while (!isAbortRequested && iterator.hasNext()) {
            T item = iterator.next();
            bufferedItems.add(item);
        }
    }

    public void abort() {
        isAbortRequested = true;
    }

    public static <T, I extends Iterator<T>> PrefetchTask<T, I> of(I iterator) {
        return new PrefetchTask<>(iterator);
    }
}

class Prefetch
    extends PrefetchTask<Binding, QueryIterator> {

    public Prefetch(QueryIterator iterator) {
        super(iterator);
    }

    public static PrefetchTask<Binding, QueryIterator> of(QueryIterator iterator) {
        return new PrefetchTask<>(iterator);
    }
}

record TaskEntry<T>(T task, Future<?> future) {}

public abstract class QueryIterConcurrentSimple
    extends QueryIter1
{
    private int count = 0;
    private QueryIterator currentStage;
    private volatile boolean cancelRequested = false;

    private final ExecutorService executorService;
    private final BlockingQueue<TaskEntry<Prefetch>> taskQueue;

    // Upon cancel or close the taskQueue is prevented from new items and the existing once
    // are drained to this list
    private List<TaskEntry<Prefetch>> drainedTasks;

    private static final TaskEntry<Prefetch> POISON = new TaskEntry<>(null, null);

    public QueryIterConcurrentSimple(QueryIterator input, ExecutionContext execCxt, ExecutorService executorService, int maxConcurrentTasks) {
        super(input, execCxt);
        this.currentStage = null;
        this.executorService = executorService;
        this.taskQueue = new LinkedBlockingQueue<>(maxConcurrentTasks);

        if ( input == null ) {
            Log.error(this, "[QueryIterConcurrentSimple] Repeated application to null input iterator");
            return;
        }
    }

    protected QueryIterator getCurrentStage() {
        return currentStage;
    }

    protected abstract QueryIterator nextStage(Binding binding);

    @Override
    protected boolean hasNextBinding() {
        if ( isFinished() )
            return false;

        for ( ;; ) {
            if ( currentStage == null )
                currentStage = makeNextStage();

            if ( currentStage == null )
                return false;

            if ( cancelRequested )
                // Pass on the cancelRequest to the active stage.
                performRequestCancel(currentStage);

            if ( currentStage.hasNext() )
                return true;

            // finish this step
            currentStage.close();
            currentStage = null;
            // loop
        }
        // Unreachable
    }

    @Override
    protected Binding moveToNextBinding() {
        if ( !hasNextBinding() )
            throw new NoSuchElementException(Lib.className(this) + ".next()/finished");
        return currentStage.nextBinding();

    }

    private synchronized void scheduleNextTasks() {
        while (taskQueue.remainingCapacity() > 0) {
            scheduleNextTask();
        }
    }

    private void drainTasks() {
        if (drainedTasks != null) {
            drainedTasks = new ArrayList<>();
            taskQueue.drainTo(drainedTasks);
        }
    }

    private void scheduleNextTask() {
        if (cancelRequested) {
            drainTasks();
            taskQueue.add(POISON);
        } else {
            QueryIterator input = getInput();
//
//            if (!getInput().hasNext()) {
//                getInput().close();
//                return null;
//            }

            if (input.hasNext()) {
                count++;

                Binding binding = input.next();
                System.err.println(String.format("Thread %s: Starting task for: %s", Thread.currentThread().getName(), binding));
                Prefetch  task = new Prefetch(nextStage(binding));

                Future<?> future = executorService.submit(task::run);
                taskQueue.offer(new TaskEntry<>(task, future));
            } else {
                taskQueue.add(POISON);
                input.close(); // Probably not needed but was in the original code
            }
        }
    }

    private QueryIterator getNextStage() {
        QueryIterConcat result;
        try {
            TaskEntry<Prefetch> entry = taskQueue.take();
            if (entry == POISON) {
                result = null;
            } else {
                Prefetch prefetch = entry.task();
                // Send the abort signal
                prefetch.abort();
                // Wait for the task to complete
                entry.future().get();

                ExecutionContext execCxt = getExecContext();
                result = new QueryIterConcat(execCxt);
                result.add(QueryIterPlainWrapper.create(prefetch.getBufferedItems().iterator(), execCxt));
                result.add(prefetch.getIterator());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private QueryIterator makeNextStage() {
        scheduleNextTasks();
        QueryIterator iter = getNextStage();
        return iter;
    }

    @Override
    protected void closeSubIterator() {
        if ( currentStage != null )
            currentStage.close();

        drainTasks();

        drainedTasks.forEach(entry -> {
            Prefetch prefetch = entry.task();
            prefetch.abort();
            try {
                entry.future().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            prefetch.getIterator().close();
        });
    }

    @Override
    protected void requestSubCancel() {
        cancelRequested = true;

        if ( currentStage != null )
            currentStage.cancel();

        drainTasks();

        drainedTasks.forEach(entry -> {
            Prefetch prefetch = entry.task();
            prefetch.abort();
            try {
                entry.future().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            prefetch.getIterator().cancel();
        });

//        while (true) {
//            try {
//                taskQueue.put(POISON);
//                break;
//            } catch (InterruptedException e) {
//                // Force the poison onto the queue
//            }
//        }
    }
}

/*
private QueryIterator makeNextStage() {
    count++;

    if ( getInput() == null )
        return null;

    if ( !getInput().hasNext() ) {
        getInput().close();
        return null;
    }

    Binding binding = getInput().next();
    QueryIterator iter = nextStage(binding);
    return iter;
}
*/

//@Override
//public boolean hasNext() {
//  while (!currentStage.hasNext() && !taskQueue.isEmpty()) {
//      try {
//          // Retrieve the result of the first task that completes
//          Future<List<R>> completedTask = taskQueue.poll();
//          List<R> relatedItems = completedTask.get(); // blocking until the task completes
//          currentRelatedIterator = relatedItems.iterator();
//
//          // Schedule the next task
//          scheduleNextTask();
//      } catch (InterruptedException | ExecutionException e) {
//          e.printStackTrace();
//          Thread.currentThread().interrupt();
//      }
//  }
//  return currentRelatedIterator.hasNext();
//}

//@Override
//public R next() {
//  if (!hasNext()) {
//      throw new NoSuchElementException();
//  }
//  return currentRelatedIterator.next();
//}
//
//public void shutdown() {
//  executor.shutdown();
//}
//
//public static void main(String[] args) {
//  // Example usage
//  List<String> items = Arrays.asList("item1", "item2", "item3");
//
//  // FlatMap function to simulate related items
//  Function<String, List<String>> flatMapFunction = item -> {
//      // Simulate related item retrieval, e.g., adding suffix
//      return Arrays.asList(item + "-related1", item + "-related2");
//  };
//
//  TaskScheduler<String, String> scheduler = new TaskScheduler<>(items.iterator(), flatMapFunction, 2);
//
//  while (scheduler.hasNext()) {
//      System.out.println(scheduler.next());
//  }
//
//  scheduler.shutdown();
//}
