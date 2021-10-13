package org.aksw.jenax.arq.dataset.api;

import org.aksw.jenax.arq.dataset.impl.LiteralInDatasetImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Literal;

public interface LiteralInDataset
    extends Literal, RDFNodeInDataset
{
    @Override
    default LiteralInDataset asLiteral() {
        return this;
    }

    @Override
    LiteralInDatasetImpl inDataset(Dataset other);
}
