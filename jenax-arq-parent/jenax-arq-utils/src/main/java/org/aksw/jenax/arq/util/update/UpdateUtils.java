package org.aksw.jenax.arq.util.update;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.syntax.ElementTransformSubst2;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateData;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteInsert;
import org.apache.jena.sparql.modify.request.UpdateLoad;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.modify.request.UpdateWithUsing;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import com.google.common.base.Preconditions;

public class UpdateUtils {

    // RU = rdf update (file extension foo.ru)
    private static final UpdateRequest RENAME_PROPERTY_RU = UpdateFactory.create("DELETE { ?s ?from ?o } INSERT { ?s ?to ?o } WHERE { ?s ?from ?o }");
    private static final UpdateRequest RENAME_NAMESPACE_RU = UpdateFactory.create(String.join("\n",
            "DELETE { ?s ?p ?o }",
            "INSERT { ?ss ?pp ?oo }",
            "WHERE {",
            "  BIND(STRLEN(?from) + 1 AS ?l)",
            "  ?s ?p ?o",
            "  BIND(isIRI(?s) && STRSTARTS(STR(?s), ?from) AS ?cs)",
            "  BIND(isIRI(?p) && STRSTARTS(STR(?p), ?from) AS ?cp)",
            "  BIND(isIRI(?o) && STRSTARTS(STR(?o), ?from) AS ?co)",
            "  FILTER(?cs || ?cp || ?co)",
            "  BIND(IF(?cs, IRI(CONCAT(?to, SUBSTR(STR(?s), ?l))), ?s) AS ?ss)",
            "  BIND(IF(?cp, IRI(CONCAT(?to, SUBSTR(STR(?p), ?l))), ?p) AS ?pp)",
            "  BIND(IF(?co, IRI(CONCAT(?to, SUBSTR(STR(?o), ?l))), ?o) AS ?oo)",
            "}"));

    /**
     * Rename a property in a graph (via SPARQL Update)
     * @param graph
     * @param from
     * @param to
     */
    public static void renameProperty(Graph graph, String from, String to) {
        Node fromNode = NodeFactory.createURI(from);
        Node toNode = NodeFactory.createURI(to);
        execRename(graph, RENAME_PROPERTY_RU, fromNode, toNode);
    }

    public static void renameNamespace(Graph graph, String from, String to) {
        Node fromNode = NodeFactory.createLiteral(from);
        Node toNode = NodeFactory.createLiteral(to);
        execRename(graph, RENAME_NAMESPACE_RU, fromNode, toNode);
    }

    public static void execRename(Graph graph, UpdateRequest template, Node from, Node to) {
        UpdateExec
            .dataset(DatasetGraphFactory.wrap(graph))
            .update(template)
             .substitution("from", from)
             .substitution("to", to)
            .execute();
    }

    public static Update applyOpTransform(Update update, Function<? super Op, ? extends Op> transform) {
        Function<Element, Element> xform = elt -> ElementUtils.applyOpTransform(elt, transform);

        Update result = applyElementTransform(update, xform);
        return result;
    }

    public static Update applyNodeTransform(Update update, NodeTransform nodeTransform) {
        ElementTransform eltrans = new ElementTransformSubst2(nodeTransform) ;
        ExprTransform exprTrans = new ExprTransformNodeElement(nodeTransform, eltrans) ;
        Update result = UpdateTransformOps.transform(update, eltrans, exprTrans) ;

        return result;
    }

    public static Update applyElementTransform(Update update, Function<? super Element, ? extends Element> transform) {
        UpdateTransformVisitor visitor = new UpdateTransformVisitor(transform);
        update.visit(visitor);
        Update s = visitor.getResult();
        return s;
    }

    public static Update clone(Update update) {
        Update result;
        if(update instanceof UpdateDataInsert) {
            result = clone((UpdateDataInsert)update);
        } else if(update instanceof UpdateDataDelete) {
            result = clone((UpdateDataDelete)update);
        } else if(update instanceof UpdateDeleteInsert) {
            result = clone((UpdateDeleteInsert)update);
        } else {
            throw new IllegalArgumentException("Unsupported argument type: " + update.getClass());
        }

        return result;
    }

    public static UpdateDataInsert clone(UpdateDataInsert update) {
        UpdateDataInsert result = new UpdateDataInsert(new QuadDataAcc(update.getQuads()));
        return result;
    }

    public static UpdateDataDelete clone(UpdateDataDelete update) {
        UpdateDataDelete result = new UpdateDataDelete(new QuadDataAcc(update.getQuads()));
        return result;
    }

