package org.aksw.jenax.engine.qlever;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;

import org.aksw.commons.util.docker.ContainerPathResolver;
import org.aksw.commons.util.exception.FinallyRunAll;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.aksw.jenax.arq.util.prefix.ShortNameMgr;
import org.aksw.jenax.dataaccess.sparql.creator.FileSet;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabaseBuilder;
import org.aksw.jenax.engine.qlever.QleverCliProberIndexBuilder.CliType;
import org.aksw.jenax.shellgebra.cmd.ArgsBuilderJena;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitorRenderAsBashString;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.transform.CmdOpVisitorToCmdString;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.CmdOpRewriter;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.processbuilder.ProcessBuilderDockerRun;
import org.aksw.vshell.registry.CmdExecSystem;
import org.aksw.vshell.registry.CmdExecSystem.CmdArgActiveProcessSubstitution;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.node.ArrayNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.node.ObjectNode;

import jenax.engine.qlever.docker.QleverConstants;

public class RDFDatabaseBuilderQlever<X extends RDFDatabaseBuilderQlever<X>>
    implements RDFDatabaseBuilder<X>
{
    /** Langs supported by this database builder. The builder may convert e.g. rdf/xml to ntriples for the backend. */
    public static final List<Lang> supportedInputLangs = Collections.unmodifiableList(Arrays.asList(Lang.TURTLE, Lang.NQUADS, Lang.RDFXML));

    // N-quads listed first because non-supported formats are converted to this by default.
    // Order matters here: N-triples and n-quads are the first conversion targets for triple/quad based languages.
    public static final List<Lang> supportedBackendLangs = Collections.unmodifiableList(Arrays.asList(Lang.NTRIPLES, Lang.NQUADS, Lang.TURTLE, Lang.NQUADS));

    private static final Logger logger = LoggerFactory.getLogger(RDFDatabaseBuilderQlever.class);

    /** Record to capture arguments passed to this builder */
    public record FileLoadEntry(Path path, Lang lang, List<String> encodings, Node graph, Boolean splittable) {}

    /** Record to capture a set of files that make up a Qlever database. */
    public record QleverDbFileSet(List<Path> paths) implements FileSet {
        @Override
        public List<Path> getPaths() {
            return paths;
        }
    }

    protected QleverIndexBuilderConfig config = new QleverIndexBuilderConfigPojo();

    /** Mapping from absolute file paths on the host names to file names. */
    protected ShortNameMgr shortNameMgr = new ShortNameMgr();
    protected SysRuntime sysRuntime;
    protected List<FileLoadEntry> args = new ArrayList<>();
    protected List<Entry<Lang, Throwable>> errorCollector = new ArrayList<>();

    /** Base path within the container where to mount any named pipes.
     *  Must end with '/'.
     */
    protected String containerFifoPath = "/fifo/";

    /** A resolver for host paths if the database builder is used from within docker (dind). */
    protected ContainerPathResolver containerPathResolver = null;

    public RDFDatabaseBuilderQlever() {
        super();
        this.containerPathResolver = ContainerPathResolver.create();
        if (containerPathResolver != null) {
            logger.info("Detected docker-in-docker setup (dind).");
        }
    }

    /** The content types supported by the backend. */
    public List<String> getSupportedBackendContentTypes() {
        return supportedBackendLangs.stream().map(Lang::getContentType).map(ContentType::getContentTypeStr).toList();
    }

    public X setSysRuntime(SysRuntime sysRuntime) {
        this.sysRuntime = sysRuntime;
        return self();
    }

    public X setDockerImageName(String dockerImageName) {
        config.setDockerImageName(dockerImageName);
        return self();
    }

    public String getDockerImageName() {
        return config.getDockerImageName();
    }

    public X setDockerImageTag(String dockerImageTag) {
        config.setDockerImageTag(dockerImageTag);
        return self();
    }

    public String getDockerImageTag() {
        return config.getDockerImageTag();
    }

    @Override
    public X setName(String name) {
        return setIndexName(name);
    }

    public X setIndexName(String name) {
        config.setIndexName(name);
        return self();
    }

    @Override
    public X setOutputFolder(Path outputFolder) {
        config.setOutputFolder(outputFolder);
        return self();
    }

    public X setStxxlMemory(String stxxlMemory) {
        config.setStxxlMemory(stxxlMemory);
        return self();
    }

    public String getStxxlMemory() {
        return config.getStxxlMemory();
    }

    public X setParserBufferSize(String bufferSize) {
        config.setParserBufferSize(bufferSize);
        return self();
    }

    public String getParserBufferSize() {
        return config.getParserBufferSize();
    }

    @Override
    public X addPath(String source, Lang lang, Node g, Boolean splittable) throws IOException {
        Objects.requireNonNull(source);
        Path path = Path.of(source);
        String contentTypeStr = null;
        if  (lang != null) {
            contentTypeStr = Optional.ofNullable(lang.getContentType()).map(ContentType::getContentTypeStr).orElse(null);
        } else {
            ContentType contentType = RDFLanguages.guessContentType(source);
            if (contentType != null) {
                lang = RDFLanguages.contentTypeToLang(contentType);
                contentTypeStr = contentType.getContentTypeStr();
            }
        }
        // Need to probe for (compression) encodings
        RdfEntityInfo entityInfo = RDFDataMgrEx.probeEntityInfo(() -> Files.newInputStream(path, StandardOpenOption.READ), supportedInputLangs);
        if (lang == null) {
            contentTypeStr = entityInfo.getContentType();
            lang = RDFLanguages.contentTypeToLang(contentTypeStr);
        }

        // TODO Make sure that any needed conversion exists!
        if (lang == null) {
            throw new RuntimeException("Could not detect lang for path: " + path + " - contentType: " + contentTypeStr);
        }
        addPath(path, g, entityInfo.getContentEncodings(), lang, null);
        return self();
    }

    protected void addPath(Path source, Node graph, List<String> encodings, Lang lang, Boolean splittable) {
        FileLoadEntry arg = new FileLoadEntry(source, lang, encodings, graph, splittable);
        args.add(arg);
    }

//    protected CmdArg encodingToAbstractCommand(String encoding, boolean decode) {
//    	return CmdOps.execArgv("/virt/" + encoding, "-cd");
//    }

    // If the encodings and types are directly supported then just pass on the file.
    // Otherwise, make a logical plan to appropriately convert the input.
    public CmdArg convertArgToOp(FileLoadEntry loadEntry) {
        Path path = loadEntry.path();
        boolean requiresContentConversion = !supportedBackendLangs.contains(loadEntry.lang());
        boolean requiresDecoding = !loadEntry.encodings().isEmpty();
        CmdArg result;
        // This first branch returns the given file directly.
        // The other branch creates a process substitution expression.
        if (!requiresDecoding && !requiresContentConversion) {
            result = CmdArg.ofPathString(loadEntry.path().toAbsolutePath().toString());
        } else {
            List<CmdOp> pipeline = new ArrayList<>();
            pipeline.add(CmdOps.execArgv("/virt/cat", path.toString()));

            for (String encoding : loadEntry.encodings()) {
                // Encodings assumed to exist under /virt such as /virt/bzip2 -cd
                pipeline.add(CmdOps.execArgv("/virt/" + encoding, "-cd"));
            }

            // Check if content type conversion is needed.
            Lang argLang = loadEntry.lang();
            if (requiresContentConversion) {
                // RDFLanguages.isTriples(arg.lang())
                Lang targetLang = RDFLanguagesEx.findBestLang(argLang, supportedBackendLangs);
                if (targetLang == null) {
                    throw new RuntimeException("Could not find a conversion from " + argLang + " to a lang supported by the backend. Registered supported langs: " + supportedBackendLangs);
                }

                logger.info("File " + loadEntry.path() + ": Injecting content conversion " + argLang + " -> " + targetLang);
                String baseUri = loadEntry.path().toUri().toString();
                List<String> rdfConvertArgs = ArgsBuilderJena.newBuilder()
                    .setSrcLang(argLang.getContentType().toString())
                    .setTgtFormat(targetLang.getContentType().toString())
                    .setBaseUri(baseUri)
                    .build();
                pipeline.add(CmdOps.execArgs("/virt/rdf-convert", rdfConvertArgs));
            }
            result = CmdArg.ofProcessSubstution(CmdOps.pipelineIfNeeded(pipeline));
        }
        return result;
    }

    protected List<CmdArg> buildInputSpec(Supplier<Path> hostTempPath) throws NoSuchFileException {
        List<CmdArg> argUnits = new ArrayList<>(args.size());
        // For each file, check whether any operations need to be performed on the host
        for (FileLoadEntry fileArg : args) {
            Lang lang = fileArg.lang();
            Node graph = fileArg.graph();
            CmdArg cmdArg = convertArgToOp(fileArg);
            // Convert the host operations to FileWriterTasks
            List<CmdArg> argUnit = buildFileEntryArgs(hostTempPath, cmdArg, graph, lang);
            argUnits.addAll(argUnit);
        }
        return argUnits;
    }

    protected List<CmdArg> buildFileEntryArgs(Supplier<Path> hostTempPathSupp, CmdArg cmdArg, Node graph, Lang lang) throws NoSuchFileException {
        String graphArg = Optional.ofNullable(graph)
            .filter(Node::isURI)
            .filter(g -> !Quad.isDefaultGraph(g))
            .map(Node::getURI).orElse("-");

        String fmtArg = Optional.ofNullable(lang)
            .map(l -> l.getFileExtensions())
            .map(l -> l.isEmpty() ? null : l.get(0))
            .orElse("");

        List<CmdArg> result = List.of(
            CmdArg.ofLiteral("-f"), cmdArg,
            CmdArg.ofLiteral("-F"), CmdArg.ofLiteral(fmtArg),
            CmdArg.ofLiteral("-g"), CmdArg.ofLiteral(graphArg)
        );
        return result;
    }

    protected String langToFormat(Lang lang) {
        return lang.getFileExtensions().get(0);
    }


    public String getFinalIndexName() {
        String indexName = config.getIndexName();
        String finalIndexName = indexName == null ? "default" : indexName;
        return finalIndexName;
    }

    /**
     * Determine the types of arguments:
     * If all files are directly nq or ttl then use them as file arguments.
     * Otherwise, build a stream from the argument:
     * - A mix of nq and ttl is not supported.
     * - Use cat or a codec to decode files
     * - Use a flag whether to use process substitution or command grouping.
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public RdfDatabaseQlever build() throws IOException, InterruptedException {
        String imageName = config.getDockerImageName();
        String imageTag = config.getDockerImageTag();
        String finalImageName = QleverConstants.buildDockerImageName(imageName, imageTag);

        Optional<CliType> cliVersion = QleverCliProberIndexBuilder.probe(finalImageName);

        if (cliVersion.isEmpty()) {
            throw new RuntimeException("Could not detect index builder command in " + finalImageName + " - known types: " + Arrays.asList(QleverCliProberIndexBuilder.CliType.values()));
        }

        String indexBuilderCommand = cliVersion.get().getCommandName();
        return build(finalImageName, indexBuilderCommand);
    }

    /** Build for 'v1' command line using IndexBuilderMain */
    public RdfDatabaseQlever build(String finalImageName, String indexBuilderCommand) throws IOException, InterruptedException {
        // The parent directory must exist
        Path outputFolder = config.getOutputFolder();
        Path parentFolder = outputFolder.getParent();
        if (parentFolder != null) {
            if (!Files.exists(parentFolder)) {
                throw new NoSuchFileException("Folder does not exist: " + parentFolder);
            }
        }
        Files.createDirectories(outputFolder);

        String finalIndexName = getFinalIndexName();

        // Resource manager to close all task at the end
        FinallyRunAll closer = FinallyRunAll.create();

        // tempPath is only created on demand.
        Path[] tempPath = new Path[]{null};

        try {
            Supplier<Path> getHostTempPath = () -> {
                // In a DooD or DinD setup, its easiest if the folder for the named pipes (fifo)
                // resides within the database location.
                // A check must be made that the database location is mounted from the host so that
                // it can be shared with the secondary container.

                try {
                    Path r = tempPath[0];
                    if (r == null) {
                        r = Files.createTempDirectory(outputFolder, "qlever-loader");
                        // r = ContainerPathResolver.expectResolvePath(containerPathResolver, r);
                        tempPath[0] = r;
                    }
//                    = tempPath[0] != null
//                        ? tempPath[0]
//                        : (tempPath[0] = ContainerPathResolver.resolvePath(containerPathResolver,
//                                Files.createTempDirectory("qlever-loader")));

                    logger.info("Created fifo folder: " + r);
                    return r;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            closer.add(() -> {
                Path p = tempPath[0];
                if (p != null) {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // FIXME Usually if we get here it means that the loading failed
                        //       and some files were not cleaned up.
                        logger.warn("Could not delete fifo folder on host: " + p, e);
                    }
                }
            });

            List<CmdArg> spec = buildInputSpec(getHostTempPath);

            List<CmdArg> args = new ArrayList<>();
            args.add(CmdArg.ofLiteral("-i"));
            args.add(CmdArg.ofLiteral(finalIndexName));

            String stxxlMemory = config.getStxxlMemory();
            if (stxxlMemory != null && !stxxlMemory.isBlank()) {
                args.add(CmdArg.ofLiteral("-m"));
                args.add(CmdArg.ofLiteral(stxxlMemory));
            }

            String parserBufferSize = config.getParserBufferSize();
            if (parserBufferSize != null && !parserBufferSize.isBlank()) {
                args.add(CmdArg.ofLiteral("--parser-buffer-size"));
                args.add(CmdArg.ofLiteral(parserBufferSize));
            }

            args.addAll(spec);

            ExecSite execSite = ExecSites.docker(finalImageName);

            //Path sharedPath = Path.of("/tmp/shared");
            FileMapper fileMapper = FileMapper.of(containerFifoPath);
            // fileMapper.getBinds().add(null)
            CmdExecSystem cmdExecSystem = CmdExecSystem.newBuilder().build();

            // cmdExecSystem.exec(null, fileMapper, cmdArg, execSite)
            try (ProcessRunner processCxt = ProcessRunnerPosix.create()) {
                List<CmdArg> containerArgs = new ArrayList<>(args.size());
                // This resolves process substitutions by creating named pipes on the shared host folder.
                for (CmdArg cmdArg : args) {
                    CmdArgActiveProcessSubstitution activeArg = cmdExecSystem.exec(processCxt, fileMapper, cmdArg, execSite);
                    // hostArgs.add(activeArg.cmdArg());
                    CmdArg hostArg = activeArg.cmdArg();
                    CmdArg containerArg = CmdOpRewriter.rewriteForContainer(hostArg, fileMapper);
                    containerArgs.add(containerArg);
                }

                // Rewrite file arguments to container paths.
                // CmdOp containerizedCmd =

                List<String> containerArgv = CmdArg.toPlainArgs(containerArgs);

                String rawCmd = indexBuilderCommand + " " + CmdOpVisitorToCmdString.toArg(containerArgv);
                // String cmd = "'" + CmdStrOpsBash.get().escapeTokenSingleQuote(rawCmd) + "'";
                // String cmd = CmdStrOpsBash.get().escapeTokenSingleQuote(rawCmd);

                String workDir = "/data";
                fileMapper.getBinds().add(new Bind(outputFolder.toAbsolutePath().toString(), new Volume("/data"), AccessMode.rw));

                Process p = ProcessBuilderDockerRun.of(rawCmd).imageRef(finalImageName)
                        .fileMapper(fileMapper)
                        .workingDirectory(workDir)
                        .start(processCxt);

                // Process p = cmdExecSystem.exec(processCxt, fileMapper, execSite);
                p.waitFor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } finally {
            closer.run();
        }

        RdfDatabaseQlever result = new RdfDatabaseQlever(outputFolder, finalIndexName);
        return result;
    }

    @Override
    public X setProperty(String key, Object value) {
        try {
            PropertyUtils.setProperty(config, key, value);
            logger.info("Set property " + key + " -> " + value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.info("Unsupported property: " + key + "(value was: " + value + ")");
        }
        return self();
    }

    @Override
    public <T> T getProperty(String key) {
        Object r = null;
        try {
            r = PropertyUtils.getProperty(config, key);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // Nothing to do.
        }
        return (T)r;
    }

    /**
     * Build for using directly qlever index (not {@code qlever-index} with a dash)
     * TODO UNFINISHED and subject to removal - because relying on the index builder is sufficient as long
     * as this api does not get removed.
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private RdfDatabaseQlever buildV2() throws IOException, InterruptedException {
        // The parent directory must exist
        Path outputFolder = config.getOutputFolder();
        Path parentFolder = outputFolder.getParent();
        if (parentFolder != null) {
            if (!Files.exists(parentFolder)) {
                throw new NoSuchFileException("Folder does not exist: " + parentFolder);
            }
        }
        Files.createDirectories(outputFolder);

        String finalIndexName = getFinalIndexName();

        // Resource manager to close all task at the end
        FinallyRunAll closer = FinallyRunAll.create();

        // tempPath is only created on demand.
        Path[] tempPath = new Path[]{null};

        try {
            Supplier<Path> getHostTempPath = () -> {
                // In a DooD or DinD setup, its easiest if the folder for the named pipes (fifo)
                // resides within the database location.
                // A check must be made that the database location is mounted from the host so that
                // it can be shared with the secondary container.

                try {
                    Path r = tempPath[0];
                    if (r == null) {
                        r = Files.createTempDirectory(outputFolder, "qlever-loader");
                        // r = ContainerPathResolver.expectResolvePath(containerPathResolver, r);
                        tempPath[0] = r;
                    }
//                    = tempPath[0] != null
//                        ? tempPath[0]
//                        : (tempPath[0] = ContainerPathResolver.resolvePath(containerPathResolver,
//                                Files.createTempDirectory("qlever-loader")));

                    logger.info("Created fifo folder: " + r);
                    return r;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            closer.add(() -> {
                Path p = tempPath[0];
                if (p != null) {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // FIXME Usually if we get here it means that the loading failed
                        //       and some files were not cleaned up.
                        logger.warn("Could not delete fifo folder on host: " + p, e);
                    }
                }
            });

            String imageName = config.getDockerImageName();
            String imageTag = config.getDockerImageTag();
//          Path outputFolder = config.getOutputFolder();
//          // Path finalOutputFolder = ContainerPathResolver.resolvePath(containerPathResolver, outputFolder);
      //
            String finalImageName = QleverConstants.buildDockerImageName(imageName, imageTag);
            ExecSite execSite = ExecSites.docker(finalImageName);

            //Path sharedPath = Path.of("/tmp/shared");
            FileMapper fileMapper = FileMapper.of(containerFifoPath);
            // fileMapper.getBinds().add(null)
            CmdExecSystem cmdExecSystem = CmdExecSystem.newBuilder().build();
            try (ProcessRunner processCxt = ProcessRunnerPosix.create()) {
                // CmdOpVisitorToCmdString.toArg(containerArgv);

                ObjectMapper mapper = new ObjectMapper();
                ArrayNode rootArray = mapper.createArrayNode();

                // For each file, check whether any operations need to be performed on the host
                for (FileLoadEntry fileArg : args) {
                    Lang lang = fileArg.lang();
                    Node graph = fileArg.graph();
                    CmdArg cmdArg = convertArgToOp(fileArg);

                    CmdArgActiveProcessSubstitution activeArg = cmdExecSystem.exec(processCxt, fileMapper, cmdArg, execSite);
                    CmdArg hostArg = activeArg.cmdArg();
                    CmdArg containerArg = CmdOpRewriter.rewriteForContainer(hostArg, fileMapper);

                    String containerArgStr = CmdArgVisitorRenderAsBashString.render(containerArg);

                    ObjectNode file = mapper.createObjectNode();
                    file.put("cmd", "cat " + containerArgStr);

                    Lang l = fileArg.lang;
                    if (l != null) {
                        String fmt;
                        if (Lang.NTRIPLES.equals(l)) {
                            fmt = "nt";
                        } else if (Lang.NQUADS.equals(l)) {
                            fmt = "nq";
                        } else if (Lang.TURTLE.equals(l)) {
                            fmt = "ttl";
                        } else {
                            throw new RuntimeException("Unsupported format: " + l);
                        }
                        file.put("format", fmt);
                    }

                    if (fileArg.graph != null) {
                        file.put("graph", fileArg.graph.getURI());
                    }
                    if (fileArg.splittable != null) {
                        file.put("parallel", fileArg.splittable);
                    }

                    rootArray.add(file);
                }

                Path outputPath = outputFolder.resolve("qlever_inputs.json");

                // Generate string in memory
                String qleverJson = mapper.writeValueAsString(rootArray);

                // Write the string instantly via NIO
                Files.writeString(outputPath, qleverJson);
//            [
//             {
//               "cmd": "cat file1.nt",
//               "format": "nt",
//               "graph": "http://my.graph"
//               "parallel": true
//             },
//             {
//               "cmd": "cat file2.nq",
//               "format": "nq",
//               "graph": "http://my.other"
//             }
//            ]

                List<CmdArg> as = new ArrayList<>();
                as.add(CmdArg.ofLiteral("qlever"));
                as.add(CmdArg.ofLiteral("index"));

                as.add(CmdArg.ofLiteral("-i"));
                as.add(CmdArg.ofLiteral(finalIndexName));

                String stxxlMemory = config.getStxxlMemory();
                if (stxxlMemory != null && !stxxlMemory.isBlank()) {
                    as.add(CmdArg.ofLiteral("--stxxl-memory"));
                    as.add(CmdArg.ofLiteral(stxxlMemory));
                }

                String parserBufferSize = config.getParserBufferSize();
                if (parserBufferSize != null && !parserBufferSize.isBlank()) {
                    as.add(CmdArg.ofLiteral("--parser-buffer-size"));
                    as.add(CmdArg.ofLiteral(parserBufferSize));
                }

                as.add(CmdArg.ofLiteral("--multi-input-json"));
                as.add(CmdArg.ofPathString(outputPath.toString()));

            // cmdExecSystem.exec(null, fileMapper, cmdArg, execSite)
                List<CmdArg> containerArgs = new ArrayList<>(args.size());
                // This resolves process substitutions by creating named pipes on the shared host folder.
//                for (CmdArg cmdArg : args) {
//                    CmdArgActiveProcessSubstitution activeArg = cmdExecSystem.exec(processCxt, fileMapper, cmdArg, execSite);
//                    // hostArgs.add(activeArg.cmdArg());
//                    CmdArg hostArg = activeArg.cmdArg();
//                    CmdArg containerArg = CmdOpRewriter.rewriteForContainer(hostArg, fileMapper);
//                    containerArgs.add(containerArg);
//                }

                // Rewrite file arguments to container paths.
                List<String> containerArgv = CmdArg.toPlainArgs(containerArgs);

                String rawCmd = "IndexBuilderMain " + CmdOpVisitorToCmdString.toArg(containerArgv);
                // String cmd = "'" + CmdStrOpsBash.get().escapeTokenSingleQuote(rawCmd) + "'";
                // String cmd = CmdStrOpsBash.get().escapeTokenSingleQuote(rawCmd);

                String workDir = "/data";
                fileMapper.getBinds().add(new Bind(outputFolder.toAbsolutePath().toString(), new Volume("/data"), AccessMode.rw));

                Process p = ProcessBuilderDockerRun.of(rawCmd).imageRef(finalImageName)
                        .fileMapper(fileMapper)
                        .workingDirectory(workDir)
                        .start(processCxt);

                // Process p = cmdExecSystem.exec(processCxt, fileMapper, execSite);
                p.waitFor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } finally {
            closer.run();
        }

        RdfDatabaseQlever result = new RdfDatabaseQlever(outputFolder, finalIndexName);
        return result;
    }
}

