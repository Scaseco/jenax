package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;


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
        SparqlStmtMgr.execSparql(model, "udf-inferences.sparql");

        Set<String> activeProfiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/" + profile));
        ExprTransformVirtualBnodeUris result = ExprTransformVirtualBnodeUris.createTransformFromUdfModel(model, activeProfiles);
        return result;
    }

    public static String detectProfile(RDFConnection conn) {
        String result = null;
        // if ("auto".equalsIgnoreCase(givenProfileName) && transformer == null) {
        // A helper dataset against which the probing query is run.
        // The dataset's context gets special service handler
        Dataset probeDs = DatasetFactory.create();
        ServiceExecutorRegistry registry = new ServiceExecutorRegistry();
        registry.addSingleLink((opExec, opOrig, binding, execCxt, chain) -> {
            QueryIterator r;
            if (opExec.getService().getURI().equals("env://REMOTE")) {
                // try {
                // These requests materialize the result set, so the connection should be idle
                // once execService returns
                    r = RDFConnectionUtils.execService(opExec, conn);
                    // RDFLinkAdapter.adapt(base).query(query).sel
                    // r = new QueryIteratorResultSet(base.query(query).execSelect());
//                    } catch (Exception e) {
//                        logger.warn("Probing failed", e);
//                    }
            } else {
                r = chain.createExecution(opExec, opOrig, binding, execCxt);
            }
            return r;
        });
        ServiceExecutorRegistry.set(probeDs.getContext(), registry);

        SparqlStmtMgr.execSparql(probeDs, "probe-endpoint-dbms.sparql");
        Property dbmsShortName = ResourceFactory.createProperty("http://www.example.org/dbmsShortName");

        Model report = probeDs.getDefaultModel();
        List<String> nodes = report.listObjectsOfProperty(dbmsShortName)
            .mapWith(n -> n.isLiteral() ? Objects.toString(n.asLiteral().getValue()) : null)
            .toList();
        String first = Iterables.getFirst(nodes, null);

        if(first != null) {
            result = first;
        }
        return result;
    }

    @Override
    public RDFConnection getConnection() {
        // TODO Do probing, block further requests, allow for async shutdown
        RDFConnection base = getDelegate().getConnection();

        RDFConnection result;
        if (transformer == null) {
            if ("auto".equalsIgnoreCase(givenProfileName)) {
                derivedProfileName = detectProfile(base);
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
