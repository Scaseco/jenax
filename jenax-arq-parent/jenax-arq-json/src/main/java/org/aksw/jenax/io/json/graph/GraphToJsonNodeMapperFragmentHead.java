package org.aksw.jenax.io.json.graph;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.util.direction.Direction;
import org.aksw.jenax.arq.util.triple.TripleFilter;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.io.json.accumulator.AggJsonFragmentHead;
import org.aksw.jenax.io.json.accumulator.AggJsonNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Path0;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class GraphToJsonNodeMapperFragmentHead
    extends GraphToJsonEdgeMapper
{
    public GraphToJsonNodeMapperFragmentHead(TripleFilter baseFilter) {
        super(baseFilter);
        this.targetNodeMapper = new GraphToJsonNodeMapperFragmentBody();
    }

    public static GraphToJsonNodeMapperFragmentHead of(P_Path0 basicPath) {
        return of(basicPath.getNode(), Direction.ofFwd(basicPath.isForward()));
    }

    public static GraphToJsonNodeMapperFragmentHead of(Node node, boolean isForward) {
        return of(node, Direction.ofFwd(isForward));
    }

//    public static GraphToJsonNodeMapperFragment of(Node node, boolean isForward, GraphToJsonNodeMapperObject mapper) {
//        TripleFilter baseFilter = TripleFilter.create(Vars.s, node, isForward);
//        GraphToJsonNodeMapperFragment result = new GraphToJsonNodeMapperFragment(baseFilter);
//        result.targetNodeMapper = mapper;
//        return result;
//    }

    public static GraphToJsonNodeMapperFragmentHead of(Node predicate, Direction direction) {
        TripleFilter baseFilter = TripleFilter.create(Vars.s, predicate, direction.isForward());
        return new GraphToJsonNodeMapperFragmentHead(baseFilter);
    }

    public TripleFilter getBaseFilter() {
        return baseFilter;
    }

    public GraphToJsonNodeMapperFragmentHead setBaseFilter(TripleFilter baseFilter) {
        this.baseFilter = baseFilter;
        return this;
    }

    @Override
    public GraphToJsonNodeMapperObjectLike getTargetNodeMapper() {
        return (GraphToJsonNodeMapperObjectLike)targetNodeMapper;
    }

    public GraphToJsonNodeMapperFragmentHead setTargetNodeMapper(GraphToJsonNodeMapperObjectLike targetNodeMapper) {
        this.targetNodeMapper = targetNodeMapper;
        return this;
    }

    @Override
    public JsonElement map(PathJson path, JsonArray errors, Graph graph, Node node) {
        throw new RuntimeException("Mapper for fragments not implemented");
//        // Always generate a json array first. If the schema allows for a single value then extract
//        // the only value from the array.
//        JsonArray tmp = null;
//
//        // Node pNode = ps.getNode();
//        boolean isForward = baseFilter.isForward();
//        TripleFilter filter = baseFilter.bindSource(node);
//        if (filter != null) {
//            Triple pattern = filter.getTriplePattern();
//            Iterator<Triple> it = graph.find(pattern);
//
//            try {
//                int i = 0;
//                while (it.hasNext()) {
//                    PathJson subPath = path.resolve(Step.of(i));
//                    Triple triple = it.next();
//
//                    Boolean filterResult = filter.evalExpr(triple);
//                    if (Boolean.TRUE.equals(filterResult)) {
//                        // Node s = TripleUtils.getSource(triple, isForward);
//                        Node o = TripleUtils.getTarget(triple, isForward);
//
//                        JsonElement contrib = targetNodeMapper.map(subPath, errors, graph, o);
//                        if (tmp == null) {
//                            tmp = new JsonArray();
//                        }
//                        tmp.add(contrib);
//                    }
//                    ++i;
//                }
//            } finally {
//                Iter.close(it);
//            }
//        }
//
//        JsonElement result;;
//        if (tmp == null) {
//            result = JsonNull.INSTANCE;
//        } else {
//            if (isUniqueLang || maxCount == 1 || single) {
//                int arraySize = tmp.size();
//                if (arraySize == 0) {
//                    result = JsonNull.INSTANCE;
//                } else if (arraySize == 1) {
//                    result = tmp.get(0);
//                } else {
//                    // TODO more than 1 item, raise warning or error
//                    result = tmp;
//                }
//            } else {
//                result = tmp;
//            }
//        }
//        return result;
    }

    @Override
    public String toString() {
        return "PropertyMapper [baseFilter=" + baseFilter + ", targetNodeMapper=" + targetNodeMapper + "]";
    }

    public AggJsonFragmentHead toAggregator(Node jsonKey) {
        AggJsonNode targetAgg = targetNodeMapper.toAggregator();

        // It should be fairly easy to extend AggJsonProperty such that the TripleFilter can be passed to it
        // It just hasn't happened

        if (baseFilter.getExprs() != null) {
            throw new UnsupportedOperationException("Expressions are not yet supported.");
        }

        Triple t = baseFilter.getTriplePattern();
        Node p = t.getPredicate();
        if (!p.isConcrete()) {
            throw new UnsupportedOperationException("Predicate must be concrete");
        }
        if (t.getSubject().isConcrete() || t.getObject().isConcrete()) {
            throw new UnsupportedOperationException("Subject and/or object must nont be variables");
        }

        boolean isForward = baseFilter.isForward();

        AggJsonFragmentHead result = AggJsonFragmentHead.of(jsonKey, p, isForward, targetAgg);
        // TODO uniqueLang and maxCount act as filters for the matcher
        // they don't directly translate to the accumulator - so what to do with them?
        // We could have the accumulator materialize the values and then pick the best one
        // Or we assume that the input to the accumulator is already pre-filtered

        // boolean isSingle = isUniqueLang || (maxCount >= 0 && maxCount <= 1);
        // result.setSingle(single);

        return result;
    }
}
