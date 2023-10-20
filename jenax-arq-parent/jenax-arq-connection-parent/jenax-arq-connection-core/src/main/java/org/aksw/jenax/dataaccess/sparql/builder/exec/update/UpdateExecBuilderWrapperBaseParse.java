package org.aksw.jenax.dataaccess.sparql.builder.exec.update;

import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecBuilderWrapperBaseParse
    extends UpdateExecBuilderWrapperBase
{
    public UpdateExecBuilderWrapperBaseParse(UpdateExecBuilder delegate) {
        super(delegate);
    }

    @Override
    public UpdateExecBuilder update(String updateString) {
        UpdateRequest ur = UpdateFactory.create(updateString);
        delegate = update(ur);
        return this;
    }

    @Override
    public UpdateExecBuilder update(Update update) {
        UpdateRequest ur = new UpdateRequest(update);
        delegate = update(ur);
        return this;
    }
}
