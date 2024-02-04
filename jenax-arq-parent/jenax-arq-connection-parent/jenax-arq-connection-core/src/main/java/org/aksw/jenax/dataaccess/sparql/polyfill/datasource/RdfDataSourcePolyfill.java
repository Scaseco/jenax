package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.jena_sparql_api.algebra.transform.TransformExpandAggCountDistinct;
import org.aksw.jena_sparql_api.algebra.transform.TransformFactorizeTableColumnsToExtend;
import org.aksw.jena_sparql_api.algebra.transform.TransformRedundantFilterRemoval;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransform;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;

import com.google.common.collect.Iterables;

/**
 * Detect (missing) features of a sparql endpoint and recommend a sequence of RdfDataSourceTransform
 * instances to polyfill those.
 *
 * This class should eventually be improved or extended into a more general
 * SPARQL endpoint feature detector.
 */
public class RdfDataSourcePolyfill {
    public static List<Suggestion<RdfDataSourceTransform>> suggestPolyfills(RdfDataSource rdfDataSource) {
        List<Suggestion<RdfDataSourceTransform>> result = null;
        String profile = RdfDataSources.compute(rdfDataSource, RdfDataSourcePolyfill::detectProfile);

        if (profile != null && profile.equals("virtuoso")) {
            result = virtuosoProfile();
        }


        if (result == null) {
            result = List.of();
        }

        return result;
    }

    public static List<Suggestion<RdfDataSourceTransform>> virtuosoProfile() {
        List<Suggestion<RdfDataSourceTransform>> result = new ArrayList<>();
        result.add(Suggestion.of("Generic - LATERAL", "Client-side execution of LATERAL", RdfDataSourceWithLocalLateral::wrap));
        result.add(Suggestion.of("Virtuoso - Rewrite empty table", "Rewrite VALUES blocks with empty bindings", ds -> RdfDataSources.wrapWithOpTransform(ds, TransformFactorizeTableColumnsToExtend::new)));
        result.add(Suggestion.of("Virtuoso - Rephrase COUNT(DISTINCT ?x)", "Rewrite as COUNT(*) over a sub query using DISTINCT ?x", ds -> RdfDataSources.wrapWithOpTransform(ds, TransformExpandAggCountDistinct::new)));
        result.add(Suggestion.of("Virtuoso - Remove redundant filters", "Some versions of Virtuoso raise errors for BIND('foo' AS ?x) FILTER(?x = 'foo')", ds -> RdfDataSources.wrapWithOpTransform(ds, TransformRedundantFilterRemoval::new)));
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
                    r = RDFConnectionUtils.execService(binding, execCxt, opExec, conn);
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
}
