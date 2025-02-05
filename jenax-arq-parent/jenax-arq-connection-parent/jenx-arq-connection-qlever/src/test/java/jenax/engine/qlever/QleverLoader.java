package jenax.engine.qlever;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.arq.util.prefix.ShortNameMgr;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.aksw.jsheller.algebra.logical.CodecOp;
import org.aksw.jsheller.algebra.logical.CodecOpCodecName;
import org.aksw.jsheller.algebra.logical.CodecOpCommand;
import org.aksw.jsheller.algebra.logical.CodecOpConcat;
import org.aksw.jsheller.algebra.logical.CodecOpFile;
import org.aksw.jsheller.algebra.logical.CodecOpTransformer;
import org.aksw.jsheller.algebra.logical.CodecOpVisitor;
import org.aksw.jsheller.algebra.logical.CodecOpVisitorStream;
import org.aksw.jsheller.algebra.logical.CodecRegistry;
import org.aksw.jsheller.algebra.logical.CodecSysEnv;
import org.aksw.jsheller.algebra.logical.CodecTransformToCmdOp;
import org.aksw.jsheller.algebra.logical.SysRuntime;
import org.aksw.jsheller.algebra.logical.SysRuntimeImpl;
import org.aksw.jsheller.algebra.physical.CmdOp;
import org.aksw.jsheller.algebra.physical.CmdOpExec;
import org.aksw.jsheller.algebra.physical.CmdOpPipe;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.shaded.com.github.dockerjava.core.command.AttachContainerResultCallback;

import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.google.common.io.ByteSource;

import jenax.engine.qlever.docker.GenericContainer;

public class QleverLoader {
    public static final List<Lang> supportedLangs = Collections.unmodifiableList(Arrays.asList(Lang.NQUADS, Lang.TURTLE));

    private static final Logger logger = LoggerFactory.getLogger(QleverLoader.class);

    /** Record to capture arguments passed to this builder */
    public record FileArg(Path path, Lang lang, List<String> encodings, Node graph) {}

    /** Record to capture a set of files that make up a Qlever database. */
    public record QleverDbFileSet(List<Path> paths) { }

    protected ShortNameMgr shortNameMgr = new ShortNameMgr();

    protected Path outputFolder = null;
    protected List<FileArg> args = new ArrayList<>();
    protected List<Entry<Lang, Throwable>> errorCollector = new ArrayList<>();

    protected String indexName;

    public QleverLoader setIndexName(String name) {
        this.indexName = name;
        return this;
    }

