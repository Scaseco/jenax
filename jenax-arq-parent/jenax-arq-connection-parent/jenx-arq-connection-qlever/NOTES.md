
```
docker run -it --rm -u "$(id -u):$(id -g)" -v "$(pwd):/data" -w "/data" adfreiburg/qlever:latest "ServerMain -h"
```

```
Options for ServerMain:
  -h [ --help ]                         Produce this help message.
  -i [ --index-basename ] arg           The basename of the index files 
                                        (required).
  -p [ --port ] arg                     The port on which HTTP requests are 
                                        served (required).
  -a [ --access-token ] arg             Access token for restricted API calls 
                                        (default: no access).
  -j [ --num-simultaneous-queries ] arg (=1)
                                        The number of queries that can be 
                                        processed simultaneously.
  -m [ --memory-max-size ] arg (=4 GB)  Limit on the total amount of memory 
                                        that can be used for query processing 
                                        and caching. If exceeded, query will 
                                        return with an error, but the engine 
                                        will not crash.
  -c [ --cache-max-size ] arg (=30 GB)  Maximum memory size for all cache 
                                        entries (pinned and not pinned). Note 
                                        that the cache is part of the total 
                                        memory limited by --memory-max-size.
  -e [ --cache-max-size-single-entry ] arg (=5 GB)
                                        Maximum size for a single cache entry. 
                                        That is, results larger than this will 
                                        not be cached unless pinned.
  -E [ --lazy-result-max-cache-size ] arg (=5 MB)
                                        Maximum size up to which lazy results 
                                        will be cached by aggregating partial 
                                        results. Caching does cause significant
                                        overhead for this case.
  -k [ --cache-max-num-entries ] arg (=1000)
                                        Maximum number of entries in the cache.
                                        If exceeded, remove least-recently used
                                        non-pinned entries from the cache. Note
                                        that this condition and the size limit 
                                        specified via --cache-max-size both 
                                        have to hold (logical AND).
  -P [ --no-patterns ]                  Disable the use of patterns. If 
                                        disabled, the special predicate 
                                        `ql:has-predicate` is not available.
  -T [ --no-pattern-trick ]             Maximum number of entries in the cache.
                                        If exceeded, remove least-recently used
                                        entries from the cache if possible. 
                                        Note that this condition and the size 
                                        limit specified via --cache-max-size-gb
                                        both have to hold (logical AND).
  -t [ --text ]                         Also load the text index. The text 
                                        index must have been built before using
                                        `IndexBuilderMain` with options `-d` 
                                        and `-w`.
  -o [ --only-pso-and-pos-permutations ] 
                                        Only load the PSO and POS permutations.
                                        This disables queries with predicate 
                                        variables.
  -s [ --default-query-timeout ] arg (=30s)
                                        Set the default timeout in seconds 
                                        after which queries are 
                                        cancelledautomatically.
  -S [ --service-max-value-rows ] arg (=10000)
                                        The maximal number of result rows to be
                                        passed to a SERVICE operation as a 
                                        VALUES clause to optimize its 
                                        computation.
  --throw-on-unbound-variables arg (=0) If set to true, the queries that use 
                                        GROUP BY, BIND, or ORDER BY with 
                                        variables that are unbound in the query
                                        throw an exception. These queries 
                                        technically are allowed by the SPARQL 
                                        standard, but typically are the result 
                                        of typos and unintended by the user
```
