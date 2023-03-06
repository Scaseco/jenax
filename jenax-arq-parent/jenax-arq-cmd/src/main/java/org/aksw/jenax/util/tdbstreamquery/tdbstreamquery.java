package org.aksw.jenax.util.tdbstreamquery;

import arq.cmdline.ModDataset;
import arq.query;
import org.aksw.jenax.stmt.resultset.SPARQLResultEx;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.query.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.Txn;
import tdb2.cmdline.CmdTDB;
import tdb2.cmdline.ModTDBDataset;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

class tdbstreamquery extends query {

    public static void main(String... argv) {
        CmdTDB.init();
        new tdbstreamquery(argv).mainRun();
    }

    @Override
    protected ModDataset setModDataset() {
        return new ModTDBDataset();
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " --loc=<path> --query=<query>";
    }

    static Map<ResultsFormat, Lang> resultsFormatToLangMap;

    static {
        resultsFormatToLangMap = new HashMap<>();
        resultsFormatToLangMap.put(ResultsFormat.FMT_RDF_XML, Lang.RDFXML);
        resultsFormatToLangMap.put(ResultsFormat.FMT_RDF_N3, Lang.N3);
        resultsFormatToLangMap.put(ResultsFormat.FMT_RDF_TTL, Lang.TURTLE);
        resultsFormatToLangMap.put(ResultsFormat.FMT_RDF_NT, Lang.NTRIPLES);
        resultsFormatToLangMap.put(ResultsFormat.FMT_RDF_JSONLD, Lang.JSONLD);
        resultsFormatToLangMap.put(ResultsFormat.FMT_RDF_NQ, Lang.NQUADS);
        resultsFormatToLangMap.put(ResultsFormat.FMT_RDF_TRIG, Lang.TRIG);
    }

    protected static Lang convert(ResultsFormat fmt) {
        Lang lang = ResultsFormat.convert(fmt);
        if (lang != null)
            return lang;
        return resultsFormatToLangMap.get(fmt);
    }

    @Override
    protected void queryExec(boolean timed, ResultsFormat fmt, PrintStream resultsDest) {

        Context cxt = null;


        try {
            Query query = getQuery();
            Lang lang = defaultLang(convert(fmt), query);

            if (isVerbose()) {
                IndentedWriter out = new IndentedWriter(resultsDest, true);
                query.serialize(out);
                out.setLineNumbers(false);
                out.println();
                out.flush();
            }

            if (isQuiet())
                LogCtl.setError(SysRIOT.riotLoggerName);
            Dataset dataset = getDataset(query);
            // Check there is a dataset. See dealWithNoDataset(query).
            // The default policy is to create an empty one - convenience for VALUES and BIND providing the data.
            if (dataset == null && !query.hasDatasetDescription()) {
                System.err.println("Dataset not specified in query nor provided on command line.");
                throw new TerminationException(1);
            }
            Transactional transactional = (dataset != null && dataset.supportsTransactions()) ? dataset : new TransactionalNull();
            Txn.executeRead(transactional, () -> {
                modTime.startTimer();
                try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
                    SPARQLResultEx sr = SparqlStmtUtils.execAny(qe, query);
                    ResultSetWriterFactory rsWriterFactory = ResultSetWriterRegistry.getFactory(lang);

                    if (rsWriterFactory != null) {
                        ResultSetWriter rsWriter = rsWriterFactory.create(lang);
                        if (sr.isBoolean()) {
                            boolean v = sr.getBooleanResult();
                            rsWriter.write(out, v, cxt);
                        } else {
                            rsWriter.write(out, sr.getResultSet(), cxt);
                        }
                    } else if (StreamRDFWriter.registered(lang)) {
                        StreamRDF writer = StreamRDFWriter.getWriterStream(out, lang, cxt);
                        writer.start();

                        if (sr.isQuads()) {
                            StreamRDFOps.sendQuadsToStream(sr.getQuads(), writer);
                        } else if (sr.isTriples()) {
                            StreamRDFOps.sendTriplesToStream(sr.getTriples(), writer);
                        }

                        writer.finish();
                    } else {
                        throw new CmdException("Could not handle execution of query " + query + " with lang " + lang);
                    }

                    long time = modTime.endTimer();
                    if (timed) {
                        totalTime += time;
                        System.err.println("Time: " + modTime.timeStr(time) + " sec");
                    }
                } catch (ResultSetException ex) {
                    System.err.println(ex.getMessage());
                    ex.printStackTrace(System.err);
                } catch (QueryException qEx) {
                    // System.err.println(qEx.getMessage()) ;
                    throw new CmdException("Query Exeception", qEx);
                }
            });
        } catch (ARQInternalErrorException intEx) {
            System.err.println(intEx.getMessage());
            if (intEx.getCause() != null) {
                System.err.println("Cause:");
                intEx.getCause().printStackTrace(System.err);
                System.err.println();
            }
            intEx.printStackTrace(System.err);
        } catch (JenaException | CmdException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CmdException("Exception", ex);
        }
    }

    static Lang defaultLang(Lang lang, Query query) {
        if (lang != null)
            return lang;
        else if (query.isConstructQuad())
            return Lang.NQUADS;
        else if (query.isConstructType())
            return Lang.NTRIPLES;
        else if (query.isJsonType())
            return Lang.RDFJSON;
        else
            return ResultSetLang.RS_JSON;
    }
    public tdbstreamquery(String[] argv) {
        super(argv);
    }
}