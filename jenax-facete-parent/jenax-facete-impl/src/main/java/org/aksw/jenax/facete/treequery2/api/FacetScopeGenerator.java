package org.aksw.jenax.facete.treequery2.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.sparql.core.Var;

import com.google.common.base.Converter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Pattern of generated variable names: '_scope_pathname_origvar_' Underscores in scope and path are doubled for escaping.
 */
public class FacetScopeGenerator {


    protected ScopeNode root;

    protected Generator<String> scopeNameGenerator;

    // protected TreeDataMap<Path<String>, FacetScopeGenerator> scopeHierarchy;
    protected Table<String, FacetPath, ScopeNode> scopeToPathToName = HashBasedTable.create();

    public FacetScopeGenerator(Var rootVar) {
        super();
        this.root = new ScopeNode("", rootVar);
    }



    public ScopeNode getOrCreateScope(ScopeNode parent, FacetPath facetPath) {
        String baseName = parent.getScopeName();
        ScopeNode result = scopeToPathToName.row(baseName).computeIfAbsent(facetPath, fp -> {
            String newName = scopeNameGenerator.next();
            Var parentVar = parent.getParentTargetVar();
            ScopeNode r = new ScopeNode(parent, newName, parentVar);
            return r;
        });
        return result;
    }

    public Converter<Var, Var> getConverter() {
        return null;
    }


}
