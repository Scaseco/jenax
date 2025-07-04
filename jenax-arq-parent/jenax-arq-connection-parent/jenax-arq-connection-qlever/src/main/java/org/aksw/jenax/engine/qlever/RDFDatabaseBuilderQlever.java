package org.aksw.jenax.engine.qlever;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.exception.FinallyRunAll;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.arq.util.prefix.ShortNameMgr;
import org.aksw.jenax.dataaccess.sparql.creator.FileSet;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabaseBuilder;
import org.aksw.jenax.engine.docker.common.ContainerPathResolver;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpFile;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipe;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpRedirect;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpSubst;
import org.aksw.shellgebra.algebra.common.TranscodeMode;
import org.aksw.shellgebra.algebra.stream.op.CodecSysEnv;
import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpCommand;
import org.aksw.shellgebra.algebra.stream.op.StreamOpConcat;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpTransformExecutionPartitioner;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpTransformExecutionPartitioner.Location;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpTransformSubst;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpTransformToCmdOp;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpEntry;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformer;
import org.aksw.shellgebra.exec.FileWriterTask;
import org.aksw.shellgebra.exec.FileWriterTaskFromByteSource;
import org.aksw.shellgebra.exec.FileWriterTaskFromProcess;
import org.aksw.shellgebra.exec.FileWriterTaskNoop;
import org.aksw.shellgebra.exec.PathLifeCycles;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.registry.CodecRegistry;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
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
import jenax.engine.qlever.docker.QleverConstants;

