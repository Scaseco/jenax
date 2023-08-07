package org.aksw.facete.v3.api;

import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ScopedTreeQueryImpl
    implements ScopedTreeQuery
{
    protected BiMap<Var, ScopedTreeQueryNode> varToRoot = HashBiMap.create();

    @Override
    public ScopedTreeQueryNode root(VarScope scope) {
        return null;
    }
}
