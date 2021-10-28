package org.aksw.jenax.arq.util.binding;

import java.util.Iterator;
import java.util.Map;

import org.aksw.jenax.arq.util.node.NodeTransformRenameMap;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;

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

}