    // Note UpdateModify gets 'upgraded' to UpdateDeleteInsert
    public static UpdateDeleteInsert clone(UpdateModify update) {
        UpdateDeleteInsert result = new UpdateDeleteInsert();
        result.setElement(update.getWherePattern());
        result.setWithIRI(update.getWithIRI());

        for(Quad quad : update.getDeleteQuads()) {
            result.getDeleteAcc().addQuad(quad);
        }

        for(Quad quad : update.getInsertQuads()) {
            result.getInsertAcc().addQuad(quad);
        }

        for(Node node : update.getUsing()) {
            result.addUsing(node);
        }

        for(Node node : update.getUsingNamed()) {
            result.addUsingNamed(node);
        }

        return result;
    }

//	public static boolean hasWithIri(Update update) {
//		boolean result = update instanceof UpdateWithUsing;
//		return result;
//	}

    public static String getWithIri(Update update) {
        Node with = update instanceof UpdateWithUsing
                ? ((UpdateWithUsing)update).getWithIRI()
                : null;

        String result = with == null ? null : with.toString();

        return result;
    }

//    public static void setWithIri(Update update, Node withIri) {
//    	UpdateWithUsing x = (UpdateWithUsing)update;
//    	x.setWithIRI(withIri);
//    }
    public static void applyWithIriIfApplicable(Update update, String withIri) {
        Node node = NodeFactory.createURI(withIri);
        applyWithIriIfApplicable(update, node);
    }

    public static void applyWithIriIfApplicable(Update update, Node withIri) {
        if(update instanceof UpdateWithUsing) {
            UpdateWithUsing x = (UpdateWithUsing)update;
            boolean hasWithIri = x.getWithIRI() != null;
            if(!hasWithIri) {
                x.setWithIRI(withIri);
            }
        }
    }

    public static boolean applyDatasetDescriptionIfApplicable(Update update, DatasetDescription dg) {
        boolean result;
        if(update instanceof UpdateWithUsing) {
            UpdateWithUsing x = (UpdateWithUsing)update;
            // We only apply the change if there is no dataset description
            result = !hasDatasetDescription(x);

            if(result) {
                applyDatasetDescription(x, dg);
            }
        } else {
            result = false;
        }

        return result;
    }

    public static boolean hasDatasetDescription(UpdateWithUsing update) {
        boolean result = update.getUsing() != null && !update.getUsing().isEmpty();
        result = result || update.getUsingNamed() != null && !update.getUsingNamed().isEmpty();

        return result;
    }

    public static void applyDatasetDescription(UpdateWithUsing update, DatasetDescription dg) {
        if(dg != null) {
            List<String> dgus = dg.getDefaultGraphURIs();
            if(dgus != null) {
                for(String dgu : dgus) {
                    Node node = NodeFactory.createURI(dgu);
                    update.addUsing(node);
                }
            }

            List<String> ngus = dg.getDefaultGraphURIs();
            if(ngus != null) {
                for(String ngu : ngus) {
                    Node node = NodeFactory.createURI(ngu);
                    update.addUsing(node);
                }
            }
        }
    }

    public static boolean overwriteDatasetDescription(UpdateWithUsing update, DatasetDescription dd) {
        boolean result = false;
        if (dd != null) {
            {
                List<String> items = dd.getDefaultGraphURIs();
                if (items != null && !items.isEmpty()) {
                    result = true;
                    List<Node> usingGraphs = update.getUsing();
                    if (usingGraphs != null) {
                        usingGraphs.clear();
                    }

                    for(String dgu : items) {
                        Node node = NodeFactory.createURI(dgu);
                        update.addUsing(node);
                    }
                }
            }

            {
                List<String> items = dd.getNamedGraphURIs();
                if (items != null && !items.isEmpty()) {
                    result = true;
                    List<Node> usingGraphs = update.getUsingNamed();
                    if (usingGraphs != null) {
                        usingGraphs.clear();
                    }

                    for(String dgu : items) {
                        Node node = NodeFactory.createURI(dgu);
                        update.addUsingNamed(node);
                    }
                }
            }
        }
        return result;
    }

    public static boolean overwriteDatasetDescriptionIfApplicable(Update update, DatasetDescription dg) {
        boolean result;
        if(update instanceof UpdateWithUsing) {
            UpdateWithUsing x = (UpdateWithUsing)update;
            result = overwriteDatasetDescription(x, dg);
        } else {
            result = false;
        }
        return result;
    }


