package org.aksw.jenax.arq.engine.quad;

import org.aksw.jenax.dataaccess.sparql.factory.dataset.connection.DatasetRDFConnectionFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataset.connection.DatasetRDFConnectionFactoryBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.util.Context;

public class RDFConnectionFactoryQuadForm {

    /** Connect to a dataset using the quad form engine */
    public static RDFConnection connect(Dataset dataset) {
        return connect(dataset, null);
    }

    public static DatasetRDFConnectionFactory createFactory(Context context) {
        return DatasetRDFConnectionFactoryBuilder.create()
            .setQueryEngineFactoryProvider(QueryEngineMainQuadForm.FACTORY)
            .setUpdateEngineFactory(UpdateEngineMainQuadForm.FACTORY)
            .setContext(context)
            .build();
    }

    public static RDFConnection connect(Dataset dataset, Context context) {
        RDFConnection result = createFactory(context)
                .connect(dataset);

        return result;
    }

}
