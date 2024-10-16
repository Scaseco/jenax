package org.aksw.jenax.model.udf.api;

import java.util.List;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.norse.term.udf.NorseTermsUdf;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface UserDefinedFunctionResource
    extends Resource
{
    /**
     * Get a simple definition of the function in form of a list of strings.
     * The first item is the SPARQL expression string whereas the remaining elements are the parameter
     * variable names.
     *
     * @return
     */
    @Iri(NorseTermsUdf.simpleDefinition)
    List<String> getSimpleDefinition();

    @Iri(NorseTermsUdf.definition)
    Set<UdfDefinition> getDefinitions();
}
