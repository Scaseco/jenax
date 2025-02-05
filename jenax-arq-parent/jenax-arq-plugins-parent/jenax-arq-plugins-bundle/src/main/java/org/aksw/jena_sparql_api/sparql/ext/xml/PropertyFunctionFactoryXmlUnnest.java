package org.aksw.jena_sparql_api.sparql.ext.xml;

import javax.xml.xpath.XPathFactory;

import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * {
 *    Bind("['foo', 'bar']"^^xsd:json As ?json)
 *    ?json json:array ?items.
 * }
 * </pre>
 *
 * @author raven
 *
 */
public class PropertyFunctionFactoryXmlUnnest
    implements PropertyFunctionFactory {

    private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryXmlUnnest.class);
    protected XPathFactory xPathFactory;


    public PropertyFunctionFactoryXmlUnnest() {
        this(XPathFactory.newInstance());
    }

    public PropertyFunctionFactoryXmlUnnest(XPathFactory xPathFactory) {
        super();
        this.xPathFactory = xPathFactory;
    }

    @Override
    public PropertyFunction create(final String uri)
    {
        return new PFuncSimpleAndList() {

            @Override
            public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg object, ExecutionContext execCxt) {
                // Get the subject's value
                Node node = BindingUtils.getValue(binding, subject);

                if(object.getArgListSize() != 2) {
                    throw new RuntimeException("property function for xpath evaluation requires two arguments");
                }

                Node xpathNode = object.getArg(0);
                Node outputNode = object.getArg(1);

                if(!outputNode.isVariable()) {
                    throw new RuntimeException("Object of xml array splitting must be a variable");
                }
                Var outputVar = (Var)outputNode;

                return JenaXmlUtils.evalXPath(xPathFactory, binding, execCxt, node, xpathNode, outputVar);
            }
        };
    }
}
