package org.aksw.jenax.arq.picocli;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.aksw.jenax.arq.util.exec.query.ContextUtils;
import org.apache.jena.geosparql.InitGeoSPARQL;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.mgt.Explain.InfoLevel;
import org.apache.jena.sparql.util.Context;

import picocli.CommandLine.Option;

public class CmdMixinArq
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Option(names = { "--explain" }, description="Enable detailed ARQ log output")
    public boolean explain = false;

    @Option(names = { "--set" }, description="Set ARQ options (key=value)", mapFallbackValue="true")
    public Map<String, String> arqOptions = new HashMap<>();

    @Option(names = { "--rdf10" }, description = "RDF 1.0 mode; e.g. xsd:string on literals matter (no longer supported by Jena5)", defaultValue = "false")
    public boolean useRdf10 = false;

    @Option(names = { "--geoindex" },  description = "Build Geoindex")
    public boolean geoindex;

    @Option(names = { "--geoindex-srs" },  arity="0..1", description = "GeoIndex SRS. If absent then data will be scanned for SRS. If given without argument then the default SRS will be used.", defaultValue = SRS_URI.DEFAULT_WKT_CRS84, fallbackValue = SRS_URI.DEFAULT_WKT_CRS84)
    public String geoindexSrs;

    @Option(names = { "--geoindex-file" },  description = "Geoindex filename")
    public Path geoindexFile;


    /** Sets global options - does not configure context-specific options  */
    public static void configureGlobal(CmdMixinArq cmd) {
        // JenaRuntime.isRDF11 = !cmd.useRdf10;

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
