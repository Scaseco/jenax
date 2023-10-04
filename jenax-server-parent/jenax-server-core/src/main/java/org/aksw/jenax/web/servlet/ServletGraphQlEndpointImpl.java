package org.aksw.jenax.web.servlet;

import javax.ws.rs.Path;

import org.aksw.jenax.graphql.GraphQlExecFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/")
public class ServletGraphQlEndpointImpl
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
