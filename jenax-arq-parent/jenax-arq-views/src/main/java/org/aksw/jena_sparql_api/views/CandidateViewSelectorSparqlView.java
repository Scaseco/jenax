package org.aksw.jena_sparql_api.views;

import java.util.List;

import org.aksw.sparqlify.database.FilterPlacementOptimizer2;
import org.aksw.sparqlify.sparqlview.OpSparqlViewPattern;
import org.aksw.sparqlify.sparqlview.ViewInstanceJoin;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CandidateViewSelectorSparqlView
    extends CandidateViewSelectorBase<SparqlView, Void>
{
    private static final Logger logger = LoggerFactory.getLogger(CandidateViewSelectorSparqlView.class);


    public CandidateViewSelectorSparqlView() {
        super((op, rm) -> FilterPlacementOptimizer2.optimizeStatic(op, rm));
    }

    @Override
    public Op createOp(OpQuadBlock op, List<RecursionResult<SparqlView, Void>> conjunctions) {

        //ViewInstanceJoin<SparqlView> conjunctions = item.get

        OpDisjunction result = OpDisjunction.create();

        for(RecursionResult<SparqlView, Void> entry : conjunctions) {
            ViewInstanceJoin<SparqlView> item = entry.getViewInstances();
            Op tmp = new OpSparqlViewPattern(item);
            result.add(tmp);
        }

        return result;
    }

}