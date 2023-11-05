package org.aksw.jena_sparql_api.concept.builder.test;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concept.builder.api.NodeBuilder;
import org.aksw.jena_sparql_api.concept.builder.impl.NodeBuilderFluent;
import org.aksw.jena_sparql_api.concept.builder.utils.Exprs;
import org.aksw.jena_sparql_api.shape.algebra.op.Algebra;
import org.aksw.jena_sparql_api.shape.syntax.Element;
import org.aksw.jena_sparql_api.shape.syntax.ElementEnumeration;
import org.aksw.jena_sparql_api.shape.syntax.ElementExists;
import org.aksw.jena_sparql_api.shape.syntax.ElementFilter;
import org.aksw.jena_sparql_api.shape.syntax.ElementFocus;
import org.aksw.jena_sparql_api.shape.syntax.ElementForAll;
import org.aksw.jena_sparql_api.shape.syntax.ElementGroup;
import org.aksw.jena_sparql_api.shape.syntax.ElementType;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.RelationOps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class MainTestConceptBuilder {
    public static void main(String[] args) {

        PrefixMapping2 pm = new PrefixMapping2(PrefixMapping.Extended);
        pm.setNsPrefix("o", "http://fp7-pp.publicdata.eu/ontology/");
        pm.setNsPrefix("foaf", FOAF.NS);
        Prologue prologue = new Prologue(pm);

        //Function<String, Element> elementParser = SparqlElementParserImpl.create(Syntax.syntaxARQ, prologue);


        Map<Node, Fragment2> virtualPredicates = new HashMap<>();
        Node mypred = NodeFactory.createURI("http://transitive");
        //virtualPredicates.put(mypred, Relation.create("?s foaf:knows+ ?o", "s", "o", elementParser));
        //pm.getLocalPrefixMapping().

        Node allInTheSameCountry = NodeFactory.createURI("http://sameCountry");
        virtualPredicates.put(mypred, RelationOps.forAllHavingTheSameValue(
                RelationOps.from(PathParser.parse("o:partner/o:address/o:country", pm)), null));

        virtualPredicates.forEach((k, v) -> System.out.println(v.getSourceConcept().asQuery()));

//        ConceptBuilder cb = ConceptBuilderFluent
//             .from(ConceptBuilderFluent.union()
//                     .addMember(new ConceptExprConcept(Concept.create("?s a o:Project", "s", pm))))
//             //.unionMode() // whether multiple restrictions are interpreted as dis - or conjunctive - if disjunctive, the base concept is conjunctive which each restriction
//            .newRestriction().on(RDFS.label).as("x").forAll()
//            .getRoot();
//
//
//        ConceptExprConceptBuilder ce = new ConceptExprConceptBuilder(cb);
//        ConceptExprVisitorSparql visitor = new ConceptExprVisitorSparql();
//        Concept c = ce.accept(visitor);
//        System.out.println(c);


        Element e = new ElementGroup(
                new ElementType(NodeFactory.createURI("http://Airport")),
                new ElementForAll(PathParser.parse("o:operator/o:address/o:country", pm), new ElementEnumeration(RDF.subject.asNode())), // new ElementGroup()),
                new ElementExists(PathParser.parse("o:openingDate", pm), new ElementEnumeration(RDF.predicate.asNode(), RDF.object.asNode())), // new ElementGroup()),
                new ElementFocus(PathParser.parse("rdfs:label", PrefixMapping.Extended)),
                //new ElementExists(path, filler)
                new ElementFilter(ExprUtils.parse("regex(?_, 'dbpedia')"))
                );



        Fragment1 c = Algebra.toConcept(Algebra.compile(e));

        System.out.println("CONCEPT: " + c);



        //cb.isUnion();
        //cb.isIntersection();
        //cb.asUnion();

        /**
         * Project those relations for which there exists rdfs:label predicates
         * I suppose this would work by stating that some in/out predicate is desired, which is assigned an alias,
         * and the alias is mapped to a concept builder
         *
         * So this means, that the projection is closely related to the concept builder, as the set of predicates
         * which to project can be expressed as a concept.
         *
         * Hence: C, D := C AND D, C OR D, NOT C, exists r.C, forAll r.C
         * becomes:
         * C, D := C AND D, C OR D, NOT C, exists R.C, forAll R.C
         * with
         * R := r, C
         * so a role can be either a primitive role, or a set of roles specified by a concept.
         * so essentially we can have role expressions analogous to class expressions.
         *
         *
         */

        NodeBuilder nb = NodeBuilderFluent.start()
            .out(RDFS.label).setOptional(true).getTarget().addExpr(Exprs.langMatches("en"))
            .out(mypred).setOptional(true).getSource()
            .getRoot();




//            .beginOut()
//             .end().star()




//        Concept concept = cb.get();
//        System.out.println("CONCEPT: " + concept);
    }
}
