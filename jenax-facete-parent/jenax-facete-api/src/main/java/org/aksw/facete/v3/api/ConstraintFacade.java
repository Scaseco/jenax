package org.aksw.facete.v3.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import com.google.common.collect.Iterators;
import com.google.common.collect.Range;

/**
 * In general, there are anonymous and named constraints.
 * Named constraints can be
 *
 *
 * @author raven
 *
 */
// TODO Rename to ConstraintNode?
public interface ConstraintFacade<B> {
    Collection<FacetConstraintControl> list();

    Collection<HLFacetConstraint<? extends ConstraintFacade<B>>> listHl();

    default Stream<FacetConstraintControl> stream() {
        return list().stream();
    }

    /** Add an anonymous equal constraint */
    HLFacetConstraint<? extends ConstraintFacade<B>> eq(Node node);

    /**
     * Constraint that matches all items that have any value
     * on the predicate this constraint is activated on.
     *
     * @return
     */
    HLFacetConstraint<? extends ConstraintFacade<B>> exists();


    /**
     * Typically complex constraint that matches all items
     * that lack the predicate (on which this constraint is activated on)
     * on the *immediate parent*.
     *
     *
     * @return
     */
    HLFacetConstraint<? extends ConstraintFacade<B>> absent();



    HLFacetConstraint<? extends ConstraintFacade<B>> gt(Node node);
    HLFacetConstraint<? extends ConstraintFacade<B>> neq(Node node);


    HLFacetConstraint<? extends ConstraintFacade<B>> nodeRange(Range<ComparableNodeValue> range);

    HLFacetConstraint<? extends ConstraintFacade<B>> range(Range<?> range);

    /**
     * Return the expr that denotes the ConstraintFacade's underlying
     * FacetNode or FacetMultiNode.
     * @return
     */
    Expr thisAsExpr();

    default boolean hasExpr(Expr expr) {
        boolean result = listHl().stream().map(HLFacetConstraint::expr)
            .filter(e -> Objects.equals(expr, e))
            .findFirst().isPresent();

        return result;
    }


    default boolean removeExpr(Expr expr) {
        ExtendedIterator<Expr> it = WrappedIterator.create(listHl().iterator())
                .mapWith(HLFacetConstraint::expr);

        boolean result = Iterators.removeAll(it, Collections.singleton(expr));
        return result;
    }

    //ConstraintFacade<B> addExpr(Expr expr);

    HLFacetConstraint<? extends ConstraintFacade<B>> createConstraint(Expr expr);


    default HLFacetConstraint<? extends ConstraintFacade<B>> getOrCreateConstraint(Expr expr) {
        List<HLFacetConstraint<? extends ConstraintFacade<B>>> list = findConstraintByExpr(expr).toList();


        HLFacetConstraint<? extends ConstraintFacade<B>> result;
        if(list.isEmpty()) {
            result = createConstraint(expr);
            //addExpr(expr);
        } else {
            result = list.iterator().next();
        }

        return result;
    }

    default ExtendedIterator<HLFacetConstraint<? extends ConstraintFacade<B>>> findConstraintByExpr(Expr expr) {
        ExtendedIterator<HLFacetConstraint<? extends ConstraintFacade<B>>> result = WrappedIterator.create(listHl().iterator())
                .filterKeep(e -> Objects.equals(expr, e.expr()));

        return result;

    }


    default boolean toggle(Expr expr) {
        boolean alreadySet = hasExpr(expr);
        if(!alreadySet) {
            HLFacetConstraint<? extends ConstraintFacade<B>> constraint = createConstraint(expr);
            listHl().add(constraint);
            //addExpr(expr);
        } else {
            removeExpr(expr);
        }

        return !alreadySet;
    }



    default HLFacetConstraint<? extends ConstraintFacade<B>> eqIri(String iriStr) {
        return eq(NodeFactory.createURI(iriStr));
    }

    default HLFacetConstraint<? extends ConstraintFacade<B>> regex(String pattern) {
        return regex(pattern, "i");
    }

    HLFacetConstraint<? extends ConstraintFacade<B>> regex(String pattern, String flags);

    default HLFacetConstraint<? extends ConstraintFacade<B>> eqStr(String stringLiteral) {
        return eq(NodeFactory.createLiteral(stringLiteral));
    }

    default HLFacetConstraint<? extends ConstraintFacade<B>> eq(RDFNode rdfNode) {
        return eq(rdfNode.asNode());
    }

//	default find(Function<? super Expr, ? extends Expr> expr) {
//
//	}
//
//	default Stream<HLFacetConstraint> find(Class<?> exprType, Node ... nodes) {
//		Stream<HLFacetConstraint> result = listHl().stream()
//			.filter(c -> exprType.isAssignableFrom(c.expr().getClass())
//					;
//			//.filter(x -> true);
//		return result;
//	}

//	default ConstraintFacade<B> exists(RDFNode rdfNode) {
//		return exists(rdfNode.asNode());
//	}

    @Deprecated // Renamed to 'leave'
    B end();

    /** Leave constraint building and return the parent object */
    default B leaveConstraints() {
        return end();
    }
}
