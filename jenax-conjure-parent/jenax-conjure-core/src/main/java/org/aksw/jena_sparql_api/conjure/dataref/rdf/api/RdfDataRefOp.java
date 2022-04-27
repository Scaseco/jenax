package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefOp;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.PolymorphicOnly;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
@RdfType("rpif:DataRefOp")
public interface RdfDataRefOp
    extends DataRefOp, RdfDataRef
{
    @IriNs("rpif")
    @PolymorphicOnly
    RdfDataRefOp setOp(Op dataRef);
    Op getOp();


    @Override
    default <T> T acceptRdf(RdfDataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    public static RdfDataRefOp create(Op op) {
        RdfDataRefOp result = op.getModel().createResource().as(RdfDataRefOp.class)
                .setOp(op);
        return result;
    }

}