public class RDFDatabaseBuilderQlever<X extends RDFDatabaseBuilderQlever<X>>
    implements RDFDatabaseBuilder<X>
{
    public static final List<Lang> supportedLangs = Collections.unmodifiableList(Arrays.asList(Lang.TURTLE, Lang.NQUADS));

    private static final Logger logger = LoggerFactory.getLogger(RDFDatabaseBuilderQlever.class);

    /** Record to capture arguments passed to this builder */
    public record FileArg(Path path, Lang lang, List<String> encodings, Node graph) {}

    /** Record to capture a set of files that make up a Qlever database. */
    public record QleverDbFileSet(List<Path> paths) implements FileSet {
        @Override
        public List<Path> getPaths() {
            return paths;
        }
    }

    protected QleverIndexBuilderConfig config = new QleverIndexBuilderConfigPojo();

//    protected String dockerImageName;
//    protected String dockerImageTag;

    /** Mapping from absolute file paths on the host names to file names. */
    protected ShortNameMgr shortNameMgr = new ShortNameMgr();

    /** */
    protected SysRuntime sysRuntime;

//    protected Path outputFolder = null;
    protected List<FileArg> args = new ArrayList<>();
    protected List<Entry<Lang, Throwable>> errorCollector = new ArrayList<>();

//    protected String indexName;

//    protected String stxxlMemory = null;

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

    @Override
    public X addPath(String source, Node g) throws IOException {
        Path path = Path.of(source);
        RdfEntityInfo entityInfo = RDFDataMgrEx.probeEntityInfo(() -> Files.newInputStream(path, StandardOpenOption.READ), supportedLangs);
        String contentType = entityInfo.getContentType();
        Lang lang = RDFLanguages.contentTypeToLang(contentType);
        addPath(path, g, entityInfo.getContentEncodings(), lang);
        return self();
    }

    protected void addPath(Path source, Node graph, List<String> encodings, Lang lang) {
        FileArg arg = new FileArg(source, lang, encodings, graph);
        args.add(arg);
    }

    public StreamOp convertArgToOp(FileArg arg) {
        Path path = arg.path();
        StreamOp result = new StreamOpFile(path.toString());
        for (String encoding : arg.encodings()) {
            result = new StreamOpTranscode(encoding, TranscodeMode.DECODE, result);
        }
        return result;
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

    protected StreamOpTransformToCmdOp sysCallTransform() {
        CodecRegistry reg = CodecRegistry.get();
        SysRuntime runtime = getRuntime();
        CodecSysEnv env = new CodecSysEnv(runtime);
        StreamOpTransformToCmdOp sysCallTransform = new StreamOpTransformToCmdOp(reg, env); // , Mode.COMMAND_GROUP);
        return sysCallTransform;
    }

    /**
     * On the host side: create a FileWriter for a file that can be mounted into the container
     * bridge: create a bind of the host file to a container path.
     *   (the host file is not required to exist at this stage)
     * On the container side: create a StreamOp that reads the container file.
     *
     *
     * @return
     */
//    public StreamOpTransformExecutionPartitioner execPartitionTransform() {
//        StreamOpTransformToCmdOp sysCallTransform = sysCallTransform();
//        StreamOpTransformExecutionPartitioner execPartitioner = new StreamOpTransformExecutionPartitioner(sysCallTransform);
//
//
//    }

    protected ByteSourceSpec buildByteSourceCmd(List<StreamOp> args, Lang lang) {
        // Inject a dummy codec 'cat' to cat immediate file arguments
        // FIXME HACK 'cat' is certainly not a transcoding operation! Its something like StreamOpFile
        args = args.stream()
            .map(x -> x instanceof StreamOpFile f ? new StreamOpTranscode("cat", TranscodeMode.DECODE, f) : x)
            .toList();

        StreamOpTransformToCmdOp sysCallTransform = sysCallTransform();

        StreamOp javaOp = StreamOpConcat.of(args);
        SysRuntime sysRuntime = SysRuntimeImpl.forCurrentOs();

        ByteSource javaByteSource = new ByteSourceOverStreamOp(javaOp); // TODO Supply sysRuntime

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


    /**
     * hostFileWriterTask:
     *               The name of a regular file or a named pipe on the host.
     *               The writer may be a noop or a generator for that file's content.
     * bind:         Docker bind specification of the hostFile into a container path
     * cmdContrib:   Contribution to the docker invocation. Contains the container file path and any post-processing.
     */
    public record DockerDataArgumentBridge(FileWriterTask hostFileWriterTask, Bind bind, String[] cmdContrib) {}

    public record InputSpec(List<DockerDataArgumentBridge> dataBridges) {}

    protected InputSpec buildInputSpec(Supplier<Path> hostTempPath) throws NoSuchFileException {

        List<DockerDataArgumentBridge> dataBridges = new ArrayList<>(args.size());
        // For each file, check whether any operations need to be performed on the host
        for (FileArg fileArg : args) {
            Lang lang = fileArg.lang();
            Node graph = fileArg.graph();
            StreamOp op = convertArgToOp(fileArg);

            // Convert the host operations to FileWriterTasks
            DockerDataArgumentBridge fileSpec = buildHostToContainerDataBridge(hostTempPath, op, graph, lang);
            dataBridges.add(fileSpec);
        }
        return new InputSpec(dataBridges);
        // List<StreamOp> ops = args.stream().map(this::convertArgToOp).toList();
    }

    public static record FileAndCmd(String fileName, String[] cmd) {}

    protected FileAndCmd buildCmdPart(String containerBasePath, StreamOp containerOp, String fileArg, Node graph, Lang lang) {
        CmdOp cmdOp;
        if (containerOp instanceof StreamOpFile opFile) {
            cmdOp = new CmdOpFile(opFile.getPath());
        } else {
            // String fileArg = StreamOpPlanner.streamOpToFileName(containerOp);
            StreamOpTransformToCmdOp sysCallTransform = sysCallTransform();
            StreamOp sysOp = StreamOpTransformer.transform(containerOp, sysCallTransform);

            if (sysOp instanceof StreamOpCommand streamOpCmd) {
                cmdOp = streamOpCmd.getCmdOp();
                cmdOp = new CmdOpSubst(cmdOp);

//                SysRuntime runtime = getRuntime();
//                String[] cmd = runtime.compileCommand(cmdOp);
                // SysRuntimeImpl.forCurrentOs().
            } else {
                throw new IllegalStateException("Op unexpectedly did not compile to a command.");
            }
        }

        SysRuntime sysRuntime = getRuntime();
        // CmdOpVisitorToProcessSubstString stringifier = new CmdOpVisitorToProcessSubstString(sysRuntime.getStrOps());


        // String fileArg = cmdOp.accept(stringifier);

//        String fileArg = Optional.ofNullable(arg)
//                .map(Path::toAbsolutePath)
//                .map(Path::toString)
//                .orElse("-");

        String uriStr = fileArg;
        String shortName = shortNameMgr.allocate(uriStr).localName();
        String filePath = containerBasePath + shortName;

        String graphArg = Optional.ofNullable(graph)
            .filter(Node::isURI)
            .filter(g -> !Quad.isDefaultGraph(g))
            .map(Node::getURI).orElse("-");

        String fmtArg = Optional.ofNullable(lang)
            .map(l -> l.getFileExtensions())
            .map(l -> l.isEmpty() ? null : l.get(0))
            .orElse("");

        String[] cmdContrib = new String[] { "-f", filePath, "-F", fmtArg, "-g", graphArg };
        // String cmdPart = "-f " + shortName + " -F " + fmtArg + " -g " + graphArg;
        // cmdParts.add(cmdPart);
        return new FileAndCmd(filePath, cmdContrib);
    }

    protected FileWriterTask createHostFileWriter(Supplier<Path> tempPathSupp, StreamOp sysCallOp) throws NoSuchFileException {
        StreamOpTransformToCmdOp sysCallTransform = sysCallTransform();
        FileWriterTask hostFileWriter;
        if (sysCallOp instanceof StreamOpFile opFile) {
            // Case 1: Direct filename
            String hostFileName = opFile.getPath();
            Path hostPath = Path.of(hostFileName).toAbsolutePath();
            if (!Files.exists(hostPath)) {
                throw new NoSuchFileException("" + hostPath);
            }
            hostFileWriter = new FileWriterTaskNoop(hostPath);
        } else {
            // Case 2: Stream to named pipe - we need to allocate a file on the host
            Path tempPath = tempPathSupp.get();
            String hostFileName = StreamOpPlanner.streamOpToFileName(sysCallOp);

            String plainFileName = Path.of(hostFileName).getFileName().toString();
            Path hostPath = tempPath.resolve(plainFileName);
            // Path hostPath = Path.of(hostFileName).toAbsolutePath();

            StreamOp sysOp = StreamOpTransformer.transform(sysCallOp, sysCallTransform);

            if (sysOp instanceof StreamOpCommand streamOpCmd) {
                CmdOp cmdOp = streamOpCmd.getCmdOp();
                String hostPathStr = hostPath.toString();
                CmdOpRedirect redirectOp = new CmdOpRedirect(hostPathStr, cmdOp);

                SysRuntime runtime = getRuntime();
                String[] cmd = runtime.compileCommand(redirectOp);
                hostFileWriter = new FileWriterTaskFromProcess(hostPath, PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe()), cmd);
            } else {
                throw new IllegalStateException("Execution partitioner suggested that operation could be executed via sys call - but sys call generation failed.");
            }
        }
        return hostFileWriter;
    }

//    protected String toFlatPath(Path folderPat, Path filePath) {
//
//    }

    protected DockerDataArgumentBridge buildHostToContainerDataBridge(Supplier<Path> hostTempPathSupp, StreamOp op, Node graph, Lang lang) throws NoSuchFileException {
        StreamOpTransformToCmdOp sysCallTransform = sysCallTransform();
        StreamOpTransformExecutionPartitioner execPartitioner = new StreamOpTransformExecutionPartitioner(sysCallTransform);

        // The operation that could not be handled by system calls
        StreamOpEntry<Location> nonSysCallOpEntry = StreamOpTransformer.transform(op, execPartitioner);
        StreamOp nonSysCallOp = nonSysCallOpEntry.getKey();
        Location location = nonSysCallOpEntry.getValue();

        // The operation(s) executable on the host - there should typically be at most one.
        Map<String, StreamOp> hostOps = execPartitioner.getVarToOp();

        // Process the host side
        FileWriterTask hostFileWriter;
        String plainFileName;
        if (location == Location.HANDLED) {
            hostFileWriter = createHostFileWriter(hostTempPathSupp, nonSysCallOp);
            plainFileName = hostFileWriter.getOutputPath().getFileName().toString();
//            if (nonSysCallOp instanceof StreamOpFile) {
//                // We can use the file directly
//                hostFileWriter = createHostFileWriter(hostTempPathSupp, nonSysCallOp);
//            } else if (nonSysCallOp instanceof StreamOpVar opVar) {
//                // Here everything can be handled by a sys call.
//                // We either have a direct file or a stream.
//                String varName = opVar.getVarName();
//                StreamOp sysCallOp = hostOps.get(varName);
//                hostFileWriter = createHostFileWriter(hostTempPathSupp, sysCallOp);
//            } else {
//                throw new IllegalStateException("Support for other ops not implemented yet.");
//            }
        } else {
            // We need to handle some parts in java - expand variables with their definitions again.
            Path hostTempPath = hostTempPathSupp.get();
            String hostFileName = StreamOpPlanner.streamOpToFileName(nonSysCallOp, hostOps::get);
            plainFileName = Path.of(hostFileName).getFileName().toString();
            Path hostPath = hostTempPath.resolve(plainFileName); // Path.of(hostFileName).toAbsolutePath();
            // StreamOp sysOp = StreamOpTransformer.transform(sysCallOp, sysCallTransform);

            StreamOp finalOp = StreamOpTransformSubst.subst(nonSysCallOp, hostOps);
            ByteSource javaByteSource = new ByteSourceOverStreamOp(finalOp, hostOps); // TODO Supply sysRuntime
            logger.info("Transformed: " + hostPath + " -> " + finalOp);
            logger.info("Host Ops: " + hostOps);
            hostFileWriter =  new FileWriterTaskFromByteSource(hostPath, PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe()), javaByteSource);
        }

        FileAndCmd containerFileAndCmd = buildCmdPart(containerFifoPath, op, plainFileName, graph, lang);

        // Path finalHostFileWriterPath = ContainerPathResolver.resolvePath(containerPathResolver, hostFileWriter.getOutputPath());
        Path finalHostFileWriterPath = hostFileWriter.getOutputPath();

        Bind bind = new Bind(finalHostFileWriterPath.toString(), new Volume(containerFileAndCmd.fileName()), AccessMode.ro);

        DockerDataArgumentBridge result = new DockerDataArgumentBridge(hostFileWriter, bind, containerFileAndCmd.cmd());
        return result;
    }

    protected org.testcontainers.containers.GenericContainer<?> setupContainer(String indexName, String cmdStr) throws NumberFormatException, IOException, InterruptedException {
        int uid = SystemUtils.getUID();
        int gid = SystemUtils.getGID();
        logger.info("Setting up qlever indexer container as UID: " + uid + ", GID: " + gid);

        List<String> cmdParts = new ArrayList<>();
        cmdParts.add("IndexBuilderMain -i " + indexName);

        String stxxlMemory = config.getStxxlMemory();
        if (stxxlMemory != null && !stxxlMemory.isBlank()) {
            cmdParts.add("-m " + stxxlMemory); // XXX Not escaped!
        }

        if (cmdStr != null) {
            cmdParts.add(cmdStr);
        }

        String str = cmdParts.stream().collect(Collectors.joining(" "));
        logger.info("Start command: " + str);

        String imageName = config.getDockerImageName();
        String imageTag = config.getDockerImageTag();
        Path outputFolder = config.getOutputFolder();
        String finalImageName = QleverConstants.buildDockerImageName(imageName, imageTag);
        Path finalOutputFolder = ContainerPathResolver.resolvePath(containerPathResolver, outputFolder);

        org.testcontainers.containers.GenericContainer<?> result = new org.testcontainers.containers.GenericContainer<>(finalImageName)
            .withWorkingDirectory("/data/")
            // .withExposedPorts(containerPort)
            // Setting UID does not work with latest image due to
            // error "UID 1000 already exists" ~ 2025-01-31
            // .withEnv("UID", Integer.toString(uid))
            // .withEnv("GID", Integer.toString(gid))
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(uid + ":" + gid))
            .withFileSystemBind(finalOutputFolder.toString(), "/data/", BindMode.READ_WRITE)
            .withCommand(new String[]{str})
            .withLogConsumer(frame -> logger.info(frame.getUtf8StringWithoutLineEnding()))
            // .withCommand(new String[]{"ServerMain -h"})
            ;

        return result;
    }

    protected void runContainerWithInputStream(ByteSource byteSource, Lang lang) throws InterruptedException, IOException {
        String finalIndexName = getFinalIndexName();

        String fmt = langToFormat(lang);
        // Read from stdin, data in fmt, parallel parsing (true/false)
        String optsStr = "-f - -F " + fmt + " -p true";

        logger.info("Attempting to launch container with a JVM-based input stream.");
        org.testcontainers.containers.GenericContainer<?> container = setupContainer(finalIndexName, optsStr)
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
        String finalIndexName = getFinalIndexName();

        logger.info("Attempting to launch container with binds and file arg");
        String cmdStr = Arrays.asList(fileSpec.fileArgs).stream().collect(Collectors.joining(" "));

        org.testcontainers.containers.GenericContainer<?> container = setupContainer(finalIndexName, cmdStr);

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

        String indexName = config.getIndexName();
        String imageName = config.getDockerImageName();
        String imageTag = config.getDockerImageTag();
        Path outputFolder = config.getOutputFolder();
        // Path finalOutputFolder = ContainerPathResolver.resolvePath(containerPathResolver, outputFolder);

        String finalImageName = QleverConstants.buildDockerImageName(imageName, imageTag);
        String fmt = langToFormat(lang);

        GenericContainer<?> result = new GenericContainer<>(finalImageName)
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
                        logger.warn("Could not delete fifo folder on host: " + p, e);
                    }
                }
            });

            InputSpec spec = buildInputSpec(getHostTempPath);

            logger.info("Attempting to launch container with binds and file arg");
            SysRuntime runtime = SysRuntimeImpl.forCurrentOs();

            String cmdSuffix = spec.dataBridges().stream()
                .map(DockerDataArgumentBridge::cmdContrib)
                .flatMap(Stream::of)
                .map(arg -> {
                    String str = runtime.quoteFileArgument(arg);
                    return str;
                })
                .collect(Collectors.joining(" "));

