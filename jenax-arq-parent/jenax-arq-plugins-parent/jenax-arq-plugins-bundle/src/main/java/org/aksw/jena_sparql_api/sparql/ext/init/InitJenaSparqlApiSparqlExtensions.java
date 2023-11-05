package org.aksw.jena_sparql_api.sparql.ext.init;

import org.aksw.jena_sparql_api.sparql.ext.binding.JenaExtensionBinding;
import org.aksw.jena_sparql_api.sparql.ext.collection.array.JenaExtensionArray;
import org.aksw.jena_sparql_api.sparql.ext.collection.base.JenaExtensionCollection;
import org.aksw.jena_sparql_api.sparql.ext.collection.set.JenaExtensionSet;
import org.aksw.jena_sparql_api.sparql.ext.csv.JenaExtensionCsv;
import org.aksw.jena_sparql_api.sparql.ext.datatypes.JenaExtensionDuration;
import org.aksw.jena_sparql_api.sparql.ext.distinct.JenaPluginConditionalDistinct;
import org.aksw.jena_sparql_api.sparql.ext.fs.JenaExtensionFs;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.JenaExtensionsGeoSparqlX;
import org.aksw.jena_sparql_api.sparql.ext.gml.JenaExtensionGml;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaExtensionJson;
import org.aksw.jena_sparql_api.sparql.ext.mvn.JenaExtensionsMvn;
import org.aksw.jena_sparql_api.sparql.ext.number.JenaExtensionNumber;
import org.aksw.jena_sparql_api.sparql.ext.osrm.JenaExtensionOsrm;
import org.aksw.jena_sparql_api.sparql.ext.path.JenaExtensionsPath;
import org.aksw.jena_sparql_api.sparql.ext.prefix.JenaExtensionPrefix;
import org.aksw.jena_sparql_api.sparql.ext.str.JenaExtensionString;
import org.aksw.jena_sparql_api.sparql.ext.sys.JenaExtensionSys;
import org.aksw.jena_sparql_api.sparql.ext.url.JenaExtensionUrl;
import org.aksw.jena_sparql_api.sparql.ext.xml.JenaExtensionXml;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.aksw.jenax.arq.functionbinder.FunctionRegistryWithAutoProxying;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitJenaSparqlApiSparqlExtensions
    implements JenaSubsystemLifecycle
{
    private static final Logger logger = LoggerFactory.getLogger(InitJenaSparqlApiSparqlExtensions.class);

    @Override
    public void start() {
        logger.debug("Initializing JenaX SPARQL extensions");

        JenaExtensionJson.register();
        JenaExtensionCsv.register();
        JenaExtensionGml.register();
        JenaExtensionXml.register();
        JenaExtensionUrl.register();
        JenaExtensionFs.register();
        JenaExtensionSys.register();
        JenaExtensionsGeoSparqlX.register();
        JenaExtensionDuration.register();
        JenaExtensionOsrm.register();
        JenaExtensionArray.register();
        JenaExtensionSet.register();
        JenaExtensionCollection.register();
        JenaExtensionPrefix.register();
        JenaExtensionString.register();
        JenaExtensionNumber.register();
        JenaExtensionBinding.register();

        JenaExtensionsMvn.register();
        JenaExtensionsPath.register();
        //JenaExtensionsGeoSparql.loadDefs(registry);

        FunctionBinder binder = FunctionBinders.getDefaultFunctionBinder();
        binder.getFunctionGenerator().getConverterRegistry();


        FunctionRegistry original = FunctionRegistry.get(ARQ.getContext());
        FunctionRegistryWithAutoProxying replacement = new FunctionRegistryWithAutoProxying();
        original.keys().forEachRemaining(k -> replacement.put(k, original.get(k)));
        FunctionRegistry.set(ARQ.getContext(), replacement);

        JenaPluginConditionalDistinct.register(ServiceExecutorRegistry.get());
    }

    @Override
    public void stop() {
    }

    @Override
    public int level() {
        return JenaSubsystemLifecycle.super.level();
    }
}
