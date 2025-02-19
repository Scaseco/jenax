package jenax.engine.qlever.docker;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.dataaccess.sparql.creator.FileSetOverPathBase;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineBuilder;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryLegacyBase;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryLegacyBase.CloseablePath;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceSpecBasic;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceSpecBasicFromMap;
import org.aksw.jenax.engine.qlever.RdfDatabaseQlever;
import org.aksw.jenax.engine.qlever.SystemUtils;
import org.aksw.shellgebra.exec.CmdStrOps;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;

public class RDFEngineBuilderQlever<X extends RDFEngineBuilderQlever<X>>
    extends RdfDataSourceSpecBasicFromMap<X>
    implements RDFEngineBuilder<X>
{
    private static final Logger logger = LoggerFactory.getLogger(RDFEngineBuilderQlever.class);

    // public static class State {
//        public String location; // hostDbDir
//        public String qleverImageName;
//        public String qleverImageTag;
//        public Integer hostPort;
//        public QleverConfRun conf;
//        public RdfDatabaseQlever database;
//        public String indexName;
//
//        public String tempDir;
//        public Boolean autoDeleteIfCreated;
    //}

    // protected RdfDataEngineBuilderQlever<?> state = this; // new State();

    public static final String INDEX_NAME_KEY = "indexName";
    public static final String IMAGE_NAME_KEY = "imageName";
    public static final String IMAGE_TAG_KEY = "imageTag";
    public static final String CONFIG_KEY = "config";

    public RDFEngineBuilderQlever() {
        this(null, null);
    }

    public RDFEngineBuilderQlever(String qleverImageName, String qleverImageTag) {
        super();
        setImageName(qleverImageName);
        setImageTag(qleverImageTag);
        setConfig(new QleverConfRun());
//        this.qleverImageName = qleverImageName;
//        this.qleverImageTag = qleverImageTag;
        // this.conf = new QleverConfRun();
    }

    public static RdfDataEngine run(String hostDbDir, String qleverImageName, String qleverImageTag, Integer hostPort, QleverConfRun conf) throws NumberFormatException, IOException, InterruptedException {

        Integer containerPort = conf.getPort();
        if (containerPort == null) {
            throw new RuntimeException("Container port must be set.");
        }


        int uid = SystemUtils.getUID();
        int gid = SystemUtils.getGID();
        logger.info("Running as UID: " + uid + ", GID: " + gid);

        // Build command line
        List<String> cmdArgs = new ArrayList<>();
        QleverCliUtils.accumulateCliOptions(cmdArgs, conf);
        CmdStrOps strOps = SysRuntimeImpl.forCurrentOs().getStrOps();
        String cmdArgStr = cmdArgs.stream().map(strOps::quoteArg).collect(Collectors.joining(" "));
        String cmdStr = "ServerMain";
        if (!cmdArgStr.isEmpty()) {
            cmdStr += " " + cmdArgStr;
        }

        logger.info("Generated command line: " + cmdStr);

        if (qleverImageName == null) {
            qleverImageName = QleverConstants.DOCKER_IMAGE_NAME;
        }

        if (qleverImageTag == null) {
            qleverImageTag = QleverConstants.DOCKER_IMAGE_TAG;
        }

        String dockerImageName = Stream.of(qleverImageName, qleverImageTag)
            .filter(x -> x != null)
            .collect(Collectors.joining(":"));

        // https://hub.docker.com/r/adfreiburg/qlever/tags
        org.testcontainers.containers.GenericContainer<?> container = new org.testcontainers.containers.GenericContainer<>(dockerImageName)
            .withWorkingDirectory("/data")
            .withExposedPorts(containerPort)
            // Setting UID does not work with latest image due to
            // error "UID 1000 already exists" ~ 2025-01-31
            // .withEnv("UID", Integer.toString(uid))
            // .withEnv("GID", Integer.toString(gid))
            .withCreateContainerCmdModifier(cmd -> cmd.withUser(uid + ":" + gid))
            .withFileSystemBind(hostDbDir, "/data", BindMode.READ_WRITE)
            .withCommand(new String[]{cmdStr})
            ;

        // Test containers will allocate a port if an explicit mapping is omitted.
        if (hostPort != null) {
            container.setPortBindings(List.of(hostPort + ":" + containerPort));
        }

        container.start();

        String serviceUrl = "http://" + container.getHost() + ":" + container.getMappedPort(containerPort);
        logger.info("Started Qlever server at: " + serviceUrl);

        container.followOutput(outputFrame -> {
            String msg = outputFrame.getUtf8StringWithoutLineEnding();
            logger.info(msg);
        });

        // TODO Make it possible to mutate the HTTP connection creation
        RdfDataEngine result = RdfDataEngines.of(
            () -> RDFConnectionRemote.service(serviceUrl).build(),
            () -> container.stop());

        return result;
    }

    @Override
    public RdfDataEngine build() throws Exception {
        RdfDataSourceSpecBasic spec = RdfDataSourceSpecBasicFromMap.wrap(map);
        CloseablePath entry = RDFEngineFactoryLegacyBase.setupPath("rpt-qlever-", spec);
        Path finalDbPath = entry.path();
        Closeable partialCloseAction = entry.closeable();

        RdfDataEngine result;
        try {
            String location = finalDbPath.toString(); // getLocation();
            String qleverImageName = getImageName();
            String qleverImageTag = getImageTag();
            // Integer hostPort = getPort();
            QleverConfRun conf = getConfig();

            String indexName = getImageName();
            if (indexName == null) {
                indexName = "default";
            }
            Integer hostPort = null;

            int defaultContainerPort = 8080;

            QleverConfRun finalConf = new QleverConfRun();
            conf.copyInto(finalConf, false);
            finalConf.setIndexBaseName(indexName);
            finalConf.setPort(defaultContainerPort);

            // Check whether the database is currently empty
            if (Boolean.TRUE.equals(spec.isAutoDeleteIfCreated())) {
                RdfDatabaseQlever database = new RdfDatabaseQlever(finalDbPath, indexName);
                FileSetOverPathBase fileSet = database.getFileSet();
                if (fileSet.isEmpty()) {
                    logger.info("Auto delete enabled and database fileset is empty. Will delete " + fileSet.getBasePath());

                    Closeable tmp = partialCloseAction;
                    partialCloseAction = () -> {
                        try {
                            database.getFileSet().delete();
                        } finally {
                            tmp.close();
                        }
                    };
                }
            }

            result = run(location, qleverImageName, qleverImageTag, hostPort, finalConf);
            result = RdfDataEngines.wrapWithCloseAction(result, partialCloseAction);
        } catch (Throwable e) {
            partialCloseAction.close();
            throw new RuntimeException(e);
        }
        // RdfDataEngines.wrapWithCloseAction(result, null);
        // container.getDockerClient().waitContainerCmd(container.getContainerId()).exec(new WaitContainerResultCallback()).awaitCompletion();

        return result;
    }

    public X setIndexName(String indexName) {
        map.put(INDEX_NAME_KEY, indexName);
        return self();
    }

    public String getIndexName() {
        return (String)map.get(INDEX_NAME_KEY);
    }

    public X setImageTag(String imageTag) {
        map.put(IMAGE_TAG_KEY, imageTag);
        return self();
    }

    public String getImageTag() {
        return (String)map.get(IMAGE_TAG_KEY);
    }

    public X setImageName(String imageName) {
        map.put(IMAGE_NAME_KEY, imageName);
        return self();
    }

    public String getImageName() {
        return (String)map.get(IMAGE_NAME_KEY);
    }


    public X setConfig(QleverConfRun qleverConfRun) {
        map.put(CONFIG_KEY, qleverConfRun);
        return self();
    }

    public QleverConfRun getConfig() {
        return (QleverConfRun)map.get(CONFIG_KEY);
    }

    @Override
    public X setProperty(String key, Object value) {
        map.put(key, value);
//        try {
//            logger.info("Setting attribute: " + key + " -> " + value);
//            try {
//                BeanUtils.setProperty(this, key, value);
//            } catch (IllegalAccessException | InvocationTargetException e) {
//                logger.error("Error:", e);
//            }
//        } catch (Throwable e) { // May raise NoClassDefFoundError
//            throw new RuntimeException("Failed to set attribute: " + key + " -> " + value, e);
//        }
        return self();
    }

    @Override
    public X setProperties(Map<String, Object> values) {
        values.forEach(this::setProperty);
        return self();
    }

    @Override
    public X setDatabase(RDFDatabase database) {
        if (database instanceof RdfDatabaseQlever db) {
            map.put("database", db);
            Path path = db.getPath().toAbsolutePath();
            setLocationContext(null);
            setLocation(path.toString());
            setIndexName(db.getIndexName());

        } else {
            throw new IllegalArgumentException("Argument is not a qlever database: " + database);
        }
        return self();
    }

    @Override
    public RDFDatabase getDatabase() {
        return (RDFDatabase)map.get("database");
        // return database;
    }
}
