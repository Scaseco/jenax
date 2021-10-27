package org.aksw.jena_sparql_api.core;

import org.aksw.jenax.arq.connection.core.UpdateExecutionFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 12:53 PM
 */
public class UpdateExecutionFactoryDecorator
    implements UpdateExecutionFactory
{
    protected UpdateExecutionFactory decoratee;

    public UpdateExecutionFactoryDecorator(UpdateExecutionFactory decoratee) {
        this.decoratee = decoratee;
    }

//    @Override
//    public String getId() {
//        return decoratee.getId();
//    }
//
//    @Override
//    public String getState() {
//        return decoratee.getState();
//    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        return decoratee.createUpdateProcessor(updateRequest);
    }

    @Override
    public UpdateProcessor createUpdateProcessor(String updateRequestStr) {
        return decoratee.createUpdateProcessor(updateRequestStr);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> clazz) {
        T result;
        if(getClass().isAssignableFrom(clazz)) {
            result = (T)this;
        }
        else {
            result = decoratee.unwrap(clazz);
        }

        return result;
    }

    @Override
    public void close() {
        try {
            decoratee.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

