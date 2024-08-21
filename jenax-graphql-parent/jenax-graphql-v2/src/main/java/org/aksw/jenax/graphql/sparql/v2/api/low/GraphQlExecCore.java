package org.aksw.jenax.graphql.sparql.v2.api.low;

public interface GraphQlExecCore
    extends AutoCloseable
{
    /**
     * Whether this provider is expected to yield at most 1 result.
     * The client can use this information to e.g. omit starting an array in the output.
     * However, the data provider may not necessarily know whether this information is truthful,
     * because it may rely on user provided mapping information: For example, a field might be tagged
     * with &#064one but during execution bindings amounting to multiple values are encountered;
     * If a violation is encountered during runtime then an exception should be raised.
     */
    boolean isSingle();

    void abort();

    @Override
    void close();
}
