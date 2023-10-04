package org.aksw.jenax.web.servlet;

import org.aksw.jenax.graphql.GraphQlExecFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GraphQlEndpointImpl
    extends GraphQlEndpointBase
{
    /** The connection factory is mandatory. It creates RDFConnections from the http request. */
    @Autowired
    protected GraphQlExecFactory graphQlExecFactory;

    @Override
    protected GraphQlExecFactory getGraphQlExecFactory() {
        return graphQlExecFactory;
    }
}
