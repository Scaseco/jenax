package org.aksw.jena_sparql_api.rx.script;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Lists;
import org.aksw.jena_sparql_api.rx.RDFIterator;
import org.aksw.jenax.arq.util.node.NodeEnvsubst;
import org.aksw.jenax.arq.util.update.UpdateRequestUtils;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.aksw.jenax.stmt.core.*;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserWrapperSelectShortForm;
import org.aksw.jenax.stmt.parser.update.SparqlUpdateParser;
import org.aksw.jenax.stmt.parser.update.SparqlUpdateParserImpl;
import org.aksw.jenax.stmt.util.SparqlStmtIterator;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.modify.request.UpdateLoad;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.SplitIRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Super-convenient SPARQL statement loader. Probes arguments whether they are inline SPARQL statements or refer to files.
 * Referred files may contain RDF or sequences of SPARQL statements.
 * RDF files are loaded fully into memory as UpdateModify statements.
 *
 * Usually the SparqlQueryParserWrapperSelectShortForm should be active which allows omitting the SELECT keyword making
 * querying even less verbose
 *
 * Prefixes from an input source carry over to the next. Hence, if an RDF file is loaded, its prefixes can be used in
 * subsequent SPARQL statements without need for declaration.
 *
 *
 * For example assuming that mydata defines a foo prefix
 * ad-hoc querying becomes possible simply using the arguments ["people.ttl", "?s { ?s a foaf:Person }"]
 *
 * Arguments that start with cwd=/some/path sets the current working directory on which SPARQL queries operate.
 * Effectively it sets the base URL of the following SPARQL queries.
 * Relative paths are resolved against the current working directory as reported by the JVM.
 * Use "cwd=" (with an empty string) to reset the CWD to that of the JVM
 *
 * @author Claus Stadler
 *
 */
public class SparqlScriptProcessor {

    /**
     * Provenance of SPARQL statements - file:line:column
     *
     * @author Claus Stadler
     *
     */
    public static class Provenance {
        public Provenance(String arg) {
            this(arg, null, null);
        }

        public Provenance(String argStr, Long line, Long column) {
            super();
            this.argStr = argStr;
            this.line = line;
            this.column = column;
            this.sourceLocation = null;
        }


        // non-null if the query orginated from a sparql file
        protected String sourceLocation;
        protected String sourceNamespace;
        protected String sourceLocalName;

        /**
         *  The orginal argument string
         */
        protected String argStr;

        protected Long line;

        protected Long column;

        public String getSourceLocation() {
            return sourceLocation;
        }

        public void setSourceLocation(String sparqlPath) {
            this.sourceLocation = sparqlPath;
        }

        public String getSourceNamespace() {
            return sourceNamespace;
        }

        public void setSourceNamespace(String sourceNamespace) {
            this.sourceNamespace = sourceNamespace;
        }

        public String getSourceLocalName() {
            return sourceLocalName;
        }

        public void setSourceLocalName(String sourceLocalName) {
            this.sourceLocalName = sourceLocalName;
        }

        @Override
        public String toString() {
            String result = argStr +
                    (line == null ? (column == null ? "" : ":") : ":" + line) +
                    (column == null ? "" : ":" + column);
            return result;
        }
    }

    public static final String cwdKey = "cwd=";
    public static final String cwdResetCwd = "cwd";

    private static final Logger logger = LoggerFactory.getLogger(SparqlScriptProcessor.class);

    protected Function<? super Prologue, ? extends SparqlStmtParser> sparqlParserFactory; //  SparqlStmtParser sparqlParser ;

    /**
     * The set of global prefixes will be extended with the prefixes of every parsed query.
     * Set this attribute to null to disable global prefixes.
     */
    protected PrefixMapping globalPrefixes;
    protected Path cwd = null;
    protected List<Entry<SparqlStmt, Provenance>> sparqlStmts = new ArrayList<>();
    protected List<Function<? super SparqlStmt, ? extends SparqlStmt>> postTransformers = new ArrayList<>();

    public SparqlScriptProcessor(//SparqlStmtParser sparqlParser,
            Function<? super Prologue, ? extends SparqlStmtParser> sparqlParserFactory,
            PrefixMapping globalPrefixes) {
        super();
//        this.sparqlParser = sparqlParser;
        this.sparqlParserFactory = sparqlParserFactory;
        this.globalPrefixes = globalPrefixes;
    }

    public void addPostTransformer(Function<? super SparqlStmt, ? extends SparqlStmt> transformer) {
        postTransformers.add(transformer);
    }

    public void addPostMutator(Consumer<? super SparqlStmt> mutator) {
        postTransformers.add(stmt -> { mutator.accept(stmt); return stmt; });
    }

    public List<Entry<SparqlStmt, Provenance>> getSparqlStmts() {
        return sparqlStmts;
    }

