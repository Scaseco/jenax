package org.aksw.jenax.engine.qlever;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.arq.util.prefix.ShortNameMgr;
import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseBuilder;
import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabaseFileSet;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.aksw.jsheller.algebra.cmd.op.CmdOp;
import org.aksw.jsheller.algebra.cmd.op.CmdOpExec;
import org.aksw.jsheller.algebra.cmd.op.CmdOpPipe;
import org.aksw.jsheller.algebra.common.TranscodeMode;
import org.aksw.jsheller.algebra.stream.op.CodecSysEnv;
import org.aksw.jsheller.algebra.stream.op.StreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpCommand;
import org.aksw.jsheller.algebra.stream.op.StreamOpConcat;
import org.aksw.jsheller.algebra.stream.op.StreamOpFile;
import org.aksw.jsheller.algebra.stream.op.StreamOpTranscode;
import org.aksw.jsheller.algebra.stream.transform.StreamTransformToCmdOp;
import org.aksw.jsheller.algebra.stream.transformer.StreamOpTransformer;
import org.aksw.jsheller.exec.SysRuntime;
import org.aksw.jsheller.exec.SysRuntimeImpl;
import org.aksw.jsheller.registry.CodecRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Volume;
import com.google.common.io.ByteSource;
import com.nimbusds.jose.util.StandardCharset;

import jenax.engine.qlever.docker.GenericContainer;

public class RdfDatabaseBuilderQlever implements RdfDatabaseBuilder {
    public static final List<Lang> supportedLangs = Collections.unmodifiableList(Arrays.asList(Lang.TURTLE, Lang.NQUADS));

    private static final Logger logger = LoggerFactory.getLogger(RdfDatabaseBuilderQlever.class);

    /** Record to capture arguments passed to this builder */
    public record FileArg(Path path, Lang lang, List<String> encodings, Node graph) {}

    /** Record to capture a set of files that make up a Qlever database. */
    public record QleverDbFileSet(List<Path> paths) implements RdfDatabaseFileSet {
        @Override
        public List<Path> getPaths() {
            return paths;
        }
    }

    /** Mapping from absolute file paths on the host names to file names. */
    protected ShortNameMgr shortNameMgr = new ShortNameMgr();

    /** */
    protected SysRuntime sysRuntime;

    protected Path outputFolder = null;
    protected List<FileArg> args = new ArrayList<>();
    protected List<Entry<Lang, Throwable>> errorCollector = new ArrayList<>();

    protected String indexName;

    public RdfDatabaseBuilderQlever setSysRuntime(SysRuntime sysRuntime) {
        this.sysRuntime = sysRuntime;
        return this;
    }

    @Override
    public RdfDatabaseBuilder setName(String name) {
        return setIndexName(name);
    }

    public RdfDatabaseBuilder setIndexName(String name) {
        this.indexName = name;
        return this;
    }

