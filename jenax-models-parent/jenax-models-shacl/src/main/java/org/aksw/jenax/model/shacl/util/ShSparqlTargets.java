package org.aksw.jenax.model.shacl.util;

import org.aksw.jenax.model.shacl.domain.HasPrefixes;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.PrefixMap;
import org.topbraid.shacl.model.SHSPARQLTarget;
import org.topbraid.shacl.model.impl.SHSPARQLTargetImpl;

public class ShSparqlTargets {

    /**
     * Returns null if the target is not a sparql target.
     * Otherwise return the parsed sparql query, thereby raising an exception on failure.
     *
     * This method is simpler version of jena's TargetExtensions.sparqlTargetType without
     * shacl parameter support.
     */
    public static Query tryParseSparqlQuery(Resource extraTarget) {
        SHSPARQLTargetImpl sparqlTarget = (SHSPARQLTargetImpl)extraTarget.as(SHSPARQLTarget.class);

        Query result = null;
        String queryString = sparqlTarget.getSPARQL();
        if (queryString != null) {
            HasPrefixes prefixes = extraTarget.as(HasPrefixes.class);
            PrefixMap pm = PrefixSetUtils.collect(prefixes);
            result = new Query();
            if (prefixes != null) {
                result.getPrefixMapping().setNsPrefixes(pm.getMapping());
            }
            QueryFactory.parse(result, queryString, null, Syntax.syntaxARQ);
        }
        return result;
    }

}