    public List<SparqlStmt> getPlainSparqlStmts() {
        return Lists.transform(sparqlStmts, Entry::getKey);
    }

    public SparqlStmtParser getSparqlParser() {
        // return sparqlParserFactory.apply(PrologueUtils.newPrologueAsGiven(globalPrefixes));

        // With Jena 4.6.0 the E_IRI function no longer works with relative base IRIs
        // due to explicit checks
        return sparqlParserFactory.apply(new Prologue(globalPrefixes));
    }

    public static SparqlStmtParser createParserPlain(Prologue prologue, String base) {
        SparqlQueryParser queryParser = SparqlQueryParserWrapperSelectShortForm.wrap(
                // SparqlQueryParserImpl.create(Syntax.syntaxARQ, prologue));
                SparqlQueryParserImpl.create(Syntax.syntaxARQ, prologue, base, null));


        SparqlUpdateParser updateParser = SparqlUpdateParserImpl
                // .create(Syntax.syntaxARQ, prologue);
                .create(Syntax.syntaxARQ, prologue, base, null);

        return new SparqlStmtParserImpl(queryParser, updateParser, base, true);
    }

    public static SparqlStmtParser createParserWithEnvSubstitution(Prologue prologue) {
        SparqlStmtParser core = createParserPlain(prologue, prologue.getBaseURI());
        SparqlStmtParser sparqlParser =
                SparqlStmtParser.wrapWithTransform(
                       core,
                        stmt -> SparqlStmtUtils.applyNodeTransform(stmt, x -> NodeEnvsubst.subst(x, System::getenv)));

        return sparqlParser;
    }


    /**
     * Create a script processor that substitutes references to environment variables
     * with the appropriate values.
     *
     * @param pm
     * @return
     */
    public static SparqlScriptProcessor createWithEnvSubstitution(PrefixMapping globalPrefixes) {
        SparqlScriptProcessor result = new SparqlScriptProcessor(
                SparqlScriptProcessor::createParserWithEnvSubstitution,
                globalPrefixes);
        return result;
    }

    public static SparqlScriptProcessor createPlain(PrefixMapping globalPrefixes, String base) {
        SparqlScriptProcessor result = new SparqlScriptProcessor(
                prologue -> SparqlScriptProcessor.createParserPlain(prologue, base),
                globalPrefixes);
        return result;
    }




    public void process(List<String> filenames) {
        int i = 1;
        for (String filename : filenames) {
            process(i++, filename);
        }
    }

    public void processPaths(Collection<Path> paths) {
        int i = 1;
        for (Path path : paths) {
            process(i++, path);
        }
    }

    public void process(int index, Path path) {
        logger.info("Interpreting path" + (index >= 0 ? " #" + index : "" ) + ": '" + path + "'");
        List<SparqlStmt> stmts = SparqlStmtMgr.loadSparqlStmts(path, getSparqlParser());
        for (SparqlStmt stmt : stmts) {
            sparqlStmts.add(new SimpleEntry<>(stmt, new Provenance(path.toString())));
        }
    }

    public void process(String filename) {
        process(-1, filename);
    }

    public void process(int index, String filename) {
        process(index, filename, sparqlStmts);
    }