    @Override
    public RdfDatabaseBuilderQlever setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder;
        return this;
    }

    @Override
    public RdfDatabaseBuilder addPath(String source, Node g) throws IOException {
        Path path = Path.of(source);
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

    public StreamOp convertArgToOp(FileArg arg) {
        StreamOp result = new StreamOpFile(arg.path().toString());
        for (String encoding : arg.encodings()) {
            result = new StreamOpTranscode(encoding, TranscodeMode.DECODE, result);
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

        String[] argParts = cmdParts.toArray(String[]::new);

        return new FileSpec(argParts, binds);
    }

    /** Record to hold either a command for a ProcessBuilder that produces output or a ByteSource. */
    public static record ByteSourceSpec(CmdOp cmdOp, ByteSource byteSource, Lang lang) {
        /* cmd arg should be copied
        public ByteSourceCmd(String[] cmd, ByteSource byteSource) {
            this(Arrays.copy(cmd, cmd,length), byteSource);
        }
        */
    }

    protected SysRuntime getRuntime() {
        SysRuntime result = sysRuntime;
        if (result == null) {
            result = SysRuntimeImpl.forCurrentOs();
        }
        return result;
    }

    protected StreamTransformToCmdOp sysCallTransform() {
        CodecRegistry reg = CodecRegistry.get();
        SysRuntime runtime = getRuntime();
        CodecSysEnv env = new CodecSysEnv(runtime);
        StreamTransformToCmdOp sysCallTransform = new StreamTransformToCmdOp(reg, env); // , Mode.COMMAND_GROUP);
        return sysCallTransform;
    }

    protected ByteSourceSpec buildByteSourceCmd(List<StreamOp> args, Lang lang) {
        // Inject a dummy codec 'cat' to cat immediate file arguments
        // FIXME HACK 'cat' is certainly not a transcoding operation! Its something like StreamOpFile.
        args = args.stream()
            .map(x -> x instanceof StreamOpFile f ? new StreamOpTranscode("cat", TranscodeMode.DECODE, f) : x)
            .toList();

        StreamTransformToCmdOp sysCallTransform = sysCallTransform();

        StreamOp javaOp = StreamOpConcat.of(args);
        ByteSource javaByteSource = new ByteSourceOverStreamOp(javaOp);

        // Try to compile the codec op to a system call.
        StreamOp sysOp = StreamOpTransformer.transform(javaOp, sysCallTransform);

        ByteSourceSpec result;
        if (sysOp instanceof StreamOpCommand codecOp) {
            CmdOp cmdOp = codecOp.getCmdOp();
            // String[] cmd = SysRuntimeImpl.forBash().compileCommand(cmdOp);
            result = new ByteSourceSpec(cmdOp, javaByteSource, lang);
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
        List<StreamOp> ops = args.stream().map(this::convertArgToOp).toList();

        boolean isAllFiles = ops.stream().allMatch(op -> op instanceof StreamOpFile);

        InputSpec result;
        if (isAllFiles) {
            FileSpec fileSpec = buildFileSpec();
            result = new InputSpec(fileSpec, null, null);
        } else {
            if (usedLangs.contains(Lang.NQUADS) && usedLangs.contains(Lang.TURTLE)) {
                throw new RuntimeException("Unsupported mix of languages: nq + ttl");
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
        logger.info("Setting up qlever indexer container as UID: " + uid + ", GID: " + gid);

        String str = "IndexBuilderMain -i " + indexName + (cmdStr.isEmpty() ? " " : " ") + cmdStr;
        logger.info("Start command: " + str);

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
            .withCommand(new String[]{str})
            .withLogConsumer(frame -> logger.info(frame.getUtf8StringWithoutLineEnding()))
            // .withCommand(new String[]{"ServerMain -h"})
            ;

        return result;
    }

    protected void runContainerWithInputStream(ByteSource byteSource, Lang lang) throws InterruptedException, IOException {

        String fmt = langToFormat(lang);
        // Read from stdin, data in fmt, parallel parsing (true/false)
        String optsStr = "-f - -F " + fmt + " -p true";

        logger.info("Attempting to launch container with a JVM-based input stream.");
        org.testcontainers.containers.GenericContainer<?> container = setupContainer(optsStr)
            .withCreateContainerCmdModifier(cmd -> cmd
                // .withTty(true)         // Required to keep input open
                .withTty(false)
                // .withStdInOnce(true)
                .withStdinOpen(true)
                .withAttachStdin(true) // Allow attaching input stream
                // .withAttachStdout(true)
                // .withAttachStderr(true)
            );

            container.start();
//            container.followOutput(outputFrame -> {
//                String msg = outputFrame.getUtf8String();
//                logger.info(msg);
//            });

            System.out.println("Waiting");
            Thread.sleep(2000);
            System.out.println("Attaching data");

        // Get input stream (e.g., file or command output)
        try (InputStream in = byteSource.openStream()) {
            String str = IOUtils.toString(in, StandardCharsets.UTF_8);
            System.out.println(str);
            InputStream is = new ByteArrayInputStream(str.getBytes());

            // BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharset.UTF_8));
            // br.lines().forEach(System.out::println);

            // Attach input stream to the container
            // Adapter<Frame> xxx =
                AttachContainerCmd tmp = container.getDockerClient()
                .attachContainerCmd(container.getContainerId())
                .withStdIn(is)
                // .withStdErr(true)
                // .withStdOut(true)
                // .withFollowStream(true)
                ;
                // .withLogs(true);


               Adapter<Frame> callback = new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        String msg = new String(frame.getPayload(), StandardCharset.UTF_8);
                        logger.info(msg);
                        super.onNext(frame);
                    }
               };
               System.out.println("Waiting");
               Thread.sleep(5000);
               System.out.println("Awaiting completion");
               tmp.exec(callback).awaitCompletion();

               // tmp.exec(new AttachContainerResultCallback()).awaitCompletion();

            // x.exec(new AttachContainerResultCallback()).awaitCompletion();

            // ResultCallbackTemplate<?, Frame> foo = x.start();
            System.out.println("Done");

            // x.getStdin()
                //.exec(new AttachContainerResultCallback());
                // .awaitCompletion();f
            // container.waitingFor(WaitStrategy)
        }

        container.getDockerClient()
            .waitContainerCmd(container.getContainerId())
            .exec(new WaitContainerResultCallback())
            .awaitCompletion();

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
//        container.followOutput(outputFrame -> {
//            String msg = outputFrame.getUtf8String();
//            logger.info(msg);
//        });
        container.getDockerClient()
            .waitContainerCmd(container.getContainerId())
            .exec(new WaitContainerResultCallback())
            .awaitCompletion();
    }

    protected String langToFormat(Lang lang) {
        return lang.getFileExtensions().get(0);
    }

    protected GenericContainer<?> setupContainerSysCall(String fileName, Lang lang) throws NumberFormatException, IOException, InterruptedException {
        int uid = SystemUtils.getUID();
        int gid = SystemUtils.getGID();
        logger.info("Attempting to launch container via syscall. UID: " + uid + ", GID: " + gid);

        String dockerImageName = buildDockerImageName();
        String fmt = langToFormat(lang);

        GenericContainer<?> result = new GenericContainer<>(dockerImageName)
            .withWorkingDirectory("/data")
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(uid + ":" + gid))
            .withFileSystemBind(outputFolder.toString(), "/data", BindMode.READ_WRITE)
            .withCommand(new String[]{"IndexBuilderMain -i " + indexName + " -f " + fileName + " -F " + fmt})
            //.withCommand(new String[]{SysRuntimeImpl.quoteArg("IndexBuilderMain -i " + indexName + " -f " + fileName + " -F " + fmt)})
            ;

        return result;
    }

    protected void runContainerViaSysCallWithInputStream(ByteSource byteSource, Lang lang) throws InterruptedException, IOException {
        GenericContainer<?> container = setupContainerSysCall("-", lang);
        CmdOp cmdOp = CmdOpExec.of(container.buildCmdLine());

        SysRuntime runtime = getRuntime();
        String[] cmd = runtime.compileCommand(cmdOp);
        cmd = runtime.resolveCommand(cmd);

        logger.info("CmdOp:" + cmdOp);
        logger.info("EffectiveCommand: " + Arrays.asList(cmd));

        Process process = SystemUtils.run(logger::info, cmd);
        try (OutputStream out = process.getOutputStream()) {
            try (InputStream in = byteSource.openStream()) {
                in.transferTo(out);
            }
            out.flush();
        }
        process.waitFor();
        int exitValue = process.exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Process failed, exit value: " + exitValue);
        }
    }

    protected void runContainerViaSysCall(CmdOp generatorCmd, Lang lang) throws NumberFormatException, IOException, InterruptedException {
        GenericContainer<?> container = setupContainerSysCall("-", lang);
        String[] cmdLine = container.buildCmdLine();
        CmdOp pipe = new CmdOpPipe(generatorCmd, CmdOpExec.of(cmdLine));

        SysRuntime runtime = getRuntime();
        String[] cmd = runtime.compileCommand(pipe);

        logger.info("CmdOp:" + pipe);
        logger.info("EffectiveCommand: " + Arrays.asList(cmd));

        Process process = SystemUtils.run(logger::info, cmd);
        process.waitFor();
        int exitValue = process.exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Process failed, exit value: " + exitValue);
        }
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
        InputSpec inputSpec = buildInputSpec();

        FileSpec fileSpec = inputSpec.fileSpec();
        if (fileSpec != null) {
            runContainerWithFileArgs(fileSpec);
        } else {
            ByteSourceSpec byteSourceSpec = inputSpec.byteSourceSpec();
            CmdOp cmdOp = byteSourceSpec.cmdOp();
            // String[] cmd = byteSourceSpec.cmd();
            ByteSource byteSource = byteSourceSpec.byteSource();
            Lang lang = byteSourceSpec.lang();
            if (cmdOp != null) {
                runContainerViaSysCall(cmdOp, lang);
            } else if (byteSource != null) {
                // runContainerWithInputStream(byteSource, lang);
                runContainerViaSysCallWithInputStream(byteSource, lang);
            } else {
                throw new IllegalStateException("Unexpected error: Failed to determine a strategy to process the input data");
            }
        }

        RdfDatabaseQlever result = new RdfDatabaseQlever(outputFolder, indexName);
        return result;
    }
}

