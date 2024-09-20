package org.aksw.jenax.model.udf.api;

import java.util.List;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.model.shacl.domain.ShHasPrefixes;
import org.aksw.jenax.norse.term.udf.NorseTermsUdf;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface UdfDefinition
    extends ShHasPrefixes
{
    @Iri(NorseTermsUdf.expr)
    String getExpr();

    @Iri(NorseTermsUdf.params)
    List<String> getParams();

    /**
     * Function definitions can be associated with profiles.
     * This allows loading a function if <b>any</b> of the profiles is active.
     * (I.e. the set of profiles is disjunctive)
     *
     * @return
     */
    @Iri(NorseTermsUdf.profile)
    Set<Resource> getProfiles();

    @Iri(NorseTermsUdf.inverse)
    Set<InverseDefinition> getInverses();

    /**
     * Definition that refers to another function for macro-expansion under the given profiles
     *
     * @return
     */
    @Iri(NorseTermsUdf.aliasFor)
    UserDefinedFunctionResource getAliasFor();

    UdfDefinition setAliasFor(Resource r);


    /**
     * True means, that the function is realized using a property function with the same name
     *
     *
     * @return
     */
    @Iri(NorseTermsUdf.mapsToPropertyFunction)
    Boolean mapsToPropertyFunction();

    // @Iri("http://ns.aksw.org/jena/udf/definition")
    // boolean mapsToPropertyFunction;
    // <T> Set<T> getProfiles(Class<T> x);
}


