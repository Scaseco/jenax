package org.aksw.commons.allocation.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.aksw.commons.allocation.api.Allocatable;
import org.aksw.commons.allocation.api.AllocationLostException;

public class Allocations {

    public static <T> T runOnce(
            Callable<T> callable,
            Allocatable ... allocatables) throws Exception {
        return runOnce(callable, Arrays.asList(allocatables), Collections.emptyList());
    }

    public static <T> T runOnce(
            Callable<T> callable,
            Iterable<Allocatable> allocatables,
            Iterable<Allocatable> deallocatables) throws Exception {
        T result;

        // Allocate all resources
        for (Allocatable allocatable : allocatables) {
            allocatable.allocate();
        }

        // Attempt to run the action
        try {
            result = callable.call();
        } catch (Exception e) {

            // If running the action fails then check for whether any resources were lost
            for (Allocatable allocatable : allocatables) {
                boolean isAllocated = allocatable.isAllocated();
                if (!isAllocated) {
                    throw new AllocationLostException(e);
                }
            }

            throw new RuntimeException(e);
        } finally {
            for (Allocatable deallocatable : deallocatables) {
                try {
                    deallocatable.deallocate();
                } catch (Exception e) {
                    // TODO Collect them in a kind of CompositeException
                    e.printStackTrace();
                }
            }
        }


        return result;
    }
}
