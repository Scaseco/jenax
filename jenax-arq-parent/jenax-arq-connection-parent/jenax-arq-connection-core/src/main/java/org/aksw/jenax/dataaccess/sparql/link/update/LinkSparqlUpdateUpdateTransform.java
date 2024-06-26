package org.aksw.jenax.dataaccess.sparql.link.update;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderWrapperWithTransform;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/**
 * LinkSparqlQuery wrapper that can transform both
 * the incoming Query and the obtained QueryExec instances.
 * Supplied transformation functions may be null.
 */
public class LinkSparqlUpdateUpdateTransform
    extends LinkSparqlUpdateWrapperBase
{
    protected Function<? super UpdateRequest, ? extends UpdateRequest> updateTransform;
    protected BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> updateExecTransform;

    public LinkSparqlUpdateUpdateTransform(
            LinkSparqlUpdate delegate,
            Function<? super UpdateRequest, ? extends UpdateRequest> updateTransform,
            BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> updateExecTransform) {
        super(delegate);
        this.updateTransform = updateTransform;
        this.updateExecTransform = updateExecTransform;
    }

    @Override
    public UpdateExecBuilder newUpdate() {
        return UpdateExecBuilderWrapperWithTransform.create(delegate.newUpdate(), updateTransform, updateExecTransform);
    }
}
