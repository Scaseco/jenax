package org.aksw.jena_sparql_api.sparql.ext.prefix;

import java.util.Optional;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.expr.NodeValue;

public class E_PrefixAbbrev
    extends FunctionPrefixBase
{
    @Override
    public boolean isValidArg(NodeValue arg) {
        return arg.isIRI();
    }

    @Override
    public NodeValue process(PrefixMap prefixMap, String str) {
        return Optional.ofNullable(prefixMap.abbreviate(str)).map(NodeValue::makeString).orElse(null);
    }
}
