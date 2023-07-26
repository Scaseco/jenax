package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.arq.dataset.impl.DatasetGraphOneNgImpl;
import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import io.reactivex.rxjava3.core.Flowable;


/**
 * LookupService for fetching models related to resources
 * @author raven
 *
 */
public class LookupServiceSparqlConstructQuads
    implements LookupService<Node, DatasetOneNg>
{
    private QueryExecutionFactoryQuery qef;
    private Query query;
    private Var var;

    public LookupServiceSparqlConstructQuads(QueryExecutionFactoryQuery qef, Query query) {
        super();
        Preconditions.checkArgument(query.isConstructQuad(), "Query must be constructQuad type.");
        this.qef = qef;
        this.query = query;
        Set<Node> graphs = query.getConstructTemplate().getQuads().stream().map(Quad::getGraph).collect(Collectors.toSet());
        Preconditions.checkArgument(graphs.size() == 1, "Construct template must produce exactly one graph");
        Node graphNode = graphs.iterator().next();
        Preconditions.checkArgument(graphNode.isVariable(), "Graph must be a variable");
        this.var = (Var)graphNode;
    }

    @Override
    public Flowable<Entry<Node, DatasetOneNg>> apply(Iterable<Node> keys) {
        //System.out.println("Lookup Request with " + Iterables.size(keys) + " keys: " + keys);

        //Map<Node, Model> result = new HashMap<Node, Model>();

        Flowable<Entry<Node, DatasetOneNg>> result;

        if(!Iterables.isEmpty(keys)) {

            ExprList exprs = new ExprList();
            for(Node key : keys) {
                Expr e = NodeValue.makeNode(key);
                exprs.add(e);
            }

            E_OneOf expr = new E_OneOf(new ExprVar(var), exprs);
            Element filterElement = new ElementFilter(expr);

            Query q = query.cloneQuery();
            Element newElement = ElementUtils.mergeElements(q.getQueryPattern(), filterElement);
            q.setQueryPattern(newElement);

            //System.out.println("Lookup query: " + q);

            // SparqlRx.execPartitioned(null, null);
            result = SparqlRx.execConstructQuads(() -> qef.createQueryExecution(q))
                    .groupBy(t -> t.getGraph())
                    .flatMapSingle(groups -> groups
                            .collectInto(GraphFactory.createDefaultGraph(), (g, quad) -> g.add(quad.asTriple()))
                            .map(x -> Maps.immutableEntry(groups.getKey(), DatasetOneNgImpl.wrap(DatasetGraphOneNgImpl.create(groups.getKey(), x)))));
// DatasetGraphOneNgImpl.create(var, null)

//            QueryExecution qe = qef.createQueryExecution(q);
//            Model fullModel = qe.execConstruct();
//
//            Iterator<Node> it = keys.iterator();
//            while(it.hasNext()) {
//                Node key = it.next();
//
//                Resource s = new ResourceImpl(key, (ModelCom)fullModel);
//                Model tmp = ModelUtils.filterBySubject(fullModel, s);
//                result.put(key, tmp);
//            }
        } else {
            result = Flowable.empty();
        }

        return result;
    }
}
