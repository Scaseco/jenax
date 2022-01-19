package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * Apply union default graph to the underlying dataset.
 *
 * Depending on the backend, this operation may be implemented by means of e.g.
 * best-effort query rewriting via {@link org.apache.jena.sparql.algebra.TransformUnionQuery}
 * or using {@link org.apache.jena.sparql.core.DatasetGraph#getUnionGraph()}
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpUnionDefaultGraph
    extends Op1
{
    @Override
    OpUnionDefaultGraph setSubOp(Op subOp);

    @Override
    default <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    default OpUnionDefaultGraph clone(Model cloneModel, List<Op> subOps) {
        return this.inModel(cloneModel).as(OpUnionDefaultGraph.class)
                .setSubOp(subOps.iterator().next());
    }

    public static OpUnionDefaultGraph create(Op subOp) {
        return create(subOp.getModel(), subOp);
    }

    public static OpUnionDefaultGraph create(Model model, Op subOp) {
        OpUnionDefaultGraph result = model.createResource().as(OpUnionDefaultGraph.class)
            .setSubOp(subOp);

        return result;
    }
}