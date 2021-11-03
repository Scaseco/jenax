package org.aksw.jena_sparql_api.rx.dataset;

import java.util.List;
import java.util.function.Function;

import org.aksw.commons.io.syscall.SysCalls;
import org.aksw.commons.io.syscall.sort.SysSort;
import org.aksw.jenax.arq.util.query.CannedQueryUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.sparql.relation.dataset.NodesInDataset;
import org.aksw.jenax.sparql.relation.dataset.NodesInDatasetImpl;
import org.aksw.jenax.sparql.rx.op.ResultSetMappers;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import io.reactivex.rxjava3.core.FlowableTransformer;




/**
 * This class provides operators to map between individual objects and flowables of
 * Datasets, ResourceInDataset and NodesInDataset types.
 *
 * Dataset is a set of quads.
 *
 * ResourceInDataset is an abstraction providing Jena's convenient Resource API to a specific
 * resource in one specific model of the dataset's ones.
 * A change to that resource does not affect resources with the
 * same blank node / uri label in another model.
 *
 * NodesInDataset is used for efficiency in operators as it prevents
 * duplicate processing of datasets from which many ResourceInDataset instances are created.
 *
 *
 * @author raven
 *
 */
public class ResourceInDatasetFlowOps {


    public static FlowableTransformer<NodesInDataset, NodesInDataset> sysCallSort(
            Function<? super SparqlQueryConnection, Node> keyMapper,
            List<String> sysCallArgs) {

        return DatasetFlowOps.sysCallSortCore(
                gid -> ResultSetMappers.wrapForDataset(keyMapper).apply(gid.getDataset()),
                sysCallArgs,
                (key, data) -> DatasetFlowOps.serializeForSort(DatasetFlowOps.GSON, key, data),
                line -> DatasetFlowOps.deserializeFromSort(DatasetFlowOps.GSON, line, NodesInDatasetImpl.class)
                );
    }


    public static Function<? super SparqlQueryConnection, Node> createKeyMapper(
            String keyArg,
            Function<? super String, ? extends Query> queryParser,
            Query fallback) {
        //Function<Dataset, Node> keyMapper;

        Query effectiveKeyQuery;
        boolean useFallback = Strings.isNullOrEmpty(keyArg);
        if(!useFallback) {
            effectiveKeyQuery = queryParser.apply(keyArg);
            QueryUtils.optimizePrefixes(effectiveKeyQuery);
        } else {
            effectiveKeyQuery = fallback;
        }

        Function<? super SparqlQueryConnection, Node> result = ResultSetMappers.createNodeMapper(effectiveKeyQuery, NodeFactory.createLiteral(""));
        return result;
    }


    public static FlowableTransformer<NodesInDataset, NodesInDataset> createSystemSorter(
            SysSort cmdSort,
            SparqlQueryParser keyQueryParser) {
        String keyArg = cmdSort.key;

        Function<? super SparqlQueryConnection, Node> keyMapper = createKeyMapper(keyArg, keyQueryParser, CannedQueryUtils.DISTINCT_NAMED_GRAPHS);


//		keyQueryParser = keyQueryParser != null
//				? keyQueryParser
//				: SparqlQueryParserWrapperSelectShortForm.wrap(SparqlQueryParserImpl.create(DefaultPrefixes.prefixes));

        // SPARQL      : SELECT ?key { ?s eg:hash ?key }
        // Short SPARQL: ?key { ?s eg:hash ?key }
        // LDPath      : issue: what to use as the root?


        List<String> sortArgs = SysCalls.createDefaultSortSysCall(cmdSort);

        return sysCallSort(keyMapper, sortArgs);
    }

}
