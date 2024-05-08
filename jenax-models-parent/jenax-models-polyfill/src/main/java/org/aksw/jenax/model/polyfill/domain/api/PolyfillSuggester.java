package org.aksw.jenax.model.polyfill.domain.api;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.PolymorphicOnly;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.model.rdfs.domain.api.HasRdfsComment;
import org.aksw.jenax.model.rdfs.domain.api.HasRdfsLabel;
import org.apache.jena.rdf.model.Resource;

// Unfinished. Towards an RDF model to capture polyfill suggestion rules.
@Namespace("https://w3id.org/aksw/norse#polyfill.")
@RdfType
@ResourceView
public interface PolyfillSuggester
    extends HasRdfsLabel, HasRdfsComment // HasKeywords
{
    @Override
    PolyfillSuggester setLabel(String label);

    @Override
    PolyfillSuggester setComment(String label);

    @IriNs
    PolyfillCondition getCondition();
    PolyfillSuggester setCondition(Resource condition);

    @IriNs
    @PolymorphicOnly
    Resource getSuggestion();
    PolyfillSuggester setSuggestion(Resource suggestion);

    /**
     * If enabled by default is not explicitly specified then the following rules apply:
     *
     * If a condition is present, then the effective value is true.
     * Otherwise false.
     */
    @IriNs
    Boolean isEnabledByDefault();
    PolyfillSuggester setEnabledByDefault(Boolean value);

    /** A default priority value for this suggester. Suggesters are usually run in order of this value. */
    @IriNs
    Integer getLevel();
    PolyfillSuggester setLevel(Integer level);
}
