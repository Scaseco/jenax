package org.aksw.jenax.arq.picocli;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.aksw.jenax.arq.util.exec.ContextUtils;
import org.apache.jena.JenaRuntime;
import org.apache.jena.geosparql.InitGeoSPARQL;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.mgt.Explain.InfoLevel;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.MappingRegistry;
import org.apache.jena.sparql.util.Symbol;

import picocli.CommandLine.Option;

public class CmdMixinArq
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Option(names = { "--explain" }, description="Enable detailed ARQ log output")
    public boolean explain = false;

    @Option(names = { "--set" }, description="Set ARQ options (key=value)", mapFallbackValue="true")
    public Map<String, String> arqOptions = new HashMap<>();

    @Option(names = { "--rdf10" }, description = "RDF 1.0 mode; e.g. xsd:string on literals matter", defaultValue = "false")
    public boolean useRdf10 = false;

    @Option(names = { "--geoindex" },  description = "Build Geoindex")
    public boolean geoindex;


    /** Sets global options - does not configure context-specific options  */
    public static void configureGlobal(CmdMixinArq cmd) {
        JenaRuntime.isRDF11 = !cmd.useRdf10;

        if (cmd.explain) {
            ARQ.setExecutionLogging(InfoLevel.ALL);
        }

        if (cmd.geoindex) {
            System.setProperty("jena.geosparql.skip", String.valueOf(false));
            //InitGeoSPARQL.start();
            new InitGeoSPARQL().start();
            GeoSPARQLConfig.setupNoIndex();
        }
    }

    public static void configureCxt(Context cxt, CmdMixinArq cmd) {
        // Automatically load external javascript functions from functions.js unless specified
        // Symbol jsLibrarySym =  ARQ.symJavaScriptLibFile; //Symbol.create(MappingRegistry.mapPrefixName("arq:js-library"));
        cxt.setIfUndef(ARQ.symJavaScriptLibFile, "functions.js");

        // Set arq options
        ContextUtils.putAll(cxt, cmd.arqOptions);
    }
}
