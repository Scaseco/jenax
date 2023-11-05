package org.aksw.facete.v3.api;

import java.util.Set;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;

class FacetPathScope {

}

public class FacetPathContext
    implements FacetPathMapping
{
    /** So far allocated mappings */
    protected BiMap<FacetPath, Var> pathToVar;

    /** A name used to disambiguate the same variable name at different levels of nesting. */
    protected String scopeName;

    /** The root variable the path started from. */
    protected Var rootVar;

    /** An absolute path against which to resolve relative paths.*/
    protected FacetPath basePath;

    protected Set<Var> forbiddenVars;

    /** Generator for names that are used to build variables */
    protected Generator<String> nameGenerator;

    public FacetPathContext(String scopeName, Var rootVar, FacetPath basePath, Set<Var> forbiddenVars) {
        super();
        this.scopeName = scopeName;
        this.rootVar = rootVar;
        this.basePath = basePath;
        this.forbiddenVars = forbiddenVars;
    }

    @Override
    public Var allocate(FacetPath facetPath) {
        return pathToVar.computeIfAbsent(facetPath, fp -> {
            return null;
//            String localName = GeneratorBlacklist.create(nameGenerator, forbiddenVars).next();
//            Var r = Var.alloc(scopeName)
        });
    }




}