    public void process(int index, String filename, List<Entry<SparqlStmt, Provenance>> result) {
        logger.info("Interpreting argument" + (index >= 0 ? " #" + index : "" ) + ": '" + filename + "'");

        if(filename.startsWith(cwdKey)) {
            String cwdValue = filename.substring(cwdKey.length()).trim();

            if(cwd == null) {
                cwd = Paths.get(StandardSystemProperty.USER_DIR.value());
            }

            cwd = cwd.resolve(cwdValue);
            logger.info("Pinned working directory to " + cwd);
        } else if(filename.equals(cwdResetCwd)) {
            // If cwdValue is an empty string, reset the working directory
            logger.info("Unpinned working directory");

            cwd = null;
        } else {

            boolean isProcessed = false;
            Collection<Entry<Lang, Throwable>> rdfErrorCollector = new ArrayList<>();
            loadAsRdf: try {
                Provenance prov = new Provenance(filename);
                UpdateRequest ur = tryLoadFileAsUpdateRequest(filename, globalPrefixes, rdfErrorCollector);

                if (ur == null)
                    break loadAsRdf;

                result.add(new SimpleEntry<>(new SparqlStmtUpdate(ur), prov));

                isProcessed = true;
            } catch (Exception e) {
                logger.debug("Probing " + filename + " as RDF data file failed", e);
            }

            Collection<Throwable> stmtErrorCollector = new ArrayList<>();
            if (!isProcessed) {

                String baseIri = cwd == null ? null : cwd.toUri().toString();
                try {
//                    Iterator<SparqlStmt> it = SparqlStmtMgr.loadSparqlStmts(filename, globalPrefixes, sparqlParser, baseIri);
                    // globalPrefixes,
                    IRIxResolver resolver = IRIxResolver.create(baseIri).allowRelative(true).build();
                    // resolver = IRIxResolverUtils.newIRIxResolverAsGiven(baseIri
                    Prologue prologue = new Prologue(
                            globalPrefixes == null ? new PrefixMappingImpl() : globalPrefixes,
                            resolver);
                            // IRIxResolver.create(baseIri).build());
                    SparqlStmtParser sparqlParser = sparqlParserFactory.apply(prologue);


                    TypedInputStream tmpIn = null;
                    Iterator<SparqlStmt> it;

                    try {
                        // On NPE we enter the catch block with the fallback strategy
                        tmpIn = SparqlStmtUtils.openInputStream(filename);
                        if (tmpIn == null) {
                            throw new FileSystemException(filename);
                        }

                        it = SparqlStmtUtils.parse(tmpIn, sparqlParser);
                        it.hasNext();

                        logger.debug("Attempting to interpret argument as a file containing sparql statements");
                    } catch (Exception e) {
                        stmtErrorCollector.add(e);
                        try {
                            if (tmpIn != null) {
                                tmpIn.close();
                            }

                            tmpIn = new TypedInputStream(new ByteArrayInputStream(filename.getBytes()), null, null);
                            it = SparqlStmtUtils.parse(tmpIn, sparqlParser);
                            it.hasNext();
                        } catch (Exception f) {
                            stmtErrorCollector.add(f);
                            Throwable cause = f.getCause();
                            if (!SparqlStmtUtils.isEncounteredSlashException(cause)) {
                                throw new RuntimeException(filename + " could not be opened and failed to parse as SPARQL query", f);
                            } else {
                                throw new RuntimeException("Could not parse " + filename, e);
                            }
                        }
                    }

                    try (TypedInputStream in = tmpIn) {

                        //Path sparqlPath = Paths.get(filename).toAbsolutePath();
                        String sourceLocation = in.getBaseURI();
                        String sourceNamespace = sourceLocation == null ? null : SplitIRI.namespace(sourceLocation);
                        String sourceLocalName = sourceLocation == null ? null : SplitIRI.localname(sourceLocation);

                        String effNamespace = cwd == null ? sourceNamespace : cwd.toUri().toString();

                        SparqlStmtIterator itWithPos = it instanceof SparqlStmtIterator
                                ? (SparqlStmtIterator)it
                                : null;

                        while(it.hasNext()) {
                            Provenance prov;
                            if(itWithPos != null) {
                                prov = new Provenance(filename, (long)itWithPos.getLine(), (long)itWithPos.getColumn());
                                logger.info("Preparing SPARQL statement at line " + itWithPos.getLine() + ", column " + itWithPos.getColumn());
                            } else {
                                prov = new Provenance(filename);
                                logger.info("Preparing inline SPARQL argument " + filename);
                            }
                            prov.setSourceLocation(sourceLocation);
                            prov.setSourceNamespace(effNamespace);
                            prov.setSourceLocalName(sourceLocalName);

                            SparqlStmt stmt = it.next();

                            if (!stmt.isParsed()) {
                                throw new RuntimeException(stmt.getParseException());
                            }

                            if (globalPrefixes != null) {
                                PrefixMapping stmtPrefixes = stmt.getPrefixMapping();
                                if(stmtPrefixes != null) {
                                    globalPrefixes.setNsPrefixes(stmtPrefixes);
                                }
                            }

                            // TODO: Move optimizePrefixes to transformers?
                            //SparqlStmtUtils.optimizePrefixes(stmt);

                            for (Function<? super SparqlStmt, ? extends SparqlStmt> postTransformer : postTransformers) {
                                SparqlStmt tmp = postTransformer.apply(stmt);
                                stmt = Objects.requireNonNull(tmp, "Transformations yeld null " + postTransformer);
                            }

                            result.add(new SimpleEntry<>(stmt, prov));
                        }
                    }
                } catch (Exception e) {
                    if (e.getCause() != null && stmtErrorCollector.contains(e.getCause()))
                        stmtErrorCollector.remove(e.getCause());
                    stmtErrorCollector.add(e);
                    String message = "Failed to process argument" + (index >= 0 ? " #" + index : "") + ": " + filename;
                    MultiException multiException = new MultiException(message, rdfErrorCollector, stmtErrorCollector);
                    throw new RuntimeException(message, multiException);
                }
            }
        }

    }


