package org.aksw.jenax.arq.service.vfs;

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
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;

class PrefetchTask<T, I extends Iterator<T>>
    implements Runnable
{
    protected volatile I iterator;
    protected volatile List<T> bufferedItems;

    protected volatile boolean isStopRequested;

    // states: created, starting, running (= first item seen), terminated
    protected volatile String state = "created";

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

    // XXX Warn when read too much
    // XXX Consider stop when read too much
    @Override
    public void run() {
//        System.out.println(Thread.currentThread().getName() + " Task started " + this);
        state = "starting";
        while (!isStopRequested) {
            if (iterator.hasNext()) {
                state = "running";
                T item = iterator.next();
//                System.out.println(Thread.currentThread().getName() + "Buffering " + item + " in task " + this);
                bufferedItems.add(item);
            } else {
                break;
            }
        }
//        System.out.println(Thread.currentThread().getName() + "Task finished " + this);
        state = "terminated";
    }

    public void stop() {
//        System.out.println(Thread.currentThread().getName() + "Requested stop of task " + this);
        isStopRequested = true;
    }

    public static <T, I extends Iterator<T>> PrefetchTask<T, I> of(I iterator) {
        return new PrefetchTask<>(iterator);
    }
}

class Prefetch
    extends PrefetchTask<Binding, QueryIterator> {

    protected Binding id;

    public Prefetch(Binding id, QueryIterator iterator) {
        super(iterator);
        this.id = id;
    }

    @Override
    public String toString() {
        return "TaskId: " + id + " [" + state + (isStopRequested ? "aborted" : "") + "]: " + bufferedItems.size() + " items buffered.";
    }
}

record TaskEntry<T>(T task, Future<?> future) {}

/** This is a variant of {@link QueryIterRepeatApply} which consumes up to N elements from the input and
 *  schedules concurrent tasks that start buffering the related items for each input.
 *
 *  Whenever _this_ iterator advances to data of the next task, that task is stopped and a
 *  {@link QueryIterConcat} is formed between the buffered items and the remaining items.
 *  This means that at this point the remaining items of the current task are no longer loaded concurrently.
 *  However, all further tasks still run concurrently.
 *
 *  Buffers currently have unlimited size.
 */
public abstract class QueryIterRepeatApplyConcurrent
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
    private boolean isPoisonScheduled = false;

    public QueryIterRepeatApplyConcurrent(QueryIterator input, ExecutionContext execCxt, ExecutorService executorService, int maxConcurrentTasks) {
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

    /**
     *
     * @param binding
     * @param isolatedExecCxt A fresh execution context local to the executing thread.
     *           ExecutionContexts are not thread-safe (as of Jena 5.2.0) and
     *           concurrent access causes exceptions with at least the iterator tracking mechanism.
     * @return
     */
    protected abstract QueryIterator nextStage(Binding binding, ExecutionContext isolatedExecCxt);

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
        while (!isPoisonScheduled && taskQueue.remainingCapacity() > 0) {
            // A possible async cancel request is handled in scheduleNextTask
            scheduleNextTask();
        }
    }

    private synchronized void drainAndStopAllTasks() {
        if (drainedTasks == null) {
            drainedTasks = new ArrayList<>();
            taskQueue.drainTo(drainedTasks);
            drainedTasks = drainedTasks.stream().filter(item -> item != POISON).toList();

            drainedTasks.forEach(entry -> {
                Prefetch prefetch = entry.task();
                prefetch.stop();
                try {
                    entry.future().get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            schedulePoison();
        }
    }

    /**
     * Attempt to take the next item from the input and schedule a task from it.
     * If cancel was requested then POISON is scheduled instead.
     * POISON is also scheduled if the input is empty.
     */
    private void scheduleNextTask() {
        if (cancelRequested) {
            schedulePoison();
        } else {
            QueryIterator input = getInput();

            if (input.hasNext()) {
                count++;

                Binding binding = input.next();
//                System.err.println(String.format("Thread %s: Starting task for: %s", Thread.currentThread().getName(), binding));
                ExecutionContext execCxt = getExecContext();
                ExecutionContext isolatedExecCxt = new ExecutionContext(execCxt.getContext(), execCxt.getActiveGraph(), execCxt.getDataset(), execCxt.getExecutor());
                Prefetch  task = new Prefetch(binding, nextStage(binding, isolatedExecCxt));

                Future<?> future = executorService.submit(task::run);
                try {
                    taskQueue.put(new TaskEntry<>(task, future));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                schedulePoison();
                input.close(); // Probably not needed but was in the original code
            }
        }
    }

    private void schedulePoison() {
        if (!isPoisonScheduled) {
            isPoisonScheduled = true;
            taskQueue.add(POISON);
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
                prefetch.stop();
                // Wait for the task to complete
//                System.out.println(Thread.currentThread().getName() + " Waiting for future");
                entry.future().get();
//                System.out.println(Thread.currentThread().getName() + " Got future");

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

        drainAndStopAllTasks();

        drainedTasks.forEach(entry -> {
            entry.task().getIterator().close();
        });
    }

    @Override
    protected void requestSubCancel() {
        cancelRequested = true;

        if ( currentStage != null )
            currentStage.cancel();

        drainAndStopAllTasks();

        drainedTasks.forEach(entry -> {
            entry.task().getIterator().cancel();
        });
    }
}
