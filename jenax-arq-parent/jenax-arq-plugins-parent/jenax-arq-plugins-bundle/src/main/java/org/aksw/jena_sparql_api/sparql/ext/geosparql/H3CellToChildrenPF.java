package org.aksw.jena_sparql_api.sparql.ext.geosparql;

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
import org.apache.sedona.common.utils.H3Utils;

import java.util.Iterator;
import java.util.List;

public class H3CellToChildrenPF extends PFuncSimpleAndList {

    private static final int CELL_ID_POS = 0;
    private static final int LEVEL_POS = 1;

    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        List<Node> objectArgs = argObject.getArgList();
        if (objectArgs.size() != 2  ) {
            throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": 2 arguments required: cell ID, level of resolution");
        }

        if (!subject.isVariable()) {
            throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": subject must be variable.");
        }
        Var s = Var.alloc(subject);

        Node cellIdLit = argObject.getArg(CELL_ID_POS);
        long cellId = NodeValue.makeNode(cellIdLit).getInteger().longValue();

        Node levelNode = argObject.getArg(LEVEL_POS);
        NodeValue levelNodeVal = NodeValue.makeNode(levelNode);
        if (!levelNodeVal.isInteger()) {
            throw new ExprEvalException("\"level of resolution\" argument not an integer: " + FmtUtils.stringForNode(levelNodeVal.asNode()));
        }
        int level = levelNodeVal.getInteger().intValue();


        // sanity check child resolution > current cell resolution
        int cellResolution = H3Utils.h3.getResolution(cellId);
        if (level <= cellResolution) {
            throw new ExprEvalException(String.format(
                    "\"children resolution\" must be greater than cell resolution %d: " + FmtUtils.stringForNode(levelNodeVal.asNode()),
                    cellResolution)
                    );
        }

        List<Long> childrenIds = H3Utils.h3.cellToChildren(cellId, level);
        Iterator<Binding> iterator = childrenIds.stream()
                .map(NodeFactoryExtra::intToNode)
                .map(node -> BindingFactory.binding(binding, s, node))
                .iterator();

        return QueryIterPlainWrapper.create(iterator, execCxt);
    }
}