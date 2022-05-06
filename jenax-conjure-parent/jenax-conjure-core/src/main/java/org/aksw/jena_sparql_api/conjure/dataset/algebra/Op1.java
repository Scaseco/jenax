package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.PolymorphicOnly;

public interface Op1
    extends Op
{
    @IriNs("rpif")
    @PolymorphicOnly
    @HashId
    Op getSubOp();
    Op1 setSubOp(Op op);

    @Override
    default List<Op> getChildren() {
        Op subOp = getSubOp();
        Objects.requireNonNull(subOp);
        return Collections.singletonList(subOp);
    }
}
