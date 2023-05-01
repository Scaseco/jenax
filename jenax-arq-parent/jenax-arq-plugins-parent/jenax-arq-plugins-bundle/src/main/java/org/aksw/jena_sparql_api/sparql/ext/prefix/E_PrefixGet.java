package org.aksw.jena_sparql_api.sparql.ext.prefix;

import java.util.Optional;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.expr.NodeValue;

public class E_PrefixGet
    extends FunctionPrefixBase
{
    @Override
    public boolean isValidArg(NodeValue arg) {
        return arg.isString();
    }

    @Override
    public NodeValue process(PrefixMap prefixMap, String str) {
        return Optional.ofNullable(prefixMap.get(str)).map(NodeFactory::createURI).map(NodeValue::makeNode).orElse(null);
    }
}
