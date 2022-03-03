package org.aksw.difs.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.io.util.symlink.SymbolicLinkStrategy;
import org.aksw.commons.io.util.symlink.SymbolicLinkStrategyStandard;
import org.aksw.commons.lock.LockManager;
import org.aksw.commons.lock.LockManagerCompound;
import org.aksw.commons.lock.LockManagerPath;
import org.aksw.commons.lock.ThreadLockManager;
import org.aksw.commons.txn.api.TxnMgr;
import org.aksw.commons.txn.impl.ResourceRepoImpl;
import org.aksw.commons.txn.impl.ResourceRepository;
import org.aksw.commons.txn.impl.TxnMgrImpl;
import org.aksw.difs.index.api.DatasetGraphIndexPlugin;
import org.aksw.difs.index.api.RdfTermIndexerFactory;
import org.aksw.difs.index.impl.DatasetGraphIndexerFromFileSystem;
import org.aksw.difs.sys.vocab.jena.DIFS;
import org.aksw.difs.system.domain.IndexDefinition;
import org.aksw.difs.system.domain.StoreDefinition;
import org.aksw.jena_sparql_api.difs.main.DatasetGraphFromTxnMgr;
import org.apache.jena.dboe.sys.ProcessUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Streams;
import com.kstruct.gethostname4j.Hostname;

/**
 * Builder for a dataset-in-filesystem.
 *
 * @author raven
 *
 */
public class DifsFactory {
    private static final Logger logger = LoggerFactory.getLogger(DifsFactory.class);

    protected SymbolicLinkStrategy symbolicLinkStrategy;

    protected Path repoRootPath;
    protected Path configFileRelPath;
    protected Path storeRelPath;
    protected Path indexRelPath;
    protected boolean createIfNotExists;

    /** Disable parallel streams for debugging purposes */
    protected boolean isParallel = true;

    protected StoreDefinition storeDefinition;

    protected long maximumNamedGraphCacheSize = -1; // -1 = unlimited

    protected boolean useJournal = true;

    protected Random random = new Random();

    public DifsFactory() {
        super();
    }

    public static DifsFactory newInstance() {
        DifsFactory result = new DifsFactory();
        return result;
    }

    public boolean isCreateIfNotExists() {
        return createIfNotExists;
    }

    /** Whether to use parallel streams. True by default. */
    public DifsFactory setParallel(boolean isParallel) {
        this.isParallel = isParallel;
        return this;
    }


    public DifsFactory setCreateIfNotExists(boolean createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
        return this;
    }

    public DifsFactory setMaximumNamedGraphCacheSize(long size) {
        this.maximumNamedGraphCacheSize = size;
        return this;
    }


    public static Stream<Resource> listResources(Model model, Collection<Property> properties) {
        return properties.stream()
            .flatMap(p ->
                Streams.stream(model.listResourcesWithProperty(p)));
    }

    public StoreDefinition loadStoreDefinition(Path confFilePath) throws IOException {// String filenameOrIri) {
        // TODO Handle local files vs urls
        /// Path confFilePath = Paths.get(filenameOrIri);
        // repoRootPath = confFilePath.getParent().toAbsolutePath();

        String filenameOrIri = confFilePath.getFileName().toString();
        Lang lang = RDFDataMgr.determineLang(filenameOrIri, null, null);
        Model model = ModelFactory.createDefaultModel();
        try (InputStream in = Files.newInputStream(confFilePath)) {
             RDFDataMgr.read(model, in, lang);
        }
        List<Property> mainProperties = Arrays.asList(
                DIFS.storePath,
                DIFS.indexPath,
                DIFS.index,
                DIFS.heartbeatInterval);

        Set<Resource> resources = listResources(model, mainProperties).collect(Collectors.toSet());

        StoreDefinition result;

        if (resources.isEmpty()) {
            // Log a warning?
            logger.info("No config resources found in " + filenameOrIri);
            result = null;
        } else if (resources.size() == 1) {
            result = resources.iterator().next().as(StoreDefinition.class);
        } else {
            throw new RuntimeException("Multiple configurations detected");
        }

        return result;
    }

//	public DifsFactory loadFrom(StoreDefinition storeDef) {
//		if (storeDef.getStorePath() != null) {
//			storeRelPath = Paths.get(storeDef.getStorePath());
//		}
//
//		if (storeDef.getIndexPath() != null) {
//			indexRelPath = Paths.get(storeDef.getIndexPath());
//		}
//
//		for (IndexDefinition idxDef : storeDef.getIndexDefinition()) {
//			loadIndexDefinition(idxDef);
//		}
//
//		return this;
//	}

