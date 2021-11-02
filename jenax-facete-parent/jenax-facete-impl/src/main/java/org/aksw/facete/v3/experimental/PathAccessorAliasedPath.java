package org.aksw.facete.v3.experimental;

import org.aksw.facete.v3.api.AliasedPath;
import org.apache.jena.sparql.path.P_Path0;

public class PathAccessorAliasedPath
    extends PathAccessorPath<AliasedPath>
{
    @Override
    public AliasedPath getParent(AliasedPath path) {
        return path.getParent();
    }

    @Override
    public P_Path0 getLastStep(AliasedPath path) {
        return path.getLastStep().getKey();
    }

    @Override
    public String getAlias(AliasedPath path) {
        return path.getLastStep().getValue();
    }
}
