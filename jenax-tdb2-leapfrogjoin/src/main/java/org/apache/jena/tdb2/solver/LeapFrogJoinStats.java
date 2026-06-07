/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.tdb2.solver;

/**
 * Statistics for leap frog join execution, following the Guava Cache Stats pattern.
 * <p>
 * This class tracks performance metrics for a single leap frog join execution,
 * including seek operations, merge operations, and index cache usage.
 * <p>
 * The stats object is mutable and is updated by the {@link LeapFrogJoinIteratorOptimized}
 * during query execution. Once the iterator is closed, the stats reflect the final
 * execution metrics and are no longer updated.
 */
public class LeapFrogJoinStats {

    // Core metrics: seek vs. step operations
    private long seekCount = 0;
    private long stepCount = 0;
    
    // Merge operations: successful joins vs. conflicts
    private long mergeSuccessCount = 0;
    private long mergeFailCount = 0;
    
    // Index cache performance
    private long indexCacheHits = 0;
    private long indexCacheMisses = 0;
    
    // Loop and comparison metrics
    private long iterations = 0;
    private long totalComparisons = 0;

    /**
     * Increment the seek count (B+Tree seek operations).
     */
    void incrementSeekCount() {
        seekCount++;
    }

    /**
     * Increment the step count (sequential advance fallback).
     */
    void incrementStepCount() {
        stepCount++;
    }

    /**
     * Increment the merge success count (successful join results).
     */
    void incrementMergeSuccessCount() {
        mergeSuccessCount++;
    }

    /**
     * Increment the merge fail count (binding conflicts).
     */
    void incrementMergeFailCount() {
        mergeFailCount++;
    }

    /**
     * Increment the index cache hit count.
     */
    void incrementIndexCacheHits() {
        indexCacheHits++;
    }

    /**
     * Increment the index cache miss count.
     */
    void incrementIndexCacheMisses() {
        indexCacheMisses++;
    }

    /**
     * Increment the iteration count (main loop iterations).
     */
    void incrementIterations() {
        iterations++;
    }

    /**
     * Add to the total comparison count.
     */
    void addComparisons(long count) {
        totalComparisons += count;
    }

    /**
     * Get the number of B+Tree seek operations.
     */
    public long getSeekCount() {
        return seekCount;
    }

    /**
     * Get the number of sequential advance (step) operations.
     */
    public long getStepCount() {
        return stepCount;
    }

    /**
     * Get the number of successful merge operations (join results produced).
     */
    public long getMergeSuccessCount() {
        return mergeSuccessCount;
    }

    /**
     * Get the number of failed merge operations (binding conflicts).
     */
    public long getMergeFailCount() {
        return mergeFailCount;
    }

    /**
     * Get the number of index cache hits.
     */
    public long getIndexCacheHits() {
        return indexCacheHits;
    }

    /**
     * Get the number of index cache misses.
     */
    public long getIndexCacheMisses() {
        return indexCacheMisses;
    }

    /**
     * Get the total number of main loop iterations.
     */
    public long getIterations() {
        return iterations;
    }

    /**
     * Get the total number of binding comparisons performed.
     */
    public long getTotalComparisons() {
        return totalComparisons;
    }

    /**
     * Get the seek ratio (seeks / total advances).
     * <p>
     * A higher ratio indicates better use of B+Tree seek optimization.
     * Returns NaN if no advances have been performed.
     *
     * @return seek ratio between 0.0 and 1.0, or NaN if no advances
     */
    public double getSeekRatio() {
        long total = seekCount + stepCount;
        if (total == 0) {
            return Double.NaN;
        }
        return (double) seekCount / total;
    }

    /**
     * Get the index cache hit ratio.
     * <p>
     * A higher ratio indicates better index cache utilization.
     * Returns NaN if no cache lookups have been performed.
     *
     * @return cache hit ratio between 0.0 and 1.0, or NaN if no lookups
     */
    public double getCacheHitRatio() {
        long total = indexCacheHits + indexCacheMisses;
        if (total == 0) {
            return Double.NaN;
        }
        return (double) indexCacheHits / total;
    }

    /**
     * Get the total number of advances (seeks + steps).
     */
    public long getTotalAdvances() {
        return seekCount + stepCount;
    }

    /**
     * Get the total number of merge operations.
     */
    public long getTotalMerges() {
        return mergeSuccessCount + mergeFailCount;
    }

    /**
     * Get a human-readable summary of the statistics.
     */
    @Override
    public String toString() {
        return "LeapFrogJoinStats{" +
                "seekCount=" + seekCount +
                ", stepCount=" + stepCount +
                ", seekRatio=" + String.format("%.3f", getSeekRatio()) +
                ", mergeSuccess=" + mergeSuccessCount +
                ", mergeFail=" + mergeFailCount +
                ", cacheHits=" + indexCacheHits +
                ", cacheMisses=" + indexCacheMisses +
                ", cacheHitRatio=" + String.format("%.3f", getCacheHitRatio()) +
                ", iterations=" + iterations +
                ", comparisons=" + totalComparisons +
                '}';
    }
}
