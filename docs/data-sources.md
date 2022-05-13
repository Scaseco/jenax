
RdfDataSource: A supplier for RDFConnection instances. This interface is meant for implementing clients; i.e. there is no close method because the server-component is managed elsewhere. Obtained connections of course have to be closed though!

RdfDataEngine: A subclass of RdfDataSource that provides a close() method. Implementations of this interface can be considered servers that can be shut down.

QueryExecutionFactoryQuery: Connection-agnostic query execution. Interface supplies QueryExecution instances for a Query. May or may not use backing connections / transactions.
Typical use case is fetching on-demand information where no transaction is needed, such as using multiple requests for fetching a large number of labels for display in a UI.

This interface does not provide transaction handling, however, it can be backed by an transaction:
```java
try (RDFConnection conn = rdfDataSource.getConnection()) {
  Txn.execute(conn, () -> {
    QueryExecutionFactory qef = new QueryExecutionFactorySparqlQueryConnection(conn);

    // Let components run queries on the qef - those components do not need to worry about transactions
    runQueries1(qef);
    runQueries2(qef);
  });

}

```




