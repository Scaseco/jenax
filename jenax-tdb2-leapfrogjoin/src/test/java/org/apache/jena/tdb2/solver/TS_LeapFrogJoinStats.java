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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TS_LeapFrogJoinStats {

    @Test
    public void testStatsInitialValues() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();
        
        assertEquals(0, stats.getSeekCount());
        assertEquals(0, stats.getStepCount());
        assertEquals(0, stats.getMergeSuccessCount());
        assertEquals(0, stats.getMergeFailCount());
        assertEquals(0, stats.getHeapRebuildCount());
        assertEquals(0, stats.getHeapifyCount());
        assertEquals(0, stats.getIndexCacheHits());
        assertEquals(0, stats.getIndexCacheMisses());
        assertEquals(0, stats.getIterations());
        assertEquals(0, stats.getTotalComparisons());
    }

    @Test
    public void testIncrementMethods() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();
        
        // Use reflection to call package-private increment methods
        try {
            stats.getClass().getDeclaredMethod("incrementSeekCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementStepCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementMergeSuccessCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementMergeFailCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementHeapRebuildCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementHeapifyCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementIndexCacheHits").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementIndexCacheMisses").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementIterations").invoke(stats);
            
            assertEquals(1, stats.getSeekCount());
            assertEquals(1, stats.getStepCount());
            assertEquals(1, stats.getMergeSuccessCount());
            assertEquals(1, stats.getMergeFailCount());
            assertEquals(1, stats.getHeapRebuildCount());
            assertEquals(1, stats.getHeapifyCount());
            assertEquals(1, stats.getIndexCacheHits());
            assertEquals(1, stats.getIndexCacheMisses());
            assertEquals(1, stats.getIterations());
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke increment methods", e);
        }
    }

    @Test
    public void testAddComparisons() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();
        
        try {
            stats.getClass().getDeclaredMethod("addComparisons", long.class)
                .invoke(stats, 5L);
            stats.getClass().getDeclaredMethod("addComparisons", long.class)
                .invoke(stats, 3L);
            
            assertEquals(8, stats.getTotalComparisons());
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke addComparisons", e);
        }
    }

    @Test
    public void testSeekRatio() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();
        
        // No advances yet - should return NaN
        assertTrue(Double.isNaN(stats.getSeekRatio()));
        
        try {
            // 3 seeks, 1 step = 75% seek ratio
            for (int i = 0; i < 3; i++) {
                stats.getClass().getDeclaredMethod("incrementSeekCount").invoke(stats);
            }
            stats.getClass().getDeclaredMethod("incrementStepCount").invoke(stats);
            
            double ratio = stats.getSeekRatio();
            assertTrue(ratio >= 0.74 && ratio <= 0.76, "Seek ratio should be ~0.75");
        } catch (Exception e) {
            throw new RuntimeException("Failed to test seek ratio", e);
        }
    }

    @Test
    public void testCacheHitRatio() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();
        
        // No lookups yet - should return NaN
        assertTrue(Double.isNaN(stats.getCacheHitRatio()));
        
        try {
            // 4 hits, 1 miss = 80% hit ratio
            for (int i = 0; i < 4; i++) {
                stats.getClass().getDeclaredMethod("incrementIndexCacheHits").invoke(stats);
            }
            stats.getClass().getDeclaredMethod("incrementIndexCacheMisses").invoke(stats);
            
            double ratio = stats.getCacheHitRatio();
            assertTrue(ratio >= 0.79 && ratio <= 0.81, "Cache hit ratio should be ~0.8");
        } catch (Exception e) {
            throw new RuntimeException("Failed to test cache hit ratio", e);
        }
    }

    @Test
    public void testGetTotalAdvances() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();
        
        try {
            for (int i = 0; i < 5; i++) {
                stats.getClass().getDeclaredMethod("incrementSeekCount").invoke(stats);
            }
            for (int i = 0; i < 3; i++) {
                stats.getClass().getDeclaredMethod("incrementStepCount").invoke(stats);
            }
            
            assertEquals(8, stats.getTotalAdvances());
        } catch (Exception e) {
            throw new RuntimeException("Failed to test total advances", e);
        }
    }

    @Test
    public void testGetTotalMerges() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();
        
        try {
            for (int i = 0; i < 10; i++) {
                stats.getClass().getDeclaredMethod("incrementMergeSuccessCount").invoke(stats);
            }
            for (int i = 0; i < 2; i++) {
                stats.getClass().getDeclaredMethod("incrementMergeFailCount").invoke(stats);
            }
            
            assertEquals(12, stats.getTotalMerges());
        } catch (Exception e) {
            throw new RuntimeException("Failed to test total merges", e);
        }
    }

    @Test
    public void testToString() {
        LeapFrogJoinStats stats = new LeapFrogJoinStats();
        
        try {
            stats.getClass().getDeclaredMethod("incrementSeekCount").invoke(stats);
            stats.getClass().getDeclaredMethod("incrementMergeSuccessCount").invoke(stats);
            
            String str = stats.toString();
            assertTrue(str.contains("seekCount=1"));
            assertTrue(str.contains("mergeSuccess=1"));
            assertTrue(str.contains("LeapFrogJoinStats"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to test toString", e);
        }
    }
}
