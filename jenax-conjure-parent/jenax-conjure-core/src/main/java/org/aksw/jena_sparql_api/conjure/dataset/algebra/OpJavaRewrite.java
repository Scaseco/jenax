package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.annotation.reprogen.ValueIri;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * An operator that applies a Java rewrite.
 * Requires the class name and optionally properties.
 *
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpJavaRewrite
    extends Op1
{
    @ResourceView
    public static interface Rewrite
        extends Resource
    {
        @Iri("rpif:javaClass")
        String getJavaClass();
        OpJavaRewrite setJavaClass(String javaClass);

        @Iri("rpif:property")
        @KeyIri("rpif:key")
        @ValueIri("rpif:value")
        Map<String, Node> getProperties();

    }

    @Iri("rpif:rewrites")
    List<Rewrite> getRewrites();

    default OpJavaRewrite addRewrite(String javaClass) {
        Rewrite rewrite = this.getModel().createResource().as(Rewrite.class);
        rewrite.setJavaClass(javaClass);
        getRewrites().add(rewrite);
        return this;
    }

    @Override
    OpJavaRewrite setSubOp(Op subOp);

    @Override
    default <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    default OpJavaRewrite clone(Model cloneModel, List<Op> subOps) {
        OpJavaRewrite result = this.inModel(cloneModel).as(OpJavaRewrite.class)
                .setSubOp(subOps.iterator().next());

        List<Rewrite> clonesTmp = getRewrites().stream().map(rewrite -> {
                Rewrite c = rewrite.inModel(cloneModel).as(Rewrite.class);
                c.setJavaClass(rewrite.getJavaClass());
                c.getProperties().putAll(rewrite.getProperties());
                return c;
            })
            .collect(Collectors.toList());

        // Adding items individually to an RDF-backed list is O(n^2)
        // Add in batch for O(n)
        result.getRewrites().addAll(clonesTmp);

        return result;
    }


//    public static OpJavaRewrite create(Model model, Op subOp, String javaClass) {
//        OpJavaRewrite result = create(model, subOp, javaClass);
//        return result;
//    }
    public static OpJavaRewrite create(Op subOp) {
        return create(subOp.getModel(), subOp);
    }

    public static OpJavaRewrite create(Model model, Op subOp) {
//        Rewrite rewrite = model.createResource().as(Rewrite.class);
//        rewrite.setJavaClass(javaClass);
        OpJavaRewrite result = model.createResource().as(OpJavaRewrite.class)
            .setSubOp(subOp);
        // result.getRewrites().add(rewrite);
        return result;
    }
}
