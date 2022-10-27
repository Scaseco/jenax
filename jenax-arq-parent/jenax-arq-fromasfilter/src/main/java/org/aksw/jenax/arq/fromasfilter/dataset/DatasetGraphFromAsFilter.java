package org.aksw.jenax.arq.fromasfilter.dataset;

import java.util.Map;

import org.aksw.jenax.arq.fromasfilter.engine.QueryEngineFactoryFromAsFilter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.expr.Expr;

/** This wrapper does not contain any custom logic. It is just a marker that
 * gets picked up by {@link QueryEngineFactoryFromAsFilter}. */
public class DatasetGraphFromAsFilter
    extends DatasetGraphWrapper
{
    protected Map<String, Expr> graphToExpr;

    public DatasetGraphFromAsFilter(DatasetGraph dsg, Map<String, Expr> graphToExpr) {
        super(dsg);
        this.graphToExpr = graphToExpr;
    }

    public Map<String, Expr> getGraphToExpr() {
        return graphToExpr;
    }
}
