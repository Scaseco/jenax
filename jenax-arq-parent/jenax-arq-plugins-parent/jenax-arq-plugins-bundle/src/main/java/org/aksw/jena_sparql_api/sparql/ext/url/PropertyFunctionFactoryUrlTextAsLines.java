package org.aksw.jena_sparql_api.sparql.ext.url;

import com.google.gson.Gson;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.itr.Itr;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.rdfhdt.hdt.iterator.utils.Iter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <uri> uri:resolve ?output
 *
 * @author raven
 *
 */
public class PropertyFunctionFactoryUrlTextAsLines
    implements PropertyFunctionFactory {


	private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryUrlTextAsLines.class);


    protected Gson gson;

    public PropertyFunctionFactoryUrlTextAsLines() {
        this(new Gson());
    }

    public PropertyFunctionFactoryUrlTextAsLines(Gson gson) {
        super();
        this.gson = gson;
    }

    @Override
    public PropertyFunction create(final String uri)
    {
        return new PFuncSimple()
        {

            @Override
            public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object,
                    ExecutionContext execCtx) {

                // Get the subject's value
                Node node = subject.isVariable()
                        ? binding.get((Var)subject)
                        : subject;

                if(!object.isVariable()) {
                    throw new RuntimeException("Object of json array splitting must be a variable");
                }
                Var outputVar = (Var)object;

                Iterator<Binding> bindingIterator;
                try {
                    Iterator<NodeValue> nodeIterator = JenaUrlUtils.resolveAsLines(NodeValue.makeNode(node), execCtx);
                    bindingIterator = Iter.map(nodeIterator, nv -> BindingFactory.binding(binding, outputVar, nv.asNode()));

                } catch(Exception e) {
                	logger.warn("Error resolving node as URI: " + node, e);

                    bindingIterator = Itr.iter0();
                }

//                List<Binding> bindings;
//                try {
//                	List<NodeValue> nodeValues = JenaUrlUtils.resolveAsLines(NodeValue.makeNode(node), execCtx);
//
//                    bindings = nodeValues.stream()
//                            .map(nv -> BindingFactory.binding(binding, outputVar, nv.asNode()))
//                            .collect(Collectors.toList());
//
//                } catch(Exception e) {
//                	logger.warn("Error resolving node as URI: " + node, e);
//
//                	bindings = Collections.emptyList();
//                }

                QueryIterator result = QueryIterPlainWrapper.create(bindingIterator);
                return result;
            }
        };
    }
}