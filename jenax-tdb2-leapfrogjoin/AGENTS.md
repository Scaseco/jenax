# Leap Frog Join Development Guidelines

## TDB2 Graph Unwrapping Pattern

When working with TDB2 datasets in tests or direct iterator construction, use `TDBInternal` utilities to unwrap wrapped dataset graphs:

```java
import org.apache.jena.tdb2.sys.TDBInternal;
import org.apache.jena.tdb2.store.DatasetGraphTDB;

// Get the underlying DatasetGraphTDB from a potentially wrapped dataset
DatasetGraph dsg = dataset.asDatasetGraph();
DatasetGraphTDB tdbDsg = TDBInternal.getDatasetGraphTDB(dsg);

// Access the GraphTDB directly from DatasetGraphTDB
GraphTDB graphTDB = (GraphTDB) tdbDsg.getDefaultGraph();
```

**Key points:**
- `dataset.asDatasetGraph()` may return a wrapped `DatasetGraph` (e.g., `DatasetGraphSwitchable`)
- `TDBInternal.getDatasetGraphTDB(dsg)` unwraps to the underlying `DatasetGraphTDB`
- The default graph from `DatasetGraphTDB` may also be wrapped (e.g., `GraphViewSwitchable`)
- Cast to `GraphTDB` after getting it from `DatasetGraphTDB.getDefaultGraph()`

**Example in tests:**
```java
Dataset dataset = TDB2Factory.createDataset();
dataset.begin(ReadWrite.WRITE);
try {
    // Load test data
    Graph dataGraph = SSE.parseGraph(dataSSE);
    dataGraph.find().forEachRemaining(triple -> 
        dataset.asDatasetGraph().getDefaultGraph().add(triple));
    dataset.commit();
} finally {
    dataset.end();
}

// For direct iterator construction
DatasetGraph dsg = dataset.asDatasetGraph();
DatasetGraphTDB tdbDsg = TDBInternal.getDatasetGraphTDB(dsg);
LeapFrogJoinIteratorOptimized iterator = StageGeneratorLeapFrogJoin
    .createLeapFrogIterator(bgp, tdbDsg, execCxt);
```

## SSE Test Data Format

Use SSE (Simple Syntactic Expression) for concise test data construction:

**Test data (graph):**
```java
String dataSSE = "(graph " +
    "(:s1 :p1 :o1a) (:s1 :p2 :o2a) (:s1 :p3 :o3a) " +
    "(:s2 :p1 :o1b) (:s2 :p2 :o2b) (:s2 :p3 :o3b) " +
    ")";
Graph dataGraph = SSE.parseGraph(dataSSE);
```

**BGP patterns:**
```java
String bgpSSE = "(bgp (?s :p1 ?o1) (?s :p2 ?o2) (?s :p3 ?o3))";
BasicPattern bgp = SSE.parseBGP(bgpSSE);
```

**Prefix handling:**
- `:` prefix expands to `http://example/` (empty prefix mapping)
- No need to explicitly declare prefix mappings in SSE

## Transaction Patterns

Always use try-with-resources or explicit begin/commit/abort patterns:

```java
dataset.begin(ReadWrite.WRITE);
try {
    // ... operations ...
    dataset.commit();
} catch (RuntimeException e) {
    dataset.abort();
    throw e;
} finally {
    dataset.end();
}
```

## Stats Tracking

Stats are tracked in `LeapFrogJoinStats` and integrated into `LeapFrogJoinIteratorOptimized`:

- Stats object is created once per iterator lifecycle
- Stats are updated during iteration (mutable counters)
- Access stats via `iterator.getStats()` after iteration completes
- Stats include: seeks, steps, merges, heap operations, cache hits/misses, iterations, comparisons

**Note:** `seekCount + stepCount` may be 0 if all iterators start aligned (all patterns return same subject on first iteration).

## Utility Methods

`StageGeneratorLeapFrogJoin` provides public utility methods:

```java
// Create leap frog iterator directly (for testing)
LeapFrogJoinIteratorOptimized iterator = StageGeneratorLeapFrogJoin
    .createLeapFrogIterator(bgp, tdbDsg, execCxt);

// Extract join variables from BGP
List<Var> joinVars = StageGeneratorLeapFrogJoin.findJoinVariables(triples);

// Get variables in a single triple
Set<Var> tripleVars = StageGeneratorLeapFrogJoin.tripleVars(triple);
```