    public static UpdateRequest tryLoadFileAsUpdateRequest(String filename, PrefixMapping globalPrefixes, Collection<Entry<Lang, Throwable>> errorCollector) throws IOException {
        UpdateRequest result = null;

        // TODO We should map to filename through the stream manager
//        String str = StreamManager.get().mapURI(filename);

        // Try as RDF file
        try(TypedInputStream tmpIn = RDFDataMgrEx.open(filename, Arrays.asList(Lang.TRIG, Lang.NQUADS, Lang.JSONLD, Lang.RDFXML), errorCollector)) {
//            if(tmpIn == null) {
//                throw new FileNotFoundException(filename);
//            }


            // Unwrap the input stream for less overhead
            InputStream in = tmpIn.getInputStream();


            String contentType = tmpIn.getContentType();
            if (contentType == null) {
                logger.info("Argument does not appear to be (RDF) data because content type probing yeld no result");
            } else {
                logger.info("Detected data format: " + contentType);
            }
            Lang rdfLang = contentType == null ? null : RDFLanguages.contentTypeToLang(contentType);

            //Lang rdfLang = RDFDataMgr.determineLang(filename, null, null);
            if(rdfLang != null) {

                RDFIterator<?> itTmp;
                // FIXME Validate we are really using turtle/trig here
                if(RDFLanguages.isTriples(rdfLang)) {
                    itTmp = RDFDataMgrEx.createIteratorTriples(globalPrefixes, in, Lang.TTL);
                } else if(RDFLanguages.isQuads(rdfLang)) {
                    itTmp = RDFDataMgrEx.createIteratorQuads(globalPrefixes, in, Lang.TRIG);
                } else {
                    throw new RuntimeException("Unknown lang: " + rdfLang);
                }


                int window = 100;
                try (RDFIterator<?> it = itTmp) {
                    int remaining = window;
                    while (it.hasNext()) {
                        --remaining;
                        if (remaining == 0) {
                            PrefixMap pm = it.getPrefixes();
                            // FIXME This log message should display how many prefixes were actually gathered from the file
                            //  The total number of prefixes includes preconfigured ones
                            logger.info("A total of " + pm.size() + " prefixes known after processing " + filename);
                            globalPrefixes.setNsPrefixes(pm.getMapping());
                            break;
                        }

                        if (it.prefixesChanged()) {
                            remaining = 100;
                        }

                        it.next();
                    }
                }

                // String fileUrl = "file://" + Paths.get(filename).toAbsolutePath().normalize().toString();
                result = new UpdateRequest(new UpdateLoad(filename, (Node)null));
            }
        }
        return result;
    }


    public static UpdateRequest tryLoadFileAsUpdateRequestOld(String filename, PrefixMapping globalPrefixes) throws IOException {
        UpdateRequest result = null;

        // Try as RDF file
        try(TypedInputStream tmpIn = RDFDataMgrEx.open(filename, Arrays.asList(Lang.TRIG, Lang.NQUADS))) {
//            if(tmpIn == null) {
//                throw new FileNotFoundException(filename);
//            }

            InputStream in = tmpIn.getInputStream();


            String contentType = tmpIn.getContentType();
            logger.info("Detected format: " + contentType);
            Lang rdfLang = contentType == null ? null : RDFLanguages.contentTypeToLang(contentType);

            //Lang rdfLang = RDFDataMgr.determineLang(filename, null, null);
            if(rdfLang != null) {

                if(RDFLanguages.isTriples(rdfLang)) {

                    Model tmp = ModelFactory.createDefaultModel();
                    //InputStream in = SparqlStmtUtils.openInputStream(filename);
                    // FIXME Validate we are really using turtle here
                    RDFDataMgrEx.parseTurtleAgainstModel(tmp, globalPrefixes, in);
                    // Copy any prefixes from the parse back to our global prefix mapping
                    globalPrefixes.setNsPrefixes(tmp);

                    // Convert the model to a SPARQL insert statement
                    result = UpdateRequestUtils.createUpdateRequest(tmp, null);

                } else if(RDFLanguages.isQuads(rdfLang)) {
                    Dataset tmp = DatasetFactory.create();
                    // InputStream in = SparqlStmtUtils.openInputStream(filename);

                    // FIXME Validate we are really using turtle here
                    RDFDataMgrEx.parseTrigAgainstDataset(tmp, globalPrefixes, in);
                    // Copy any prefixes from the parse back to our global prefix mapping

                    Model m = tmp.getDefaultModel();
                    if(m != null) {
                        globalPrefixes.setNsPrefixes(m);
                    }

                    logger.info("Gathering prefixes from named graphs...");
                    int i = 0;
                    Iterator<String> it = tmp.listNames();
                    while(it.hasNext()) {
                        String name = it.next();
                        m = tmp.getNamedModel(name);
                        if(m != null) {
                            ++i;
                            globalPrefixes.setNsPrefixes(m);
                        }
                    }
                    logger.info("Gathered prefixes from " + i + " named graphs");

                    result = UpdateRequestUtils.createUpdateRequest(tmp, null);

                } else {
                    throw new RuntimeException("Unknown lang: " + rdfLang);
                }

            }
        }
        return result;
    }
}
