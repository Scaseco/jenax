package org.aksw.jenax.store.qlever.assembler;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class QleverAssemblerVocab {

    public static final Resource Dataset = ResourceFactory.createResource(QleverAssemblerTerms.Dataset);

    public static final Property location = ResourceFactory.createProperty(QleverAssemblerTerms.location);
    public static final Property indexName = ResourceFactory.createProperty(QleverAssemblerTerms.indexName);
    public static final Property port = ResourceFactory.createProperty(QleverAssemblerTerms.port);
    public static final Property accessToken = ResourceFactory.createProperty(QleverAssemblerTerms.accessToken);

    public static final Property numSimultaneousQueries = ResourceFactory.createProperty(QleverAssemblerTerms.numSimultaneousQueries); // Integer
    public static final Property memoryMaxSize = ResourceFactory.createProperty(QleverAssemblerTerms.memoryMaxSize);
    public static final Property cacheMaxSize = ResourceFactory.createProperty(QleverAssemblerTerms.cacheMaxSize);
    public static final Property cacheMaxSizeSingleEntry = ResourceFactory.createProperty(QleverAssemblerTerms.cacheMaxSizeSingleEntry);
    public static final Property lazyResultMaxCacheSize = ResourceFactory.createProperty(QleverAssemblerTerms.lazyResultMaxCacheSize);
    public static final Property cacheMaxNumEntries = ResourceFactory.createProperty(QleverAssemblerTerms.cacheMaxNumEntries); // Long
    public static final Property noPatterns = ResourceFactory.createProperty(QleverAssemblerTerms.noPatterns); // Boolean
    public static final Property noPatternTrick = ResourceFactory.createProperty(QleverAssemblerTerms.noPatternTrick); // Boolean

    public static final Property text = ResourceFactory.createProperty(QleverAssemblerTerms.text);  // Boolean
    public static final Property onlyPsoAndPosPermutations = ResourceFactory.createProperty(QleverAssemblerTerms.onlyPsoAndPosPermutations); // Boolean
    public static final Property defaultQueryTimeout = ResourceFactory.createProperty(QleverAssemblerTerms.defaultQueryTimeout);
    public static final Property serviceMaxValueRows = ResourceFactory.createProperty(QleverAssemblerTerms.serviceMaxValueRows); // Long
    public static final Property throwOnUnboundVariables = ResourceFactory.createProperty(QleverAssemblerTerms.throwOnUnboundVariables); // Boolean
}
