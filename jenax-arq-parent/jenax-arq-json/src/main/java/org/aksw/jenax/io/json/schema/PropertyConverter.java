package org.aksw.jenax.io.json.schema;

import java.util.Iterator;

import org.aksw.commons.util.direction.Direction;
import org.aksw.jenax.arq.util.triple.TripleFilter;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Path0;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class PropertyConverter
    implements RdfToJsonConverter
{
    // protected String name;
    protected TripleFilter baseFilter;
    protected NodeConverter targetNodeSchema = NodeConverterLiteral.get();

    protected boolean isUniqueLang = false;
    protected int maxCount = -1;

    /**
     * Only applicable if the value produced by this PropertyConverter is a json object.
     * If hidden is true, then the owning NodeConverter should merge the produced json object into
     * its own json object.
     */
    protected boolean isHidden = false;

    public PropertyConverter(TripleFilter baseFilter) {
        super();
        this.baseFilter = baseFilter;
    }

    public static PropertyConverter of(P_Path0 basicPath) {
        return of(basicPath.getNode(), Direction.ofFwd(basicPath.isForward()));
    }

    public static PropertyConverter of(Node predicate, Direction direction) {
        TripleFilter baseFilter = TripleFilter.create(Vars.s, predicate, direction.isForward());
        return new PropertyConverter(baseFilter);
    }

    public TripleFilter getBaseFilter() {
        return baseFilter;
    }

    public PropertyConverter setBaseFilter(TripleFilter baseFilter) {
        this.baseFilter = baseFilter;
        return this;
    }

    public NodeConverter getTargetNodeSchema() {
        return targetNodeSchema;
    }

    public PropertyConverter setTargetNodeConverter(NodeConverter targetNodeSchema) {
        this.targetNodeSchema = targetNodeSchema;
        return this;
    }

    public Boolean isUniqueLang() {
        return isUniqueLang;
    }

    public PropertyConverter setUniqueLang(boolean isUniqueLang) {
        this.isUniqueLang = isUniqueLang;
        return this;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public PropertyConverter setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    @Override
    public JsonElement convert(Graph graph, Node node) {
        // Always generate a json array first. If the schema allows for a single value then extract
        // the only value from the array.
        JsonArray tmp = null;

        // Node pNode = ps.getNode();
        boolean isForward = baseFilter.isForward();
        TripleFilter filter = baseFilter.bindSource(node);
        if (filter != null) {
            Triple pattern = filter.getTriplePattern();
            Iterator<Triple> it = graph.find(pattern);

            try {
                while (it.hasNext()) {
                    Triple triple = it.next();

                    Boolean filterResult = filter.evalExpr(triple);
                    if (Boolean.TRUE.equals(filterResult)) {
                        // Node s = TripleUtils.getSource(triple, isForward);
                        Node o = TripleUtils.getTarget(triple, isForward);

                        JsonElement contrib = targetNodeSchema.convert(graph, o);
                        if (tmp == null) {
                            tmp = new JsonArray();
                        }
                        tmp.add(contrib);
                    }
                }
            } finally {
                Iter.close(it);
            }
        }

        JsonElement result;;
        if (tmp == null) {
            result = JsonNull.INSTANCE;
        } else {
            if (isUniqueLang || maxCount == 1) {
                int arraySize = tmp.size();
                if (arraySize == 0) {
                    result = JsonNull.INSTANCE;
                } else if (arraySize == 1) {
                    result = tmp.get(0);
                } else {
                    // TODO more than 1 item, raise warning or error
                    result = tmp;
                }
            } else {
                result = tmp;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "PropertyConverter [baseFilter=" + baseFilter + ", targetNodeSchema=" + targetNodeSchema
                + ", isUniqueLang=" + isUniqueLang + ", maxCount=" + maxCount + "]";
    }
}
