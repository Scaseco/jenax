package org.aksw.jenax.model.udf.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.norse.term.udf.NorseTermsUdf;
import org.apache.jena.rdf.model.Resource;

public interface InverseDefinition
    extends Resource
{
    @Iri(NorseTermsUdf.ofParam)
    String getParam();
    InverseDefinition setParam(String param);

    @Iri(NorseTermsUdf.ofFunction)
    UserDefinedFunctionResource getFunction();
    InverseDefinition setFunction(Resource fn);
}
