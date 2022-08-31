package org.aksw.jena_sparql_api.sparql.ext.array;

import java.util.List;
import java.util.Optional;

import org.aksw.jena_sparql_api.sparql.ext.geosparql.PropFuncArgUtils;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.node.NodeList;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.IterLib;

/**
 * Assign array components to a given list of variables.
 *
 * ?arr array:explode (?component1 ... ?componentN)
 *
 *
 */
public class PF_ArrayExplode
    extends PropertyFunctionBase
{
    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
            ExecutionContext execCxt) {
        Optional<BindingBuilder> bb;

        // This pf's subject component must evaluate concrete array
        Node s = BindingUtils.getValue(binding, argSubject.getArg());
        if (s != null && s.isLiteral() && s.getLiteralValue() instanceof NodeList) {
            bb = Optional.of(BindingBuilder.create(binding));
            NodeList nodes = (NodeList)s.getLiteralValue();
            List<Node> os = PropFuncArgUtils.getAsList(argObject);
            bb = Optional.of(BindingBuilder.create(binding));
            int n = nodes.size();
            for (int i = 0; i < n; ++i) {
                final int index = i;
                bb = BindingUtils.processArg(bb, os, i, () -> {
                    Node r = index < n ? nodes.get(index) : null;
                    return r;
                });
            }
        } else {
            bb = Optional.empty();
        }

        QueryIterator result = bb.isPresent()
                ? IterLib.result(bb.get().build(), execCxt)
                : IterLib.noResults(execCxt);
        return result;
    }

}
