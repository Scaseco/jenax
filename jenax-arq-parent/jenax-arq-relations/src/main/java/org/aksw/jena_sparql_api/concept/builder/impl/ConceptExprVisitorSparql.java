package org.aksw.jena_sparql_api.concept.builder.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprConcept;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprExt;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprList;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprVisitor;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprExists;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprExt;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprForAll;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionExprVisitor;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.ConceptOps;


/**
 * Expression visitor that generates a SPARQL concept from a concept expression
 *
 * @author raven
 *
 */
public class ConceptExprVisitorSparql
    implements ConceptExprVisitor<Fragment1>, RestrictionExprVisitor<Fragment1>
{
    @Override
    public Fragment1 visit(ConceptExprConcept ce) {
        Fragment1 result = ce.getConcept();
        return result;
    }

    @Override
    public Fragment1 visit(ConceptExprConceptBuilder ce) {
        ConceptBuilder cb = ce.getConceptBuilder();

        ConceptExpr baseConceptExpr = cb.getBaseConceptExpr();
        Fragment1 baseConcept = baseConceptExpr == null
                ? null
                : baseConceptExpr.accept(this);

        Fragment1 concept = createConceptFromRestrictions(cb);

        Fragment1 result = baseConcept == null
                ? concept
                : ConceptOps.intersect(baseConcept, concept, null);

        return result;
    }

    @Override
    public Fragment1 visit(ConceptExprExt cse) {
        throw new UnsupportedOperationException("subclass the visitor to handle custom types");
    }

    @Override
    public Fragment1 visit(ConceptExprList ce) {
        List<Fragment1> concepts = ce.getMembers().stream().map(x -> x.accept(this)).collect(Collectors.toList());

        Fragment1 result = ce.isUnionMode()
                ? ConceptOps.union(concepts.stream())
                : ConceptOps.intersect(concepts.stream());

        return result;
    }


    public Fragment1 createConceptFromRestrictions(ConceptBuilder cb) {
        Collection<RestrictionBuilder> rbs = cb.listRestrictions();

        Fragment1 result = rbs.stream()
            .map(rb -> rb.get())
            .map(re -> re.accept(this))
            .reduce(Concept.TOP, (a, b) -> ConceptOps.intersect(a, b, null));

        return result;
    }

    @Override
    public Fragment1 visit(RestrictionExprExists re) {
        Fragment1 r = re.getRole().accept(this);

        ConceptExpr fillerCe = re.getFiller();
        Fragment1 filler = fillerCe.accept(this);

        Fragment1 result = ConceptOps.intersect(r, filler, null);
        return result;
    }

    @Override
    public Fragment1 visit(RestrictionExprForAll re) {
        return null;
    }

    @Override
    public Fragment1 visit(RestrictionExprExt re) {
        throw new UnsupportedOperationException("subclass the visitor to handle custom types");
    }
}
