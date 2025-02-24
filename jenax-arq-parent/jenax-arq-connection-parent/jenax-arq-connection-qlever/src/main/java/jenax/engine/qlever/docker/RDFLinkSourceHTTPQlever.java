package jenax.engine.qlever.docker;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.engine.RDFLinkSourceHTTP;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderHTTP;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateWrapperBase;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.exec.http.UpdateExecHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateSendMode;

/**
 * The link source depends on the service control such that it
 * can compute its fresh endpoint URL after container restart.
 */
public class RDFLinkSourceHTTPQlever
    implements RDFLinkSourceHTTP
{
    protected ServiceControlQlever serviceControl;

    public RDFLinkSourceHTTPQlever(ServiceControlQlever serviceControl) {
        super();
        this.serviceControl = Objects.requireNonNull(serviceControl);
    }

    @Override
    public RDFLinkBuilderHTTP<?> newLinkBuilder() {
        String serviceUrl = serviceControl.getDestination();

        RDFLinkBuilderHTTP<?> result = new RDFLinkBuilderHTTP<>();
        result.destination(serviceUrl);
        LinkSparqlUpdateTransform transformAccessToken = transformInjectAccessToken();
        if (transformAccessToken != null) {
            result.linkTransform(RDFLinkTransforms.of(transformAccessToken));
        }
        return result;
    }

    protected LinkSparqlUpdateTransform transformInjectAccessToken() {
        // Automatically inject a provided access token on update requests
        String accessToken = serviceControl.getConfig().getAccessToken();

        LinkSparqlUpdateTransform result = accessToken == null
            ? null
            : lsu -> {
                return new LinkSparqlUpdateWrapperBase(lsu) {
                    @Override
                    public UpdateExecBuilder newUpdate() {
                        UpdateExecBuilder base = lsu.newUpdate();
                        UpdateExecHTTPBuilder http = (UpdateExecHTTPBuilder)base;
                        http.sendMode(UpdateSendMode.asPostForm);
                        http.param("access-token", accessToken);
                        return http;
                    }
                };
            };
       return result;
    }
}