//            for (DockerDataArgumentBridge dataBridge : spec.dataBridges()) {
//                String[] contrib = dataBridge.cmdContrib();
//            }

            try (org.testcontainers.containers.GenericContainer<?> container = setupContainer(finalIndexName, cmdSuffix)) {

                // Declare binds on the container
                if (true) {
                    for (DockerDataArgumentBridge dataBridge : spec.dataBridges()) {
                        Bind bind = dataBridge.bind();
                        BindMode bindMode = AccessMode.ro.equals(bind.getAccessMode())
                            ? BindMode.READ_ONLY
                            : null;

                        String finalHostPathStr = ContainerPathResolver.resolvePathString(containerPathResolver, bind.getPath());

                        logger.info("Adding binding: " + finalHostPathStr + " -> " + bind.getVolume().getPath());
                        container.withFileSystemBind(finalHostPathStr, bind.getVolume().getPath(), bindMode);
                    }
                } else {
                    Path p = tempPath[0];
                    if (p != null) {

                        container.withFileSystemBind(p.toString(), containerFifoPath, BindMode.READ_WRITE);
                    }
                }
                // Add the output folder
                // container.addFileSystemBind(outputFolder.toAbsolutePath().toString(), "/data", BindMode.READ_WRITE);


                // Start writing host files
                for (DockerDataArgumentBridge dataBridge : spec.dataBridges()) {
                    FileWriterTask task = dataBridge.hostFileWriterTask();
                    closer.addThrowing(task::close);
                    task.start();
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

    /** Record to capture whether to pass input data as files or as an input stream. */
    // XXX The lang argument is not ideal here - a mime type would be more generic.
    /*
    public record InputSpecOld(FileSpec fileSpec, ByteSourceSpec byteSourceSpec, Lang byteSourceSpecLang) {}

    protected InputSpecOld buildInputSpecOld() {
        Set<Lang> usedLangs = args.stream().map(FileArg::lang).collect(Collectors.toSet());
        // TODO Remove subsumed languages
        // Check whether any arguments require decoding.
        // RDFLanguagesEx.streamSubLangs(null)
        List<StreamOp> ops = args.stream().map(this::convertArgToOp).toList();

        boolean isAllFiles = ops.stream().allMatch(op -> op instanceof StreamOpFile);

        InputSpecOld result;
        if (isAllFiles) {
            FileSpec fileSpec = buildFileSpec();
            result = new InputSpecOld(fileSpec, null, null);
        } else {
            if (usedLangs.contains(Lang.NQUADS) && usedLangs.contains(Lang.TURTLE)) {
                throw new RuntimeException("Unsupported mix of languages: nq + ttl");
            }

            Lang finalLang = usedLangs.iterator().next();
            ByteSourceSpec byteSourceCmd = buildByteSourceCmd(ops, finalLang);
            result = new InputSpecOld(null, byteSourceCmd, finalLang);
        }
        return result;
    }

    public RdfDatabaseQlever buildOld() throws IOException, InterruptedException {
        InputSpecOld inputSpec = null ;// buildInputSpec();

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

        protected FileSpec buildFileSpec() {
        List<String> cmdParts = new ArrayList<>();
        Map<String, String> fsBinds = new LinkedHashMap<>();
        for (FileArg arg : args) {

            Path path = arg.path();
            if (containerPathResolver != null) {
                if (path != null) {
                    path = path.toAbsolutePath();
                    Path resolvedPath = containerPathResolver.resolve(path);
                    logger.info("Resolved path: " + path + " -> " + resolvedPath);
                    path = resolvedPath;
                }
            }

            String fileArg = Optional.ofNullable(path)
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
    */
}

