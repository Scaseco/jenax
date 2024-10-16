package org.aksw.jenax.io.json.graph;

import org.aksw.jenax.arq.util.triple.TripleFilter;
import org.aksw.jenax.io.json.accumulator.AggJsonEdge;
import org.apache.jena.sparql.path.P_Path0;

public abstract class GraphToJsonEdgeMapper
    implements GraphToJsonMapper
{
    protected TripleFilter baseFilter;
    protected GraphToJsonMapperNode targetNodeMapper = GraphToJsonNodeMapperLiteral.get();

    /**
     * Only applicable if the value produced by this PropertyMapper is a json object.
     * If hidden is true, then the owning NodeMapper should merge the produced json object into
     * its own json object.
     */
    protected boolean isHidden = false;

    public GraphToJsonEdgeMapper(TripleFilter baseFilter) {
        this.baseFilter = baseFilter;
    }

    public abstract GraphToJsonMapperNode getTargetNodeMapper();

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public abstract AggJsonEdge toAggregator(P_Path0 jsonKey);

}