    public static Update copyWithIri(Update update, String withIriStr, boolean substituteDefaultGraph) {
        Update result = copyWithIri(update, NodeFactory.createURI(withIriStr), substituteDefaultGraph);
        return result;
    }

    public static Update copyWithIri(Update update, Node withIri, boolean substituteDefaultGraph) {
        Update result;
        if(update instanceof UpdateData) {
            UpdateData ud = (UpdateData)update;
            result = substituteDefaultGraph ? copyWithDefaultGraph(ud, withIri) : ud;
        } else {
            result = clone(update);
            applyWithIriIfApplicable(result, withIri);
        }

        return result;
    }

    public static UpdateData copyWithDefaultGraph(UpdateData update, String newGStr) {
        Node newG = NodeFactory.createURI(newGStr);
        UpdateData result = copyWithDefaultGraph(update, newG);
        return result;
    }

    public static UpdateData copyWithDefaultGraph(UpdateData update, Node newG) {
        Function<Quad, Quad> fn = q -> Quad.isDefaultGraph(q.getGraph())
                ? new Quad(newG, q.asTriple())
                : q;

                UpdateData result = copyWithQuadTransform(update, fn);
        return result;
    }

    public static UpdateData copyWithQuadTransform(UpdateData update, Function<? super Quad, ? extends Quad> quadTransform) {

        UpdateData result;
        if(update instanceof UpdateDataInsert) {
            UpdateDataInsert ud = (UpdateDataInsert)update;
            result = copyWithQuadTransform(ud, quadTransform);

        } else if(update instanceof UpdateDataDelete) {
            UpdateDataDelete ud = (UpdateDataDelete)update;
            result = copyQuadTransform(ud, quadTransform);

        } else {
            throw new IllegalArgumentException("Unknown type: " + update + " " + (update == null ? "" : update.getClass()));
        }

        return result;
    }


    public static UpdateDataInsert copyWithQuadTransform(UpdateDataInsert update, Function<? super Quad, ? extends Quad> quadTransform) {
        UpdateDataInsert result = new UpdateDataInsert(new QuadDataAcc(
                update.getQuads().stream().map(quadTransform)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
        return result;
    }

    public static UpdateDataDelete copyQuadTransform(UpdateDataDelete update, Function<? super Quad, ? extends Quad> quadTransform) {
        UpdateDataDelete result = new UpdateDataDelete(new QuadDataAcc(
                update.getQuads().stream().map(quadTransform)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
        return result;
    }
//
//    public static <T> void applyInPlaceTransform(Collection<T> items, Function<? super T, ? extends T> itemTransform) {
//    	List<T> newItems = items.stream()
//    			.map(itemTransform)
//    			.filter(Objects::nonNull)
//    			.collect(Collectors.toList());
//
//    	items.clear();
//    	items.addAll(newItems);
//    }
//

    /** Convert a construct query into an insert query */
    public static Update constructToInsert(Query query) {
        Preconditions.checkArgument(query.isConstructType(), "Expected a CONSTRUCT query");

        boolean applyWrapping =
                query.hasLimit() || query.hasOffset() || query.hasOrderBy() || query.hasValues()
                // Omit features not supported by construct queries:
                // hasGroupBy() / hasHaving() / query.hasAggregators()
                ;

        Element elt = applyWrapping
                ? new ElementSubQuery(query)
                : query.getQueryPattern();

        UpdateModify result = new UpdateModify();
        result.setHasInsertClause(true);
        result.setElement(elt);

        // Copy the construct template
        query.getGraphURIs().forEach(iri -> result.addUsing(NodeFactory.createURI(iri)));
        query.getNamedGraphURIs().forEach(iri -> result.addUsingNamed(NodeFactory.createURI(iri)));
        QuadAcc acc = result.getInsertAcc();
        query.getConstructTemplate().getQuads().forEach(acc::addQuad);

        return result;
    }

    /**
     * Turn a LOAD statement into an INSERT one.
     *
     * @implNote Uses {@link AsyncParser} to parse the given LOAD statement's source.
     */
    public static UpdateDataInsert materialize(UpdateLoad update) {
        String str = update.getSource();
        Node dest = update.getDest();
        Stream<Quad> stream = AsyncParser.of(str).streamQuads();

        if (dest != null) {
            stream = stream.map(q -> new Quad(dest, q.asTriple()));
        }

        List<Quad> quads = stream.collect(Collectors.toList());
        UpdateDataInsert result = new UpdateDataInsert(new QuadDataAcc(quads));
        return result;
    }
}
