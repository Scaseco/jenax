package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.sedona.common.Functions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class H3GeometryToCellIDsPF extends PFuncSimpleAndList {

    private static final int GEOM_POS = 0;
    private static final int LEVEL_POS = 1;
    private static final int FULL_COVER_POS = 2;


    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        List<Node> objectArgs = argObject.getArgList();
        if (objectArgs.size() != 3) {
            throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": 3 arguments required: geometry, level of resolution, full cover of polygon");
        }

        if (!subject.isVariable()) {
            throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": subject must be variable.");
        }
        Var s = Var.alloc(subject);

        Node geomLit = argObject.getArg(GEOM_POS);

        Node levelNode = argObject.getArg(LEVEL_POS);
        NodeValue levelNodeVal = NodeValue.makeNode(levelNode);
        if (!levelNodeVal.isInteger()) {
            throw new ExprEvalException("\"level of resolution\" argument not an integer: " + FmtUtils.stringForNode(levelNodeVal.asNode()));
        }
        int level = levelNodeVal.getInteger().intValue();

        Node fullCoverNode = argObject.getArg(FULL_COVER_POS);
        NodeValue fullCoverNodeVal = NodeValue.makeNode(fullCoverNode);
        if (!fullCoverNodeVal.isBoolean()) {
            throw new ExprEvalException("\"full cover\" flag not a boolean: " + FmtUtils.stringForNode(fullCoverNodeVal.asNode()));
        }
        boolean fullCover = fullCoverNodeVal.getBoolean();


        GeometryWrapper geomWrapper = GeometryWrapper.extract(geomLit);

        Long[] ids = Functions.h3CellIDs(geomWrapper.getParsingGeometry(), level, fullCover);
        Iterator<Binding> iterator = Arrays.stream(ids)
                .map(NodeFactoryExtra::intToNode)
                .map(node -> BindingFactory.binding(binding, s, node))
                .iterator();

        return QueryIterPlainWrapper.create(iterator, execCxt);
    }
}