    public QleverLoader setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder;
        return this;
    }

    public QleverLoader addPath(String source) throws IOException {
        return addPath(source, null);
    }

    public QleverLoader addPath(String source, String graph) throws IOException {
        Path path = Path.of(source);
        Node g = graph == null ? null : NodeFactory.createURI(graph);
        RdfEntityInfo entityInfo = RDFDataMgrEx.probeEntityInfo(() -> Files.newInputStream(path, StandardOpenOption.READ), supportedLangs);
        String contentType = entityInfo.getContentType();
        Lang lang = RDFLanguages.contentTypeToLang(contentType);

        addPath(path, g, entityInfo.getContentEncodings(), lang);
        return this;
    }

    protected void addPath(Path source, Node graph, List<String> encodings, Lang lang) {
        FileArg arg = new FileArg(source, lang, encodings, graph);
        args.add(arg);
    }

    public CodecOp convertArgToOp(FileArg arg) {
        CodecOp result = new CodecOpFile(arg.path().toString());
        for (String encoding : arg.encodings()) {
            result = new CodecOpCodecName(encoding, result);
        }
        return result;
    }

    protected FileSpec buildFileSpec() {
        List<String> cmdParts = new ArrayList<>();
        Map<String, String> fsBinds = new LinkedHashMap<>();
        for (FileArg arg : args) {
            String fileArg = Optional.ofNullable(arg.path())
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .orElse("-");

            String uriStr = arg.path().toUri().toString();
            String shortName = shortNameMgr.allocate(uriStr).localName();

            String graphArg = Optional.ofNullable(arg.graph())
                    .filter(g -> Quad.isDefaultGraph(g))
                    .map(Node::getURI).orElse("-");

            String fmtArg = Optional.ofNullable(arg.lang())
                    .map(l -> l.getFileExtensions())
                    .map(l -> l.isEmpty() ? null : l.get(0))
                    .orElse("");

            String cmdPart = "-f " + shortName + " -F " + fmtArg + " -g " + graphArg;
            cmdParts.add(cmdPart);

            fsBinds.put(fileArg, "/data/" + shortName);
        }

        List<Bind> binds = fsBinds.entrySet().stream()
            .map(e -> new Bind(e.getKey(), new Volume(e.getValue()), AccessMode.ro))
            .toList();

        String[] argParts = cmdParts.toArray(new String[0]);

        return new FileSpec(argParts, binds);
    }

    public static record ByteSourceSpec(String[] cmd, ByteSource byteSource, Lang lang) {
        /* cmd arg should be copied
        public ByteSourceCmd(String[] cmd, ByteSource byteSource) {
            this(Arrays.copy(cmd, cmd,length), byteSource);
        }
        */
    }

    public static class ByteSourceOverCodecOp
        extends ByteSource
    {
        protected CodecOp op;
        protected CodecOpVisitor<InputStream> streamVisitor;

        public ByteSourceOverCodecOp(CodecOp op) {
            this(op, CodecOpVisitorStream.getSingleton());
        }

        public ByteSourceOverCodecOp(CodecOp op, CodecOpVisitor<InputStream> streamVisitor) {
            this.op = Objects.requireNonNull(op);
            this.streamVisitor = Objects.requireNonNull(streamVisitor);
        }

        @Override
        public InputStream openStream() throws IOException {
            return op.accept(streamVisitor);
        }
    }

    protected SysRuntime getRuntime() {
        SysRuntime result = SysRuntimeImpl.forBash();
        return result;
    }

    protected CodecTransformToCmdOp sysCallTransform() {
        CodecRegistry reg = CodecRegistry.get();
        SysRuntime runtime = getRuntime();
        CodecSysEnv env = new CodecSysEnv(runtime);
        CodecTransformToCmdOp sysCallTransform = new CodecTransformToCmdOp(reg, env); // , Mode.COMMAND_GROUP);
        return sysCallTransform;
    }

    protected ByteSourceSpec buildByteSourceCmd(List<CodecOp> args, Lang lang) {
        CodecTransformToCmdOp sysCallTransform = sysCallTransform();

        CodecOp javaOp = CodecOpConcat.of(args);
        ByteSource javaByteSource = new ByteSourceOverCodecOp(javaOp);

        CodecOp sysOp = CodecOpTransformer.transform(javaOp, sysCallTransform);

        ByteSourceSpec result;
        if (sysOp instanceof CodecOpCommand codecOp) {
            CmdOp cmdOp = codecOp.getCmdOp();
            String[] cmd = SysRuntimeImpl.forBash().compileCommand(cmdOp);
            result = new ByteSourceSpec(cmd, javaByteSource, lang);
        } else {
            result = new ByteSourceSpec(null, javaByteSource, lang);
        }
        return result;
    }

    /** A list of binds for file arguments or a byte source with the input data - mutually exclusive. */
    // public record BindsOrStream(List<Bind> binds, ByteSourceCmd byteSource) {}

    /** A list of file binds and arguments to process them */
    public record FileSpec(String[] fileArgs, List<Bind> binds) {}

    /** Record to capture whether to pass input data as files or as an input stream. */
    public record InputSpec(FileSpec fileSpec, ByteSourceSpec byteSourceSpec, Lang byteSourceSpecLang) {}

    protected InputSpec buildInputSpec() {
        Set<Lang> usedLangs = args.stream().map(FileArg::lang).collect(Collectors.toSet());
        // TODO Remove subsumed languages
        // Check whether any arguments require decoding.
        // RDFLanguagesEx.streamSubLangs(null)
        List<CodecOp> ops = args.stream().map(this::convertArgToOp).toList();

        boolean isAllFiles = ops.stream().allMatch(op -> op instanceof CodecOpFile);

        InputSpec result;
        if (isAllFiles) {
            FileSpec fileSpec = buildFileSpec();
            result = new InputSpec(fileSpec, null, null);
        } else {
            if (usedLangs.contains(Lang.NQUADS) && usedLangs.contains(Lang.TURTLE)) {
                throw new RuntimeException("Unsupported mix of languages: nt + ttl");
            }

            Lang finalLang = usedLangs.iterator().next();
            ByteSourceSpec byteSourceCmd = buildByteSourceCmd(ops, finalLang);
            result = new InputSpec(null, byteSourceCmd, finalLang);
        }
        return result;
    }


    protected String buildDockerImageName() {
        String qleverDockerTag = "latest";

        String result = Stream.of("adfreiburg/qlever", qleverDockerTag)
            .filter(x -> x != null)
            .collect(Collectors.joining(":"));

        return result;
    }

    protected org.testcontainers.containers.GenericContainer<?> setupContainer(String cmdStr) throws NumberFormatException, IOException, InterruptedException {
        int uid = SystemUtils.getUID();
        int gid = SystemUtils.getGID();
        logger.info("Launching qlever indexer container as UID: " + uid + ", GID: " + gid);

        String dockerImageName = buildDockerImageName();

        org.testcontainers.containers.GenericContainer<?> result = new org.testcontainers.containers.GenericContainer<>(dockerImageName)
            .withWorkingDirectory("/data")
            // .withExposedPorts(containerPort)
            // Setting UID does not work with latest image due to
            // error "UID 1000 already exists" ~ 2025-01-31
            // .withEnv("UID", Integer.toString(uid))
            // .withEnv("GID", Integer.toString(gid))
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(uid + ":" + gid))
            .withFileSystemBind(outputFolder.toString(), "/data", BindMode.READ_WRITE)
            .withCommand(new String[]{"IndexBuilderMain -i " + indexName + (cmdStr.isEmpty() ? " " : " ") + cmdStr})
            // .withCommand(new String[]{"ServerMain -h"})
            ;

        return result;
    }

    protected void runContainerWithInputStream(ByteSource byteSource) throws InterruptedException, IOException {

        logger.info("Attempting to launch container with a JVM-based input stream.");
        org.testcontainers.containers.GenericContainer<?> container = setupContainer("")
            .withCreateContainerCmdModifier(cmd -> cmd
                .withTty(true)  // Required to keep input open
                .withAttachStdin(true)  // Allow attaching input stream
            );

            container.start();

        // Get input stream (e.g., file or command output)
        try (InputStream inputStream = byteSource.openStream()) {
            // Attach input stream to the container
            container.getDockerClient()
                .attachContainerCmd(container.getContainerId())
                .withStdIn(inputStream)
                .withStdOut(true)
                .withFollowStream(true)
                .exec(new AttachContainerResultCallback())
                .awaitCompletion();
        }

        container.stop();
    }

    protected void runContainerWithFileArgs(FileSpec fileSpec) throws NumberFormatException, IOException, InterruptedException {
        logger.info("Attempting to launch container with binds and file arg");
        String cmdStr = Arrays.asList(fileSpec.fileArgs).stream().collect(Collectors.joining(" "));

        org.testcontainers.containers.GenericContainer<?> container = setupContainer(cmdStr);

        for (Bind bind : fileSpec.binds()) {
            BindMode bindMode = AccessMode.ro.equals(bind.getAccessMode())
                ? BindMode.READ_ONLY
                : null;
            container.addFileSystemBind(bind.getPath(), bind.getVolume().getPath(), bindMode);
        }

        container.start();
        container.followOutput(outputFrame -> {
            String msg = outputFrame.getUtf8String();
            logger.info(msg);
        });
        container.getDockerClient().waitContainerCmd(container.getContainerId()).exec(new WaitContainerResultCallback()).awaitCompletion();
    }

    protected String langToFormat(Lang lang) {
        return lang.getFileExtensions().get(0);
    }

    protected void runContainerViaSysCall(String[] generatorCmd, Lang lang) throws NumberFormatException, IOException, InterruptedException {
        int uid = SystemUtils.getUID();
        int gid = SystemUtils.getGID();
        logger.info("Attempting to launch container via syscall. UID: " + uid + ", GID: " + gid);

        String dockerImageName = buildDockerImageName();

        String fmt = langToFormat(lang);

        // String cmdStr = Arrays.asList(fileSpec.fileArgs).stream().collect(Collectors.joining(" "));

        // int containerPort = 8080;
        // https://hub.docker.com/r/adfreiburg/qlever/tags
        GenericContainer<?> container = new GenericContainer<>(dockerImageName)
            .withWorkingDirectory("/data")
            // .withExposedPorts(containerPort)
            // Setting UID does not work with latest image due to
            // error "UID 1000 already exists" ~ 2025-01-31
            // .withEnv("UID", Integer.toString(uid))
            // .withEnv("GID", Integer.toString(gid))
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(uid + ":" + gid))
            .withFileSystemBind(outputFolder.toString(), "/data", BindMode.READ_WRITE)
            .withCommand(new String[]{SysRuntimeImpl.quoteArg("IndexBuilderMain -i " + indexName + " -f - -F " + fmt)})
            // .withCommand(new String[]{"ServerMain -h"})
            ;

        CmdOp pipe = new CmdOpPipe(CmdOpExec.of(generatorCmd), CmdOpExec.of(container.buildCmdLine()));

        SysRuntime runtime = getRuntime();
        String[] cmd = runtime.compileCommand(pipe);

        // CmdOpVisitor<String> stringifier = new CmdOpVisitorToString(new CmdStrOpsBash());
        // String cmd = pipe.accept(stringifier);
        System.out.println("CMD: " + Arrays.asList(cmd));
        // CodecTransformSys sysCallTransform = sysCallTransform();
