package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.function.FixpointIteration;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.model.udf.api.InverseDefinition;
import org.aksw.jenax.model.udf.api.UdfDefinition;
import org.aksw.jenax.model.udf.api.UserDefinedFunctionResource;
import org.aksw.jenax.model.udf.util.UserDefinedFunctions;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.function.user.ExprTransformExpand;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;

public class MainUdfTest2 {
    public static void main(String[] args) {
        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        SparqlStmtMgr.execSparql(model, "udf-inferences.rq");

        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

        Set<String> profiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/jena"));
        Map<String, UserDefinedFunctionDefinition> map = UserDefinedFunctions.load(model, profiles);
        UserDefinedFunctions.registerAll(map);

        Expr e = new E_Function("http://ns.aksw.org/function/decodeBnodeIri", new ExprList(new ExprVar(Vars.x)));
        ExprTransform xform = new ExprTransformExpand(map);
        e = FixpointIteration.apply(100, e, x -> ExprTransformer.transform(xform, x));

        UserDefinedFunctionResource udf = model.createResource("http://ns.aksw.org/function/skolemizeBnodeLabel").as(UserDefinedFunctionResource.class);
        Set<UdfDefinition> defs = udf.getDefinitions().stream().filter(x -> x.getProfiles().stream().anyMatch(y -> y.toString().contains("jena"))).collect(Collectors.toSet());

        UdfDefinition def = defs.iterator().next();
        UserDefinedFunctionResource alias = def.getAliasFor();


        Set<InverseDefinition> invs = alias.getDefinitions().iterator().next().getInverses();
        InverseDefinition invDef = invs.iterator().next();

        UserDefinedFunctionResource invFn = invDef.getFunction();
        Set<UdfDefinition> invDefs = invFn.getDefinitions();
        UdfDefinition iDef = invDefs.iterator().next();
        String expr = iDef.getExpr();

        System.out.println("INVERSE: " + expr);



//                System.out.println("INVERSES: " + model.createResource("http://ns.aksw.org/function/skolemizeBnodeLabel")
//                    .as(UserDefinedFunctionResource.class)
//                    .getDefinitions().iterator().next()
//                    .getInverses()
//                    .iterator().next()
//                    .getFunction()
//                    .getDefinitions().iterator().next()
//                    .getExpr()
//                );

        // System.out.println("INVERSE EXPR: " + invExpr);

//		NodeValue x = ExprTransformVirtualBnodeUris.eval("http://ns.aksw.org/function/str", NodeValue.makeInteger(666));
        System.out.println(e);

//		System.out.println(model.size());
    }
}
