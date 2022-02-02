package org.aksw.jenax.connection.query;

import java.util.Iterator;

import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.WrappedIterator;


public class QueryExecWithNodeTransform
    implements QueryExecDecorator
{
    //protected Converter<Node, Node> nodeConverter;
	protected QueryExec decoratee;
    protected NodeTransform nodeTransform;

    public QueryExecWithNodeTransform(QueryExec decoratee, NodeTransform nodeTransform) {
        this.decoratee = decoratee;
        this.nodeTransform = nodeTransform;
    }
    
    @Override
    public QueryExec getDecoratee() {
    	return decoratee;
    }

	@Override
	public DatasetGraph getDataset() {
		return NodeTransformLib2.copyWithNodeTransform(nodeTransform, getDecoratee().getDataset());
	}

	@Override
	public RowSet select() {
		return NodeTransformLib2.applyNodeTransform(nodeTransform, getDecoratee().select());
	}

	@Override
	public Graph construct(Graph graph) {
		constructTriples().forEachRemaining(graph::add);
		return graph;
	}

	@Override
	public Iterator<Triple> constructTriples() {
		return WrappedIterator.create(getDecoratee().constructTriples()).mapWith(t -> NodeTransformLib.transform(nodeTransform, t));
	}

	@Override
	public Iterator<Quad> constructQuads() {
		return WrappedIterator.create(getDecoratee().constructQuads()).mapWith(q -> NodeTransformLib.transform(nodeTransform, q));
	}

	@Override
	public DatasetGraph constructDataset(DatasetGraph dataset) {
		constructQuads().forEachRemaining(dataset::add);
		return dataset;
	}

	@Override
	public Graph describe(Graph graph) {
		describeTriples().forEachRemaining(graph::add);
		return graph;
	}

	@Override
	public Iterator<Triple> describeTriples() {
		return WrappedIterator.create(getDecoratee().describeTriples()).mapWith(t -> NodeTransformLib.transform(nodeTransform, t));
	}

	@Override
	public boolean ask() {
		return getDecoratee().ask();
	}

	@Override
	public JsonArray execJson() {
		return getDecoratee().execJson();
	}

	@Override
	public Iterator<JsonObject> execJsonItems() {
		return getDecoratee().execJsonItems();
	}

    
//
//    @Override
//    public RowSet execSelect() {
//        ResultSet core = super.execSelect();
//
//        ResultSet result = applyNodeTransform(nodeTransform, core, this);
//        return result;
//    }
//
//    @Override
//    public Model execConstruct() {
//        Model model = super.execConstruct();
//        Model result = NodeTransformLib2.copyWithNodeTransform(nodeTransform, model);
//        return result;
//    }
//
//    @Override
//    public Model execConstruct(Model model) {
//        Model tmp = execConstruct();
//        model.add(tmp);
//        return model;
//    }
//
//    @Override
//    public Iterator<Triple> execConstructTriples() {
//        Iterator<Triple> core = super.execConstructTriples();
//        Iterator<Triple> result = WrappedIterator.create(Iter.onClose(core, this::close))
//                .mapWith(t -> NodeTransformLib.transform(nodeTransform, t));
//
//        return result;
//    }
//
//    @Override
//    public Dataset execConstructDataset() {
//        Dataset dataset = super.execConstructDataset();
//        Dataset result = NodeTransformLib2.applyNodeTransform(nodeTransform, dataset);
//        return result;
//    }
//
//    @Override
//    public Dataset execConstructDataset(Dataset dataset) {
//        Dataset tmp = execConstructDataset();
//        DatasetGraphUtils.addAll(dataset.asDatasetGraph(), tmp.asDatasetGraph());
//        return dataset;
//    }
//
//    @Override
//    public Model execDescribe() {
//        Model model = super.execConstruct();
//        Model result = NodeTransformLib2.copyWithNodeTransform(nodeTransform, model);
//        return result;
//    }
//
//    @Override
//    public Model execDescribe(Model model) {
//        Model tmp = execDescribe();
//        model.add(tmp);
//        return model;
//    }
//
//    @Override
//    public Iterator<Triple> execDescribeTriples() {
//        Iterator<Triple> core = super.execDescribeTriples();
//        Iterator<Triple> result = WrappedIterator.create(Iter.onClose(core, this::close))
//                .mapWith(t -> NodeTransformLib.transform(nodeTransform, t));
//
//        return result;
//    }
//
//    public static ResultSet applyNodeTransform(NodeTransform nodeTransform, ResultSet rs, QueryExecution qe) {
//        // Closeable closeable = rs instanceof Closeable ? (Closeable)rs : null;
//        List<String> vars = rs.getResultVars();
//
//        ExtendedIterator<Binding> it = WrappedIterator.create(RowSet.adapt(rs))
//            .mapWith(b -> NodeTransformLib2.transformValues(b, nodeTransform));
//
//        QueryIterator queryIter = QueryIterPlainWrapper.create(it);
//        ResultSet core = ResultSetFactory.create(queryIter, vars);
//
//        ResultSet result = new ResultSetCloseable(core, qe);
//        return result;
//    }
//
//
//    public static void main(String[] args) {
//        Model model = ModelFactory.createDefaultModel();
//        Node bn = NodeFactory.createBlankNode("test");
//        model.add(model.wrapAsResource(bn), RDF.type, RDF.Property);
//
//        NodeTransform nodeTransform = NodeTransformLib2.createBnodeLabelTransform(ExprUtils.parse("CONCAT('_:', ?x )"), Vars.x);
//
////		NodeTransform nodeTransform = x -> {
////			Node r = x.equals(RDF.Nodes.Property) ? OWL.ObjectProperty.asNode() : x;
////			System.out.println(x + " -> " + r);
////			return r;
////		};
//
//        try(QueryExecution qe = new QueryExecTransformResult(
//                QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", model), nodeTransform)) {
//
//            System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//        }
//    }

}