//        CodecOp compiled = CodecOpTransformer.transform(pipe, sysCallTransform);
//
//        if (compiled instanceof CodecOpCommand cmdOp) {
//            String[] cmd = cmdOp.getCmdArray();
//            System.out.println("CMD: " + Arrays.asList(cmd));
//        } else {
//            throw new IllegalStateException("Could not compile the command line: " + compiled);
//        }

    }

    /**
     * Determine the types of arguments:
     * If all files are directly nq or ttl then use them as file arguments.
     * Otherwise, build a stream from the argument:
     * - A mix of nq and ttl is not supported.
     * - Use cat or a codec to decode files
     * - Use a flag whether to use process substitution or command grouping.
     *
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */

    public QleverDbFileSet build() throws IOException, InterruptedException {
        InputSpec inputSpec = buildInputSpec();

        FileSpec fileSpec = inputSpec.fileSpec();
        if (fileSpec != null) {
            runContainerWithFileArgs(fileSpec);
        } else {
            ByteSourceSpec byteSourceSpec = inputSpec.byteSourceSpec();
            String[] cmd = byteSourceSpec.cmd();
            ByteSource byteSource = byteSourceSpec.byteSource();
            if (cmd != null) {
                Lang lang = byteSourceSpec.lang();
                runContainerViaSysCall(cmd, lang);
            } else if (byteSource != null) {
                runContainerWithInputStream(byteSource);
            } else {
                throw new IllegalStateException("Unexpected error: Failed to determine a strategy to process the input data");
            }
        }

        QleverDbFileSet result = assembleFileSet(outputFolder, indexName);
        return result;
    }

    public static QleverDbFileSet assembleFileSet(Path path, String indexName) {
        List<Path> fileSet = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, indexName + ".*")) {
            for (Path entry : stream) {
                fileSet.add(entry);
            }
        } catch (IOException | DirectoryIteratorException e) {
            throw new RuntimeException(e);
        }
        return new QleverDbFileSet(fileSet);
    }
}