    public DatasetGraphIndexerFromFileSystem loadIndexDefinition(IndexDefinition idxDef) {
        try {
            Node p = idxDef.getPredicate();
            String folderName = idxDef.getPath();
            String className = idxDef.getMethod();
            Class<?> clz = Class.forName(className);
            Object obj = clz.getDeclaredConstructor().newInstance();
            RdfTermIndexerFactory indexer = (RdfTermIndexerFactory)obj;
            DatasetGraphIndexerFromFileSystem result = addIndex(p, folderName, indexer.getMapper());
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DifsFactory setUseJournal(boolean useJournal) {
        this.useJournal = useJournal;
        return this;
    }

    public boolean isUseJournal() {
        return useJournal;
    }

    public DifsFactory setRepoRootPath(Path repoRootPath) {
        this.repoRootPath = repoRootPath;
        return this;
    }

    public Path getRepoRootPath() {
        return repoRootPath;
    }


    public DifsFactory setConfigFile(Path configFile) {
        this.configFileRelPath = configFile;
        return this;
    }

    public Path getConfigFile() {
        return configFileRelPath;
    }

    public StoreDefinition getStoreDefinition() {
        return storeDefinition;
    }

    public DifsFactory setStoreDefinition(StoreDefinition storeDefinition) {
        this.storeDefinition = storeDefinition;
        return this;
    }

    /**
     * Create a fresh store definition resource (blank node) and pass it to the mutator
     *
     * @param mutator
     * @return
     */
    public DifsFactory setStoreDefinition(Consumer<StoreDefinition> mutator) {
        this.storeDefinition = ModelFactory.createDefaultModel().createResource().as(StoreDefinition.class);
        mutator.accept(this.storeDefinition);
        return this;
    }


    public DatasetGraphIndexerFromFileSystem addIndex(Node predicate, String name, Function<Node, String[]> objectToPath) throws IOException {
//        raw, DCTerms.identifier.asNode(),
//        path = Paths.get("/tmp/graphtest/index/by-id"),
//        DatasetGraphIndexerFromFileSystem::mavenStringToToPath

        Path repoRootPath = this.repoRootPath == null
                ? configFileRelPath.getParent()
                : this.repoRootPath;

        Path indexFolder = repoRootPath.resolve("index").resolve(name);
        // Files.createDirectories(indexFolder);

        ResourceRepository<String> resStore = ResourceRepoImpl.createWithUriToPath(repoRootPath.resolve("store"));

        Objects.requireNonNull(symbolicLinkStrategy, "Symbolic link strategy not set");
        DatasetGraphIndexerFromFileSystem result = new DatasetGraphIndexerFromFileSystem(
                symbolicLinkStrategy,
                resStore,
                predicate,
                indexFolder,
                objectToPath);

        return result;
    }



    public SymbolicLinkStrategy getSymbolicLinkStrategy() {
        return symbolicLinkStrategy;
    }

    public DifsFactory setSymbolicLinkStrategy(SymbolicLinkStrategy symlinkStrategy) {
        this.symbolicLinkStrategy = symlinkStrategy;
        return this;
    }

    protected Path getConfigFilePath() {
        Path result;
        if (repoRootPath == null) {
            result = configFileRelPath;
        } else {
            if (configFileRelPath == null) {
                result = repoRootPath.resolve("store.conf.ttl");
            } else {
                result = repoRootPath.resolve(configFileRelPath);
            }
        }
        return result;
    }

    public StoreDefinition createEffectiveStoreDefinition() throws IOException {

        StoreDefinition effStoreDef;

        Path configFile = getConfigFile();

        if (configFile == null) {
            if (storeDefinition == null) {
                throw new RuntimeException("Neither config file nor store definition provided");
            } else {
                effStoreDef = storeDefinition;
            }
        } else if (!Files.exists(configFile)) {
            if (createIfNotExists) {
                if (storeDefinition == null) {
                    throw new RuntimeException(
                            String.format("Config file %s does not exist and no default config was specified", configFile));
                }

                logger.info(String.format("Creating new config file %s", configFile));
                try (OutputStream out = Files.newOutputStream(configFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    RDFDataMgr.write(out, storeDefinition.getModel(), RDFFormat.TURTLE_PRETTY);
                }

                effStoreDef = storeDefinition;
            } else {
                throw new RuntimeException(String.format("Config file %s does not exist and auto-creation is disabled", configFile));
            }
        } else {
//			if (Files.isDirectory(configFile)) {
//				configFile = configFile.resolve("store.conf.ttl");
//			}

            // Read the config
            effStoreDef = loadStoreDefinition(configFile);
        }

        return effStoreDef;
    }

    public TxnMgr createTxnMgr() throws IOException {
        StoreDefinition effStoreDef = createEffectiveStoreDefinition();

        return createTxnMgr(effStoreDef);
    }

    public TxnMgr createTxnMgr(StoreDefinition effStoreDef) throws IOException {
        // If the repo does not yet exist then run init which
        // creates default conf files

        long heartbeatIntervalMs = Optional.ofNullable(effStoreDef.getHeartbeatInterval()).orElse(5000l);
        TemporalAmount heartbeatInterval = Duration.ofMillis(heartbeatIntervalMs);

        boolean isSingleFileMode = Boolean.TRUE.equals(effStoreDef.isSingleFile());

        Path repoRootPath = this.repoRootPath != null
                ? this.repoRootPath
                : getConfigFile().getParent();

        if (createIfNotExists) {
            Files.createDirectories(repoRootPath);
        }

        Path storeRelPath = Optional.ofNullable(effStoreDef.getStorePath()).map(Path::of).orElse(Path.of(""));
        Path indexRelPath = Optional.ofNullable(effStoreDef.getIndexPath()).map(Path::of).orElse(Path.of("index"));

        Path storeAbsPath = repoRootPath.resolve(storeRelPath);
        Path indexAbsPath = repoRootPath.resolve(indexRelPath);

        // Set up indexers

        Path txnStore = repoRootPath.resolve("txns");

        LockManager<Path> processLockMgr = new LockManagerPath(repoRootPath);
        LockManager<Path> threadLockMgr = new ThreadLockManager<>();

        LockManager<Path> lockMgr = new LockManagerCompound<>(Arrays.asList(processLockMgr, threadLockMgr));

        ResourceRepository<String> resStore;
        PathMatcher pathMatcher;

        if (isSingleFileMode) {
            storeAbsPath = storeAbsPath.getParent();
            String fileName = storeAbsPath.getFileName().toString();

            resStore = new ResourceRepoImpl(storeAbsPath, iri -> new String[] {});

            // FIXME Interpret fileName as a literal!
            pathMatcher = repoRootPath.getFileSystem().getPathMatcher("glob:./" + fileName);
        } else {
            resStore = ResourceRepoImpl.createWithUriToPath(storeAbsPath);
            pathMatcher = repoRootPath.getFileSystem().getPathMatcher("glob:**/*.trig");
        }


        ResourceRepository<String> resLocks = ResourceRepoImpl.createWithUrlEncode(repoRootPath.resolve("locks"));

        SymbolicLinkStrategy effSymlinkStrategy = symbolicLinkStrategy != null ? symbolicLinkStrategy : new SymbolicLinkStrategyStandard();

        String hostname = Hostname.getHostname();
        int pid = ProcessUtils.getPid(-1);
        String txnMgrId = hostname + "-" + pid + "-" + random.nextInt();

        logger.info("Creating txn manager with id: " + txnMgrId);


        TxnMgr result = new TxnMgrImpl(txnMgrId, repoRootPath, pathMatcher, heartbeatInterval, lockMgr, txnStore, resStore, resLocks, effSymlinkStrategy);

        return result;
    }

    public Dataset connectAsDataset() throws IOException {
        DatasetGraph dg = connect();
        return DatasetFactory.wrap(dg);
    }

    public DatasetGraph connect() throws IOException {
        StoreDefinition effStoreDef = createEffectiveStoreDefinition();

        boolean allowEmptyGraphs = Optional.ofNullable(effStoreDef.isAllowEmptyGraphs()).orElse(false);

        TxnMgr txnMgr = createTxnMgr(effStoreDef);

        Collection<DatasetGraphIndexPlugin> indexers = effStoreDef.getIndexDefinition().stream()
            .map(this::loadIndexDefinition)
            .collect(Collectors.toList());

        CacheBuilder<?, ?> namedGraphCacheBuilder = CacheBuilder.newBuilder();
        if (maximumNamedGraphCacheSize > 0) {
            namedGraphCacheBuilder.maximumSize(maximumNamedGraphCacheSize);
        }

        // In single file mode the resource store is the dataset file
        boolean isSingleFileMode = Boolean.TRUE.equals(effStoreDef.isSingleFile());

        String dataFileName = isSingleFileMode
                ? Path.of(storeDefinition.getStorePath()).getFileName().toString()
                : "data.trig";

        DatasetGraphFromTxnMgr result = new DatasetGraphFromTxnMgr(
                dataFileName,
                useJournal, txnMgr, allowEmptyGraphs, isParallel, indexers, namedGraphCacheBuilder);

        // Check for stale transactions
        result.cleanupStaleTxns();

        logger.info("Done checking existing txns");

        // TODO Read configuration file if it exists
        // return DatasetGraphFromFileSystem.create(repoRootPath, lockMgr);

        return result;
    }

}
