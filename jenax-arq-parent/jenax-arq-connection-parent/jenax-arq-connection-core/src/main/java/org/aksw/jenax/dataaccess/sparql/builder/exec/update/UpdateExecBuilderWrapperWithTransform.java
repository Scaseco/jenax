package org.aksw.jenax.dataaccess.sparql.builder.exec.update;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.link.update.UpdateExecOverUpdateProcessor;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/** QueryExecBuilder base class which parses query strings and delegates them to the object based method*/
public class UpdateExecBuilderWrapperWithTransform
    extends UpdateExecBuilderWrapperBase
{
    protected Function<? super UpdateRequest, ? extends UpdateRequest> updateTransform;
    protected BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> updateExecTransform;

    protected UpdateRequest effectiveUpdateRequest = null;

    protected UpdateExecBuilderWrapperWithTransform(
            UpdateExecBuilder delegate,
            Function<? super UpdateRequest, ? extends UpdateRequest> updateTransform,
            BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> updateExecTransform) {
        super(delegate);
        this.updateTransform = updateTransform;
        this.updateExecTransform = updateExecTransform;
    }

    /**
     *
     * @param delegate
     * @param updateTransformer null for identity transformation
     * @param updateExecTransformer null for identity transformation
     * @return
     */
    public static UpdateExecBuilder create(
            UpdateExecBuilder delegate,
            Function<? super UpdateRequest, ? extends UpdateRequest> updateTransform,
            BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> updateExecTransform) {
        return new UpdateExecBuilderWrapperWithTransform(delegate, updateTransform, updateExecTransform);
    }

    @Override
    public UpdateExecBuilder update(UpdateRequest update) {
        effectiveUpdateRequest = updateTransform == null
                ? update
                : updateTransform.apply(update);
        return super.update(effectiveUpdateRequest);
    }

    @Override
    public UpdateExec build() {
        UpdateExec raw = super.build();
        UpdateExec result = updateExecTransform == null
                ? raw
                : UpdateExecOverUpdateProcessor.adapt(updateExecTransform.apply(effectiveUpdateRequest, raw));
        return result;
    }

    @Override
    public UpdateExecBuilder update(String updateString) {
        UpdateRequest ur = UpdateFactory.create(updateString);
        return update(ur);
    }

    @Override
    public UpdateExecBuilder update(Update update) {
        UpdateRequest ur = new UpdateRequest(update);
        return update(ur);
    }
}