//int uid = SystemUtils.getUID();
//int gid = SystemUtils.getGID();
//logger.info("Launching qlever indexer container as UID: " + uid + ", GID: " + gid);
//
//String qleverDockerTag = "latest";
//
//String dockerImageName = Stream.of("adfreiburg/qlever", qleverDockerTag)
//    .filter(x -> x != null)
//    .collect(Collectors.joining(":"));
//
//String cmdStr = "foo";
//
//// int containerPort = 8080;
//// https://hub.docker.com/r/adfreiburg/qlever/tags
//GenericContainer<?> container = new GenericContainer<>(dockerImageName)
//    .withWorkingDirectory("/data")
//    // .withExposedPorts(containerPort)
//    // Setting UID does not work with latest image due to
//    // error "UID 1000 already exists" ~ 2025-01-31
//    // .withEnv("UID", Integer.toString(uid))
//    // .withEnv("GID", Integer.toString(gid))
//    .withCreateContainerCmdModifier(cmd -> cmd.withUser(uid + ":" + gid))
//    .withFileSystemBind(outputFolder.toString(), "/data", BindMode.READ_WRITE)
//    .withCommand(new String[]{"IndexBuilderMain -i " + indexName + " " + cmdStr})
//    // .withCommand(new String[]{"ServerMain -h"})
//    ;
//
//System.out.println("CMD: " + Arrays.asList(container.buildCmdLine()));
//if (true) {
//    return new QleverDbFileSet(List.of());
//}
//
//container.followOutput(outputFrame -> {
//    String msg = outputFrame.getUtf8String();
//    logger.info(msg);
//});
//
//container.getDockerClient().waitContainerCmd(container.getContainerId()).exec(new WaitContainerResultCallback()).awaitCompletion();
//// container.stop();

