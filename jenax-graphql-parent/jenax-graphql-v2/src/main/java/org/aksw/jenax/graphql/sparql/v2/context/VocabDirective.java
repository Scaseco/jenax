package org.aksw.jenax.graphql.sparql.v2.context;

/**
 * Intended use:
 * <ul>
 *   <li>myField &#064;vocab(iri: "http://foo/") -&gt; Unless overridden, this field
 *   and all children are interpreted as having the annotation &#064;rdf(iri: vocab + fieldName).
 *   In this example, the field would have the implicit annotation:
 *   &#064;rdf(iri: "http://foo/myField")</li>
 * </ul>
 *
 *
 */
public class VocabDirective
//    extends Cascadable // Always cascades and affects self
{
    protected String iri;

    public VocabDirective(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }
}
