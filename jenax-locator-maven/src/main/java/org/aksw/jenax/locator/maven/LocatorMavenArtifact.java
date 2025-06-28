package org.aksw.jenax.locator.maven;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.streammgr.Locator;
import org.apache.jena.riot.system.streammgr.StreamManager;
import org.apache.jena.sys.JenaSystem;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.maveniverse.maven.mima.context.Context;
import eu.maveniverse.maven.mima.context.ContextOverrides;
import eu.maveniverse.maven.mima.context.Runtimes;

/**
 * Apache Jena Locator implementation based on the <a href="https://github.com/maveniverse/mima">MIni MAven (MIMA) Library</a>
 * (reads settings.xml, activates profiles, mirrors, proxies, auth, etc.)
 */
public class LocatorMavenArtifact implements Locator {

    private static final LocatorMavenArtifact INSTANCE = new LocatorMavenArtifact();

    public static LocatorMavenArtifact get() {
        return INSTANCE;
    }

    private static final Logger logger = LoggerFactory.getLogger(LocatorMavenArtifact.class);

    private LocatorMavenArtifact() {
    }

    private Context createContext() {
        Context context;
        // Create MIMA standalone context (loads settings.xml, profiles, proxies, mirrors, etc.)
        try {
            // Use the static runtime variant
            var runtime = Runtimes.INSTANCE.getRuntime();

            // Optional: customize overrides (e.g., force specific settings file, offline mode, etc.)
            var overrides = ContextOverrides.create()
                .withUserSettings(true)           // explicitly enable ~/.m2/settings.xml
                // .withUserSettingsFile(customFile)  // if you want non-default location
                // .withOffline(true)                // force offline if needed
                .build();

            context = runtime.create(overrides);
            logger.debug("MIMA context initialized - local repo: {}",
                context.repositorySystemSession().getLocalRepository().getBasedir());

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize MIMA standalone context", e);
        }
        return context;
    }

    private Artifact parseMvnUrn(String urn) {
        if (!urn.startsWith("urn:mvn:")) {
            throw new IllegalArgumentException("URN must start with 'urn:mvn:': " + urn);
        }
        String coords = urn.substring("urn:mvn:".length());
        String[] parts = coords.split(":", -1);

        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid Maven coordinate");
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        String extension = (parts.length > 3 && !parts[3].isEmpty()) ? parts[3] : "jar";
        String classifier = (parts.length > 4 && !parts[4].isEmpty()) ? parts[4] : null;

        return new DefaultArtifact(groupId, artifactId, classifier, extension, version);
    }

    private File resolveArtifactFile(Context context, Artifact artifact) {
        return resolveArtifactFile(context.repositorySystem(), context.repositorySystemSession(), artifact);
    }

    private static File resolveArtifactFile(
            RepositorySystem repoSystem,
            RepositorySystemSession session,
            Artifact artifact) {

        ArtifactRequest request = new ArtifactRequest().setArtifact(artifact);

        // Try local resolution first (no remote repositories)
        try {
            ArtifactResult result = repoSystem.resolveArtifact(session, request);
            File file = result.getArtifact().getFile();
            if (file != null && file.exists() && file.isFile()) {
                logger.debug("Found locally: {}", artifact);
                return file;
            }
        } catch (ArtifactResolutionException e) {
            logger.debug("Local resolution failed for {} → trying remote", artifact, e);
        }

        // Remote resolution - session already contains effective remote repositories
        // -> no need to set request.setRepositories() (unless we want to override)
        try {
            ArtifactResult result = repoSystem.resolveArtifact(session, request);
            File file = result.getArtifact().getFile();
            if (file != null && file.exists() && file.isFile()) {
                logger.debug("Resolved/downloaded: {}", artifact);
                return file;
            }
        } catch (ArtifactResolutionException e) {
            logger.debug("Resolution failed for {}: {}", artifact, e.getMessage());
        }

        return null;
    }
    @Override
    public TypedInputStream open(String uri) {
        Artifact artifact;
        try {
            artifact = parseMvnUrn(uri);
        } catch (IllegalArgumentException e) {
            return null;
        }

        File file;
        try (Context context = createContext()) {
            file = resolveArtifactFile(context, artifact);
        }

        if (file == null || !file.exists()) {
            return null;
        }

        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            ContentType contentType = null;
            Lang lang = RDFLanguages.filenameToLang(file.getName());
            if (lang != null) {
                contentType = lang.getContentType();
            }
            return new TypedInputStream(is, contentType, uri);
        } catch (FileNotFoundException e) {
            logger.debug("File not found after resolution: {}", uri, e);
            return null;
        }
    }

    @Override
    public String getName() {
        return "LocatorMavenEmbedder";
    }

    // Test main
    public static void main(String[] args) throws Exception {
        // LocatorMavenArtifact locator = new LocatorMavenArtifact();

        JenaSystem.init();
        StreamManager mgr = StreamManager.get();
        {
            // String uri = "urn:mvn:org.apache.jena:jena-core:6.0.0";
            String uri = "urn:mvn:org.aksw.data.text2sparql.2025:dbpedia:1.0.0:nt:dbpedia_2015-10";
            // System.out.println("Exists? " + (mgr.open(uri) != null));

            try (TypedInputStream tis = mgr.open(uri)) {
                if (tis != null) {
                    System.out.println("Opened: " + tis.available() + " bytes");
                }
            }
        }

        {
            String uri = "urn:mvn:org.apache.jena:jena-arq:6.0.0";
            // System.out.println("Exists? " + (locator.open(uri) != null));

            try (TypedInputStream tis = mgr.open(uri)) {
                if (tis != null) {
                    System.out.println("Opened: " + tis.available() + " bytes");
                }
            }
        }

    }
}