//protected Lang probeLang(Path source) throws IOException {
//  Lang result = null;
//  List<Lang> supportedLangs = Arrays.asList(Lang.NQUADS, Lang.TURTLE);
//  RdfEntityInfo info = RDFDataMgrEx.probeEntityInfo(source, supportedLangs);
//  List<String> xencodings = info.getContentEncodings();
//  String xcontentType = info.getContentType();
//
//  InputStream in = RDFDataMgr.open(source.toAbsolutePath().toString());
//  in = RDFDataMgrEx.decode(in, xencodings);
//  Lang lang = RDFLanguages.contentTypeToLang(xcontentType);
//
//  AsyncParserBuilder builder = AsyncParser.of(in, result, xcontentType)
//          // .mutateSources(parser -> parser.errorHandler(ErrorHandlerFactory.errorHandlerSimple()))
//
//
//  // RDFDataMgr.open()
//
//
//  try (TypedInputStream tmpIn = RDFDataMgrEx.open(source, supportedLangs, errorCollector)) {
//      // Unwrap the input stream to minimize overhead.
//      InputStream in = tmpIn.getInputStream();
//
//      String contentType = tmpIn.getContentType();
//      if (contentType == null) {
//          if (logger.isInfoEnabled()) {
//              logger.info("Argument does not appear to be (RDF) data because content type probing yeld no result");
//          }
//      } else {
//          if (logger.isInfoEnabled()) {
//              logger.info("Detected data format: " + contentType);
//          }
//      }
//      Lang rdfLang = contentType == null ? null : RDFLanguages.contentTypeToLang(contentType);
//
//      //Lang rdfLang = RDFDataMgr.determineLang(filename, null, null);
//      if(rdfLang != null) {
//          // FIXME Validate we are really using turtle/trig here
//          if(RDFLanguages.isTriples(rdfLang)) {
//              result = Lang.TTL;
//          } else if(RDFLanguages.isQuads(rdfLang)) {
//              result = Lang.TRIG;
//          } else {
//              throw new RuntimeException("Unknown lang: " + rdfLang);
//          }
//      }
//  }
//
//  if (result == null) {
//      throw new RuntimeException("Could not determine content type of : " + source);
//  }
//
//  return result;
//}


//CodecOpVisitorStream javaStreamer = CodecOpVisitorStream.getSingleton();
//try (BufferedReader br = new BufferedReader(new InputStreamReader(finalOp.accept(javaStreamer), StandardCharsets.UTF_8))) {
//  br.lines().limit(10).forEach(System.out::println);
//}


//    CodecOpVisitorStream javaStreamer = CodecOpVisitorStream.getSingleton();
//    try (BufferedReader br = new BufferedReader(new InputStreamReader(finalOp.accept(javaStreamer), StandardCharsets.UTF_8))) {
//        br.lines().limit(10).forEach(System.out::println);
//    }

//    CodecOpVisitorStream javaStreamer = CodecOpVisitorStream.getSingleton();
//
//    //
//
//    CodecOp op = new CodecOpFile("/home/raven/tmp/codec-test/test.txt.bz2.gz");
//    op = new CodecOpCodecName("gz", op);
//    op = new CodecOpCodecName("bzip2", op);
//    System.out.println(op);
//
//    try (InputStream xin = op.accept(javaStreamer)) {
//        System.out.println(IOUtils.toString(xin, StandardCharsets.UTF_8));
//    }
//
//    CodecOp cmd = CodecOpTransformer.transform(op, transform);
//    System.out.println(cmd);
//
//    try (InputStream xin = cmd.accept(javaStreamer)) {
//        System.out.println(IOUtils.toString(xin, StandardCharsets.UTF_8));
//    }
//
//    System.out.println(cmd);

/*
List<String> cmdParts = new ArrayList<>();
Map<String, String> fsBinds = new LinkedHashMap<>();
for (FileArg arg : args) {
    String fileArg = Optional.ofNullable(arg.path())
            .map(Path::toAbsolutePath)
            .map(Path::toString)
            .orElse("-");

    String uriStr = arg.path().toUri().toString();
    String shortName = shortNameMgr.allocate(uriStr).localName();

    String graphArg = Optional.ofNullable(arg.graph())
            .filter(g -> Quad.isDefaultGraph(g))
            .map(Node::getURI).orElse("-");

    String fmtArg = Optional.ofNullable(arg.lang())
            .map(l -> l.getFileExtensions())
            .map(l -> l.isEmpty() ? null : l.get(0))
            .orElse("");

    String cmdPart = "-f " + shortName + " -F " + fmtArg + " -g " + graphArg;
    cmdParts.add(cmdPart);

    fsBinds.put(fileArg, "/data/" + shortName);
}

String cmdStr = cmdParts.stream().collect(Collectors.joining(" "));
*/

// System.out.println("CMD: " + Arrays.asList(container.getCommandParts()));

//for (Entry<String, String> e : fsBinds.entrySet()) {
//    container = container.withFileSystemBind(e.getKey(), e.getValue(), BindMode.READ_ONLY);
//}

    // Note: To force a host port use .setPortBindings(List.of("1111:2222"));

// container.start();

//String serviceUrl = "http://" + container.getHost() + ":" + container.getMappedPort(containerPort);
//System.out.println("Started at: " + serviceUrl);

