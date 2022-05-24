package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.sparql.ext.url.PropertyFunctionFactoryUrlText;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyFunctionFactoryXmlParse
    implements PropertyFunctionFactory {

	private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryUrlText.class);

    @Override
    public PropertyFunction create(final String uri)
    {
        return new PFuncSimple()
        {

            @Override
            public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object,
                    org.apache.jena.sparql.engine.ExecutionContext execCtx) {

                // Get the subject's value
                Node node = subject.isVariable()
                        ? binding.get((Var)subject)
                        : subject;

                if(!object.isVariable()) {
                    throw new RuntimeException("Object of json array splitting must be a variable");
                }
                Var outputVar = (Var)object;

                List<Binding> bindings;
                try {
                	NodeValue nv = E_XmlParse.resolve(NodeValue.makeNode(node));

                    bindings = Collections.singletonList(BindingFactory.binding(binding, outputVar, nv.asNode()));

                } catch(Exception e) {
                	logger.warn("Error resolving node as URI: " + node, e);

                	bindings = Collections.emptyList();
                }

                QueryIterator result = QueryIterPlainWrapper.create(bindings.iterator());
                return result;
            }
        };
    }
}