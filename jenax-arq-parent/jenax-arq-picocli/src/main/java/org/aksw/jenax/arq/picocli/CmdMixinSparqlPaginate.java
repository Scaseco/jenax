package org.aksw.jenax.arq.picocli;

import java.io.Serializable;

import picocli.CommandLine.Option;

public class CmdMixinSparqlPaginate
    implements Serializable
    // Use paginated retrieval strategy
    // --page:size    1000
    // --page:shrink  true
{
    private static final long serialVersionUID = 1L;

    @Option(names = { "--limit" }, description="Force this result set limit on all SPARQL queries.")
    public Long queryLimit = null;

    @Option(names = { "--paginate" }, description="Work around remote result set limits by means of paginated SPARQL query execution with the given page size.")
    public Long queryPageSize = null;
}
