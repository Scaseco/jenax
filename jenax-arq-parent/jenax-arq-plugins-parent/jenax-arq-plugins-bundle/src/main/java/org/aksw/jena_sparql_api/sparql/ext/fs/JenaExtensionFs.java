package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.aksw.jenax.arq.functionbinder.FunctionGenerator;
import org.aksw.jenax.arq.service.vfs.ServiceExecutorFactoryRegistratorVfs;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionFs {
    public static final String ns = NorseTermsFs.NS;
    public static final String nsLegacy = "http://jsa.aksw.org/fn/fs/";

    public static void register() {
        ServiceExecutorFactoryRegistratorVfs.register(ARQ.getContext());

        FunctionRegistry fnReg = FunctionRegistry.get();

        registerFunctions(fnReg, ns);
        registerFunctions(fnReg, nsLegacy);

        PropertyFunctionRegistry pfnReg = PropertyFunctionRegistry.get();
        registerPropertyFunctions(pfnReg, ns);
        registerPropertyFunctions(pfnReg, nsLegacy);

        //FunctionRegistry.get().put(ns + "lastModifiedTime", E_UnaryPathFunction.newFactory(path -> NodeValue.makeInteger(Files.getLastModifiedTime(path).toInstant())));

//        FunctionRegistry.get().put(ns + "sha256", E_UnaryPathFunction.newFactory(path ->
//            NodeValue.makeString(
//                com.google.common.io.Files
//                    .asByteSource(path.toFile())
//                    .hash(Hashing.sha256())
//                    .toString())));
//
//        FunctionRegistry.get().put(ns + "md5", E_UnaryPathFunction.newFactory(path ->
//        NodeValue.makeString(
//            com.google.common.io.Files
//                .asByteSource(path.toFile())
//                .hash(Hashing.md5())
//                .toString())));

        FunctionBinder binder = FunctionBinders.getDefaultFunctionBinder();
        FunctionGenerator generator = binder.getFunctionGenerator();

        // Define two-way Geometry - GeometryWrapper coercions
        generator.getConverterRegistry()
            .register(Path.class, Node.class,
                file -> NodeFactory.createURI("file://" + file.toAbsolutePath().toString()),
                node -> {
                    Path r;
                    String str = node.getURI();
                    try {
                        URI uri = new URI(str);
                        r = Path.of(uri);
                    } catch (Exception e) {
                        r = Path.of(str);
                    }
                    return r;
                });
        generator.getJavaToRdfTypeMap().put(Path.class, Node.class);

        binder.registerAll(JenaExtensionFs.class);
    }

    public static HashCode hashPath(Path path, HashFunction hashFunction) throws IOException {
        Hasher hasher = hashFunction.newHasher();
        HashCode result;
        try (OutputStream funnel = Funnels.asOutputStream(hasher)) {
            Files.copy(path, funnel);
            result = hasher.hash();
        }

        return result;
    }


    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("fs", ns);
    }

    // Better not register the handler automatically; it is a quite intrusive deed
    public static void registerFileServiceHandler() {

        throw new RuntimeException("Global registration of file service handler was removed");
        /*
        QC.setFactory(ARQ.getContext(), execCxt -> {
            execCxt.getContext().set(ARQ.stageGenerator, StageBuilder.executeInline);

            // OpExecutorWithCustomServiceExecutors result = new OpExecutorWithCustomServiceExecutors(execCxt);
            ServiceExecutorFactoryRegistratorVfs.register(execCxt.getContext());

            return result;
            // ServiceExecutorFactoryRegistratorVfs.
            // return new OpExecutorServiceOrFile(execCxt);
        });
        */
    }

    public static void registerFunctions(FunctionRegistry fnReg, String ns) {
        fnReg.put(ns + "rdfLang", E_RdfLang.class);
        fnReg.put(ns + "probeRdf", E_ProbeRdf.class);
        fnReg.put(ns + "get", E_PathGet.class);
        fnReg.put(ns + "size", E_UnaryPathFunction.newFactory(path -> NodeValue.makeInteger(Files.size(path))));
        fnReg.put(ns + "isDirectory", E_UnaryPathFunction.newFactory(path -> NodeValue.makeBoolean(Files.isDirectory(path))));
        fnReg.put(ns + "isRegularFile", E_UnaryPathFunction.newFactory(path -> NodeValue.makeBoolean(Files.isRegularFile(path))));

        fnReg.put(ns + "probeContentType", E_UnaryPathFunction.newFactory(path -> NodeValue.makeString(Files.probeContentType(path))));
        fnReg.put(ns + "probeEncoding", E_UnaryPathFunction.newFactory(path -> NodeValue.makeString(probeEncoding.doProbeEncoding(path))));
    }

    public static void registerPropertyFunctions(PropertyFunctionRegistry pfnReg, String ns) {
        pfnReg.put(ns + "find", new PropertyFunctionFactoryFsFind(PropertyFunctionFactoryFsFind::find));
        pfnReg.put(ns + "parents", new PropertyFunctionFactoryFsFind(PropertyFunctionFactoryFsFind::parents));
    }

    @IriNs(ns)
    @IriNs(nsLegacy)
    public static String md5(Path path) throws IOException {
        return hashPath(path, Hashing.md5()).toString();
    }

    @IriNs(ns)
    @IriNs(value=nsLegacy, deprecated=true)
    public static String sha1(Path path) throws IOException {
        return hashPath(path, Hashing.sha1()).toString();
    }

    @IriNs(ns)
    @IriNs(value=nsLegacy, deprecated=true)
    public static String sha256(Path path) throws IOException {
        return hashPath(path, Hashing.sha256()).toString();
    }

    @IriNs(ns)
    @IriNs(value=nsLegacy, deprecated=true)
    public static String sha384(Path path) throws IOException {
        return hashPath(path, Hashing.sha384()).toString();
    }

    @IriNs(ns)
    @IriNs(value=nsLegacy, deprecated=true)
    public static String sha512(Path path) throws IOException {
        return hashPath(path, Hashing.sha512()).toString();
    }
}
