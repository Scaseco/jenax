package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Blank node profile probing is only activated with if the given profile name is set to "auto". */
public class RdfDataSourceWithBnodeRewrite
    extends RdfDataSourceWrapperBase<RdfDataSource>
{
    private static final Logger logger = LoggerFactory.getLogger(RdfDataSourceWithBnodeRewrite.class);

    public static final String AUTO = "auto";

    protected String givenProfileName;
    protected String derivedProfileName;

    // null = not yet initialized, empty = no suitable transformer found
    protected Optional<ExprTransformVirtualBnodeUris> transformer = null;

    public RdfDataSourceWithBnodeRewrite(RdfDataSource delegate, String givenProfileName) {
        super(delegate);
        this.givenProfileName = givenProfileName;
        this.derivedProfileName = null;
    }

    public String getGivenProfileName() {
        return givenProfileName;
    }

    public String getInferredProfileName() {
        return derivedProfileName;
    }

    public static ExprTransformVirtualBnodeUris getTransform(String profile) {
        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        SparqlStmtMgr.execSparql(model, "udf-inferences.rq");

        Set<String> activeProfiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/" + profile));
        ExprTransformVirtualBnodeUris result = ExprTransformVirtualBnodeUris.createTransformFromUdfModel(model, activeProfiles);
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
                ExprTransformVirtualBnodeUris tmp = getTransform(derivedProfileName);
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

    public static RdfDataSourceWithBnodeRewrite wrapWithAutoBnodeProfileDetection(RdfDataSource delegatee) {
        return new RdfDataSourceWithBnodeRewrite(delegatee, AUTO);
    }

}
