package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRef;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.PolymorphicOnly;
import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;

@ResourceView
@RdfTypeNs("rpif")
public interface OpDataRefResource
    extends Op0
{
    @PolymorphicOnly
    @IriNs("rpif")
    @HashId
    RdfDataRef getDataRef();
    OpDataRefResource setDataRef(RdfDataRef dataRef);

    @Override
    default <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    default OpDataRefResource clone(Model cloneModel, List<Op> subOps) {
        // TODO Here we also clone the data ref, which might be undesired
        Resource tmpDataRef = getDataRef();
        Model tmp = ResourceUtils.reachableClosure(tmpDataRef);
        cloneModel.add(tmp);
        Resource dataRef = tmpDataRef.inModel(cloneModel);
        RdfDataRef cloneDataRef = JenaPluginUtils.polymorphicCast(dataRef, RdfDataRef.class);

        return this.inModel(cloneModel).as(OpDataRefResource.class)
                .setDataRef(cloneDataRef);
    }

    public static OpDataRefResource from(RdfDataRef dataRef) {
        return from(dataRef.getModel(), dataRef);
    }

    public static OpDataRefResource from(Model model, RdfDataRef dataRef) {
        OpDataRefResource result = model
                .createResource().as(OpDataRefResource.class)
                .setDataRef(dataRef);

        return result;
    }

}
