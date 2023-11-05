package org.aksw.jena_sparql_api.data_query.api;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.jenax.arq.util.expr.ExprListUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeUtils;

public interface DataQueryVarView<T extends RDFNode> {
    DataQuery<T> getDataQuery();
    // Var getStartVar();
    Resolver getResolver();


    DataQuery<T> filterUsing(Fragment relation, String ... attrNames);

    // Return the same data query with intersection on the given concept
    DataQuery<T> filter(Fragment1 concept);

    default DataQuery<T> filter(String exprStr) {
        Expr expr = ExprUtils.parse(exprStr);
        return filter(expr);
    }
    DataQuery<T> filter(Expr expr);

    default DataQuery<T> only(Iterable<Node> nodes) {
        Expr e = new E_OneOf(new ExprVar(Vars.s), ExprListUtils.nodesToExprs(nodes));
        return filter(new Concept(new ElementFilter(e), Vars.s));
    }

    default DataQuery<T> only(Node ... nodes) {
        return only(Arrays.asList(nodes));
    }

    default DataQuery<T> only(RDFNode ... rdfNodes) {
        return only(Arrays.asList(rdfNodes).stream().map(RDFNode::asNode).collect(Collectors.toList()));
    }

    default DataQuery<T> only(String ... iris) {
        return only(NodeUtils.convertToListNodes(Arrays.asList(iris)));
    }



    default DataQuery<T> exclude(Iterable<Node> nodes) {
        Expr e = new E_NotOneOf(new ExprVar(Vars.s), ExprListUtils.nodesToExprs(nodes));
        return filter(new Concept(new ElementFilter(e), Vars.s));
    }

    default DataQuery<T> exclude(Node ... nodes) {
        return exclude(Arrays.asList(nodes));
    }

    default DataQuery<T> exclude(RDFNode ... rdfNodes) {
        return exclude(Arrays.asList(rdfNodes).stream().map(RDFNode::asNode).collect(Collectors.toList()));
    }

    default DataQuery<T> exclude(String ... iris) {
        return exclude(NodeUtils.convertToListNodes(Arrays.asList(iris)));
    }
}
