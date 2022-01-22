package org.aksw.jenax.arq.util.binding;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jenax.arq.util.node.NodeTransformRenameMap;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.util.ModelUtils;

public class QuerySolutionUtils {

    /** Applys a node transform to the variables acting as the keys of the query soluton.
     *
     *  At present always returns a new QuerySolutionMap which retains the original RDFNodes and their models.
     *  In principle if the argument is a
     *  ResultBinding we could use node transform on the Binding, but unfortunately
     *  ResultBinding does not provide a public method to retrieve the model */
    public static QuerySolution applyNodeTransformToKeys(NodeTransform nodeTransform, QuerySolution qs) {
        QuerySolutionMap result = new QuerySolutionMap();

        Iterator<String> itVarNames = qs.varNames();

        while(itVarNames.hasNext()) {
            String varName = itVarNames.next();

            RDFNode rdfNode = qs.get(varName);

            Var sourceVar = Var.alloc(varName);
            Var targetVar = (Var)nodeTransform.apply(sourceVar);
            if(targetVar == null) {
                targetVar = sourceVar;
            }

            String targetVarName = targetVar.getVarName();
            result.add(targetVarName, rdfNode);
        }

        return result;
    }


    public static QuerySolution renameKeys(QuerySolution qs, Map<Var, Var> varMap) {
        return applyNodeTransformToKeys(NodeTransformRenameMap.create(varMap), qs);
    }

    
    /**
     * See {@link #newGraphAwareBindingMapper(Dataset, Map)}
     */
	public static Function<Binding, QuerySolution> newGraphAwareBindingMapper(Dataset dataset, String nodeVarName, String graphVarName) {
		return newGraphAwareBindingMapper(dataset, Collections.singletonMap(Var.alloc(nodeVarName), Var.alloc(graphVarName)));
	}
	
    /**
     * By default, jena places all rdf nodes in query solutions in the default graph. This prohibits changing information
     * of a resource in its originating graph.
     * 
     * This mapper allows to specify a mapping where a variable is mapped to another one whose values represents graph names.
     * If a variable is mapped to {@code null} then the default graph is used.
     * 
     * 
     * @param dataset
     * @param nodeVarToGraphVar
     * @return
     */
	public static Function<Binding, QuerySolution> newGraphAwareBindingMapper(Dataset dataset, Map<Var, Var> nodeVarToGraphVar) {
		return binding -> {
			QuerySolutionMap qs = new QuerySolutionMap();
			binding.vars().forEachRemaining(nodeVar -> {
				Var graphVar = nodeVarToGraphVar.get(nodeVar);
				
				String nodeVarName = nodeVar.getName();

				Node graphNode = graphVar == null ? Quad.defaultGraphIRI : binding.get(graphVar);
				
				Graph graph = dataset.asDatasetGraph().getGraph(graphNode);
				Model model = ModelFactory.createModelForGraph(graph);
				RDFNode rdfNode = ModelUtils.convertGraphNodeToRDFNode(graphNode, model);
				qs.add(nodeVarName, rdfNode);
			});
			return qs;
		};
	}

}
