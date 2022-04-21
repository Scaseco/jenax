# FsRdfStore
Transactional File-based Respository and SPARQL Engine System. Implementated using Jena's DatasetGraph interface.


## TL;DR: Creating a Dataset Instance

```java

// The file that holds the store definition
// Will be created from the definition below if it does not exist
Path configFile = Paths.get("/tmp/store.conf.ttl");

// Create a definition for the store; adapt to your needs
StoreDefinition sd = ModelFactory.createDefaultModel().createResource().as(StoreDefinition.class)
		.setStorePath("store") // Optional
		.setIndexPath("index") // Optional
		.addIndex("http://dataid.dbpedia.org/ns/core#group", "group", RdfTermIndexerFactoryIriToFolder.class)
		.addIndex("http://purl.org/dc/terms/hasVersion", "version", RdfIndexerFactoryLexicalForm.class)
		.addIndex(DCAT.downloadURL.asNode(), "downloadUrl", RdfTermIndexerFactoryIriToFolder.class)
		.addIndex(DCTerms.identifier.asNode(), "identifier", RdfIndexerFactoryLexicalForm.class);

// Create the Dataset; if the configFile
DatasetGraph dg = DifsFactory.newInstance()
		.setStoreDefinition(sd)
		.setUseJournal(useJournal) // false for read only access; true for write access
		.setSymbolicLinkStrategy(SymbolicLinkStrategies.FILE)
		.setConfigFile(basePath)
		.connect();
Dataset d = DatasetFactory.wrap(dg);
```

## Motivation
This work is heavily inspired by Maven's approach to artifact management: Each artifact is addressed by a composite key - called a coordinate - with the essential components being group id, artifact id and version. A simple mapping of Maven coordinates to relative URIs together with a base URL is all that is needed to form an absolute URL from where the artifact's resources can be accessed. In fact, deployment of artifacts is typcially mere WebDAV interactions based on absolute URLs derived from the coordinates.

The main complexity of Semantic Web data is that one has to constantly deal with multiple IDs for the same thing - such as the IRI of a resource itself, its dct:identifier, and possibly those of resources reachable via owl:sameAs links.

In order to overcome these complexities this work introduces:
* A file-based repository system where IRIs are resolved to paths in the repository similar to Maven's GAV.
* File-based indexes to support efficient lookup of data by any alternative identifier
* A read+write SPARQL interface to the repository

Because of the file-based nature, the whole store can be put under version control using e.g. GIT or SVN - which in addition allows for simple replication.
Files can also be easily exposed with WebDAV. Because of the used virtual file system technology, remote and local querying and updates are possible.


## Features of FsRdfStore

* Java-like IRI-to-path mapping (Data for the graph http://example.org/foobar is written to the folder ./org/example/foobar)
* Virtual File System based (using Java NIO adapter to Apache Commons VFS2)
* Transaction Support with isolation levels 'read uncommitted' for unsafe read only access (e.g. using WebDAV) and 'serializable' by means of graph-level file locks
* Support for File-system based indexes

As such FsRdfStore is not tied to GIT; in fact, FsRdfStore is not tied to version control at all - the files that it writes may be under version control

### How does FsRdfStore store differ from QuitStore?
The fundamental concept of FsRdfStore to enable version control of RDF data by means of partitioning each named graph into a separate file
is conceptually similar to QuitStore. However, Quitstore as of 2021-04-14 does not provide any other of FsRdfStore's feattures listed above.

## Modules

* difs-core: The transactional DatasetGraph implementation with file system based indexing support via symbolic links
* difs-system: A system built on core that adds RDF-based configuration infrastructure.
* difs-cli: Command line tooling. E.g. start a fuseki with that dataset or reindex existing data.

* TODO Naming: FsRdfStore is not a nice name, maybe something along the lines of dataset in files system (difs), or fbd or fsbd... let's use difs for now.

## Differences to conventional triple stores

* One file for one or more named graphs
* File location depends on the graph URI and follows Java conventions.
* Files can be any RDF format (text or binary). The formor allows for use of firebird with version control systems such as GIT or SVN
  * In this case replication of a firebird store is a simple as a `git pull` or `svn update`.
* The files and folders can be exposed using WebDav allowing clients to perform queries to a remote firebird store
* Main use case is efficient and flexibile lookup of RDF metadata using identifiers and property values



## WebDAV Access

### Java code to setup a Virtual FileSystem

The basic Java code to treat to treat the URL `webdav://localhost/my/webdav/folder` as a virtual folder is shown below:
```
public static void main(String[] args) {
    String vfsUri = "webdav://localhost";

    FileSystem fs = FileSystems.newFileSystem(
        URI.create("vfs:" + vfsUri), 
        new HashMap<String, Object>());
		
    Path basePath = fs.getRootDirectories().iterator().next()
        .resolve("my").resolve("webdav").resolve("folder");

```

Unfortunately, because the VFS2 WebDAV client uses the legacy version 3 of apache http client HTTP redirects are not supported.
One may be inclined to attempt to enable redirects, but unfortunately the the code below raises an exception about redirects not being allowed.
The workaround is to configure the Web server to avoid redirects (see the section on WebDav publishing).

```
    String vfsUri = "webdav://localhost";

    FileSystemOptions webDavFsOpts = new FileSystemOptions();
    // The following option is correctly recognized but not permitted
    WebdavFileSystemConfigBuilder.getInstance().setFollowRedirect(webDavFsOpts, true);

    Map<String, Object> env = new HashMap<>();
    env.put(Vfs2NioFileSystemProvider.FILE_SYSTEM_OPTIONS, webDavFsOpts);

    FileSystem fs = FileSystems.newFileSystem(
        URI.create("vfs:" + vfsUri),
        env);
```

### Publishing a folder via WebDAV using Apache

This example assumes the folder to be exposed as webdav exists and is located at `/var/www/webdav`.

Because of legacy issues the Apache Virtual File System VFS2 cannot handle redirects on WebDav requests.
For this reason, the DirectorySlash must be turned off, such that `webdav://host/foo` does not get redirectod to `webdav://host/foo/`.

```
echo "DirectorySlash Off" > /var/www/webdav/.htaccess
sudo chown www-data:www-data /var/www/webdav/.htaccess
```


```
# /etc/apache2/site-available/000-default.conf

        DavLockDB /usr/local/apache2/var/DavLock
        Alias /webdav "/var/www/webdav"
        <Directory "/var/www/webdav">
                AllowOverride all
                DAV on
        </Directory>
```



## Storage Layout for Data, Locks, Transactions and Indexes

```
./store
./index
./locks
./txns
firebird.conf.ttl
```

* Transactions link to the accessed resources - such as by symlinking to the container in ./store
* Lock files in flatened container folders under ./locks link to the txns that own them

```
# Transactions link to the store folder - which allows obtaining the container name by relativizing the link target against the store base path
containerName = iriToPath(iri)
./store/foo/bar/data.trig
./txns/txn-123/.${lockRepo.getPath(containerName)} -> ../../store/foo/bar/data.trig

# Locks link back to the transaction that holds them
./locks/abcde/txn-123.read.lock -> ../txn/txn-123
```


