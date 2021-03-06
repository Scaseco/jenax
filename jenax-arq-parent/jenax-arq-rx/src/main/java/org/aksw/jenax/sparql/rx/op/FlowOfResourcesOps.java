package org.aksw.jenax.sparql.rx.op;

import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.apache.jena.rdf.model.Resource;

import io.reactivex.rxjava3.core.Flowable;

public class FlowOfResourcesOps {
    /**
     * Only applicable for IRI resources.
     * Maps each IRI resource to a single dataset with a single named graph.
     * The IRI of the named graph is that of the resource and the content is the
     * resource's model.
     * No triples are copied - the resulting dataset is a view over.
     *
     * @param in
     * @return
     */
    public static Flowable<DatasetOneNg> mapToDatasets(Flowable<Resource> in) {
        return in.map(r -> DatasetOneNgImpl.create(r.getURI(), r.getModel().getGraph()));
    }
}
