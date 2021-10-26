package org.aksw.jena_sparql_api.mapper.model;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.PropertyRelation;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * A property that is serialized in RDF as a Seq
 *
 * class {
 *   @Iri("geom:geometry")
 *   @RdfSeq("") // by default, lists imply an RdfSeq, with a sub-iri generated as #{subject + '-' +  property.name}
 *   @DefaultIri("") // Rule for creating the collection resource
 *   List<Object> items;
 * }
 *
 *
 * @author raven
 *
 */
public class RdfPropertySeq {
    private static final Logger logger = LoggerFactory.getLogger(RdfPropertySeq.class);

    //protected EntityManagerRdf;
    //protected

    protected Quad quad; // g, s, p; - object position should be Node.ANY - is ignored

    protected Function<Object, Node> objectToNode;

    protected RdfClass targetRdfClass;

    public static final PropertyRelation seqRelation = PropertyRelation.create("?s ?p ?o . Filter(regex(?p, 'http://www.w3.org/1999/02/22-rdf-syntax-ns#_[0-9]+'))", "s", "p", "o");
    public static final Expr seqExpr = ExprUtils.parse("regex(?p, 'http://www.w3.org/1999/02/22-rdf-syntax-ns#_[0-9]+')");


    public void toRdf(DatasetGraph target, List<Object> items) {
    }


    public List<Object> toJava(DatasetGraph datasetGraph) {
        return null;
    }

}
