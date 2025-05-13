package org.aksw.jenax.dataaccess.sparql.link.update;

import java.util.function.BiFunction;

import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class LinkSparqlUpdateTransformUpdateTransform
    implements LinkSparqlUpdateTransform
{
    protected UpdateRequestTransform updateTransform;
    protected BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> updateExecTransform;

    public LinkSparqlUpdateTransformUpdateTransform(UpdateRequestTransform updateTransform, BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> updateExecTransform) {
        super();
        this.updateTransform = updateTransform;
        this.updateExecTransform = updateExecTransform;
    }

    public UpdateRequestTransform getUpdateTransform() {
        return updateTransform;
    }

//    public BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> getUpdateExecTransform() {
//        return updateExecTransform;
//    }

    @Override
    public LinkSparqlUpdate apply(LinkSparqlUpdate base) {
        return new LinkSparqlUpdateUpdateTransform(base, updateTransform, updateExecTransform);
    }

    @Override
    public String toString() {
        return "LinkSparqlUpdateTransformUpdateTransform [updateTransform=" + updateTransform + ", updateExecTransform=" + updateExecTransform + "]";
    }
}
