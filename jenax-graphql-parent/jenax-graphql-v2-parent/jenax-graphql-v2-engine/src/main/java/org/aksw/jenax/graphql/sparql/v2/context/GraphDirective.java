package org.aksw.jenax.graphql.sparql.v2.context;

import java.util.List;

/**
 * Intended uses:
 * <ul>
 *   <li>&#064;graph(iri: "http://foo") -&gt; GRAPH <http://foo> { }</li>
 *   <li>&#064;graph(var: "g") -&gt; GRAPH ?g { }</li>
 *   <li>Not yet supported: &#064;graph(iri: ["foo", "bar"]) -&gt; GRAPH ?allocVar { } FILTER(?allocVar IN (&lt;foo&gt;, &lt;bar&gt;))</li>
 * </ul>
 * var and iri would result in FILTER(var = iri)
 */
public class GraphDirective
    extends Cascadable
{
    protected String varName;
    protected List<String> graphIris;

    public GraphDirective(String varName, List<String> graphIris, boolean isSelf, boolean isCascade) {
        super(isSelf, isCascade);
        this.varName = varName;
        this.graphIris = graphIris;
    }

    public String getVarName() {
        return varName;
    }

    public List<String> getGraphIris() {
        return graphIris;
    }
}
