package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris.BnodeRewriteMode;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceWrapperBase;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Blank node profile probing is only activated with if the given profile name is set to "auto". */
public class RdfDataSourceWithBnodeRewrite
    extends RDFDataSourceWrapperBase<RDFDataSource>
{
    private static final Logger logger = LoggerFactory.getLogger(RdfDataSourceWithBnodeRewrite.class);

    public static final String AUTO = "auto";

    protected String givenProfileName;
    protected String derivedProfileName;
    protected BnodeRewriteMode rewriteMode;

    // null = not yet initialized, empty = no suitable transformer found
    protected Optional<ExprTransformVirtualBnodeUris> transformer = null;

    public RdfDataSourceWithBnodeRewrite(RDFDataSource delegate, String givenProfileName, BnodeRewriteMode rewriteMode) {
        super(delegate);
        this.givenProfileName = givenProfileName;
        this.derivedProfileName = null;
        this.rewriteMode = Objects.requireNonNull(rewriteMode);
    }

    public String getGivenProfileName() {
        return givenProfileName;
    }

    public String getInferredProfileName() {
        return derivedProfileName;
    }

    public BnodeRewriteMode getRewriteMode() {
        return rewriteMode;
    }

    public static ExprTransformVirtualBnodeUris getTransform(String profile, BnodeRewriteMode rewriteMode) {
        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        SparqlStmtMgr.execSparql(model, "udf-inferences.rq");

        // Set<String> activeProfiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/" + profile));
        Set<String> activeProfiles = Set.of(profile);
        ExprTransformVirtualBnodeUris result = ExprTransformVirtualBnodeUris.createTransformFromUdfModel(model, activeProfiles, rewriteMode);
        return result;
    }

    @Override
    public RDFConnection getConnection() {
        // TODO Do probing, block further requests, allow for async shutdown
        RDFConnection base = getDelegate().getConnection();

        RDFConnection result;
        if (transformer == null) {
            if ("auto".equalsIgnoreCase(givenProfileName)) {
                derivedProfileName = RdfDataSourcePolyfill.detectProfile(base);
            } else {
                derivedProfileName = givenProfileName;
            }

            if(derivedProfileName != null) {
                ExprTransformVirtualBnodeUris tmp = getTransform(derivedProfileName, rewriteMode);
                transformer = Optional.ofNullable(tmp);
            } else {
                transformer = Optional.empty();
            }
        }

        if (transformer.isPresent()) {
            result = RDFConnectionUtils.wrapWithQueryTransform(base, transformer.get()::rewrite);
        } else {
            logger.warn("No bnode profile found - bnodes are not supported");
            result = base;
        }

        return result;
    }

    public static RdfDataSourceWithBnodeRewrite wrapWithAutoBnodeProfileDetection(RDFDataSource delegate, BnodeRewriteMode rewriteMode) {
        return new RdfDataSourceWithBnodeRewrite(delegate, AUTO, rewriteMode);
    }
}
