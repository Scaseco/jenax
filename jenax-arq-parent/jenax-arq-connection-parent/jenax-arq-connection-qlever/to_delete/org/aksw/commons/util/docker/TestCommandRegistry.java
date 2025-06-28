package org.aksw.commons.util.docker;

import java.io.IOException;

import org.junit.Test;

import org.aksw.shellgebra.exec.SysRuntimeCore;
import org.aksw.shellgebra.exec.SysRuntimeCoreExecSiteFactory;
import org.aksw.shellgebra.exec.SysRuntimeCoreExecSiteFactoryPool;
import org.aksw.shellgebra.exec.SysRuntimeFactoryDocker;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.shim.cmd.JvmCommandCat;
import org.aksw.shellgebra.shim.cmd.JvmCommandEcho;
import org.aksw.shellgebra.shim.cmd.JvmCommandHead;
import org.aksw.shellgebra.shim.cmd.JvmCommandTranscode;
import org.aksw.shellgebra.shim.cmd.JvmCommandWhich;
import org.aksw.shellgebra.shim.core.JvmCommand;
import org.aksw.vshell.registry.CommandRegistry;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import junit.framework.Assert;

public class TestCommandRegistry {
//    @Test
//    public void test01() throws IOException {
//        String testcontainers_retryCount = System.getProperty("testcontainers.retryCount");
//        if (testcontainers_retryCount == null) {
//            System.setProperty("testcontainers.retryCount", "1");
//        }
//
//        JvmCommandRegistry jvmCmdRegistry = initJvmCmdRegistry(new JvmCommandRegistry());
//        CommandRegistry candidates = initCmdCandRegistry(new CommandRegistry());
//
//        CommandRegistry inferredCatalog = new CommandRegistry();
//        CommandCatalog hostCatalog = new CommandCatalogOverLocator(ExecSiteCurrentHost.get(), new CommandLocatorHost());
//        CommandCatalog jvmCatalog = new CommandCatalogOverLocator(ExecSites.jvm(), new CommandLocatorJvmRegistry(jvmCmdRegistry));
//
//        CommandCatalog unionCatalog = new CommandCatalogUnion(List.of(candidates, hostCatalog, jvmCatalog, inferredCatalog));
//
//        ExecSiteProbeResults cmdAvailability = new ExecSiteProbeResults();
//        // TODO Have image introspector write into cmdAvailability without having to know about exec sites.
//        // Need an adapter or cmdAvailability.asDockerImageMap().
//
//        Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
//        ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(shellModel, cmdAvailability);
//        imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);
//
//        ExecSiteResolver resolver = new ExecSiteResolver(candidates, jvmCmdRegistry,
//            cmdAvailability, imageIntrospector);
//
//        // Some command expression.
//        // "echo 'test' | lbzip2 -c | bzip2 -cd | cat - <(echo done)"
//        System.out.println(resolver.resolve("/virt/lbzip2"));
//        CmdOpExec cmdOp1 = CmdOpExec.ofLiterals("/virt/lbzip2", "-c");
//        CmdOp cmdOp2 = CmdOpGroup.of(
//            CmdOpExec.ofLiterals("/virt/bzip2", "-dc"),
//            new CmdOpExec(List.<CmdPrefix>of(), "/usr/bin/cat", ArgumentList.of(CmdArg.ofLiteral("-"), CmdArg.ofProcessSubstution(CmdOpExec.ofLiterals("/usr/bin/echo", "done."))))
//        );
//        // TODO Do not use CmdOpExec.ofLiterals
//        // Instead: use a command registry with shim-parsers so that arguments are validated.
//
//        CmdOp cmdOp = CmdOpPipeline.of(cmdOp1, cmdOp2);
//
//        // Try to resolve the command on a certain docker image.
//        ExecSite qleverExecSite = ExecSites.docker("adfreiburg/qlever:commit-a307781");
//
//        CmdOpVisitorCandidatePlacer commandPlacer = new CmdOpVisitorCandidatePlacer(candidates, inferredCatalog, resolver, Set.of(qleverExecSite));
//        PlacedCommand placedCommand = cmdOp.accept(commandPlacer);
//        CandidatePlacement candidatePlacement = new CandidatePlacement(placedCommand, commandPlacer.getVarToPlacement());
//
//        FinalPlacement placed = FinalPlacer.place(candidatePlacement);
//        System.out.println("Placed: " + placed);
//
//        FinalPlacement inlined = FinalPlacementInliner.inline(placed);
//
//        FinalPlacement resolvedInlined = FinalPlacementResolver.resolve(inlined, resolver, unionCatalog);
//        // Resolve commands w.r.t. the final placement.
//
//        System.out.println("Inlined: " + resolvedInlined);
////        if (true) {
////            return;
////        }
//
//        // pb.redirectError(Redirect.)
//        // Final step: convert to stage (or bound stage?)
//        FileMapper fileMapper = FileMapper.of("/tmp/shared");
//        // TODO PlacedCmdOpToStage should probably accept a resolver as argument!
//        ShellEnv shellEnv = new ShellEnv(); // Perhaps pass a shellEnv for book keeping of streams / file writers?
//
//        Stage stage = PlacedCmdOpToStage.of(fileMapper, resolver).toStage(resolvedInlined);
//
//
//        ByteSource xx = new ByteSource() {
//            @Override
//            public InputStream openStream() throws IOException {
//                InputStream x = new ByteArrayInputStream("hello world".getBytes(StandardCharsets.UTF_8));
//                // x = new InputStreamTransformOverOutputStreamTransform(BZip2CompressorOutputStream::new).apply(x);
//                return x;
//            }
//        };
//
//        String str = stage.from(xx).toByteSource().asCharSource(StandardCharsets.UTF_8).read();
//        System.out.println(str);
//        System.out.println(placedCommand);
//
//        // Issue: For the qlever use case, we don't want a stage.
//        // instead we want: the expression, with allocated file names and file writer tasks.
//
//        // System.out.println(resolver.resolve("/usr/bin/lbzip2"));
//        // CommandRegistry hostRegistry = new CommandRegistryOverLocator(ExecSiteCurrentHost.get(), new CommandLocatorHost());
//        // CommandRegistry jvmRegistry = new CommandRegistryOverLocator(ExecSites.jvm(), new CommandLocatorJvmRegistry(jvmCmdRegistry));
//        // CommandRegistry baseRegistry = new CommandRegistryUnion(List.of(jvmRegistry, candidates, hostRegistry));
//    }

    @Test
    public void test02() throws IOException, InterruptedException {
        JvmCommandRegistry jvmCmdRegistry = initJvmCmdRegistry(new JvmCommandRegistry());
        SysRuntimeFactoryDocker dockerFactory = SysRuntimeFactoryDocker.create();

        String expectedStr = "Hello world";
        String actualStr;
        try (SysRuntimeCoreExecSiteFactory f = new SysRuntimeCoreExecSiteFactoryPool(jvmCmdRegistry, dockerFactory)) {
            try (SysRuntimeCore r = f.getRuntime(ExecSites.docker("nestio/lbzip2"))) {
                actualStr = r.execCmd("echo", expectedStr);
            }
        }
        Assert.assertEquals(expectedStr,actualStr);
    }

//    @Test
//    public void testJvmWhichDirect() throws IOException {
//        JvmCommandRegistry jvmCmdRegistry = initJvmCmdRegistry(new JvmCommandRegistry());
//        JvmContext context = new JvmContext(jvmCmdRegistry);
//        context.getEnvironment().put("PATH", "/jvm:/bin");
//        JvmCommandExecutor executor = new JvmCommandExecutorImpl(context);
//
//        // TODO Use command -v instead of test.
//        String expectedStr = "/jvm/bzip2";
//        String actualStr = executor.exec("which", "bzip2");
//        Assert.assertEquals(expectedStr, actualStr);
//
//        int actualValue = executor.run("test", "-e", actualStr);
//        Assert.assertEquals(0, actualValue);
//    }

//    @Test
//    public void testJvmWhichProcess() throws IOException {
//        JvmCommandRegistry jvmCmdRegistry = initJvmCmdRegistry(new JvmCommandRegistry());
//        JvmContext context = new JvmContext(jvmCmdRegistry);
//        context.getEnvironment().put("PATH", "/jvm:/bin");
//        JvmCommandExecutor executor = new JvmCommandExecutorImpl(context);
//
//        IProcessBuilder<?> builder = executor.newProcessBuilder("which", "bzip2");
//        // SystemUtils
//
////        String expectedStr = "/jvm/bzip2";
////        String actualStr = executor.exec("which", "bzip2");
//        // Assert.assertEquals(expectedStr, actualStr);
//    }

    public void testExecSiteExecutor() throws IOException, InterruptedException {
        JvmCommandRegistry jvmCmdRegistry = initJvmCmdRegistry(new JvmCommandRegistry());
        SysRuntimeFactoryDocker dockerRuntimeFactory = SysRuntimeFactoryDocker.create();
        SysRuntimeCoreExecSiteFactoryPool pool = new SysRuntimeCoreExecSiteFactoryPool(jvmCmdRegistry, dockerRuntimeFactory);
        try (SysRuntimeCore runtime = pool.getRuntime(ExecSites.jvm())) {
            runtime.execCmd("/virt/is-command", "which");
        }
    }

    /** These are the physical jvms. */
    public static JvmCommandRegistry initJvmCmdRegistry(JvmCommandRegistry jvmCmdRegistry) {
        // Core command: which - resolve short name to fully qualified command name.
        jvmCmdRegistry.put("/bin/which", new JvmCommandWhich());

        // Core command: test - return 0 if name is fully qualified command name - 1 otherwise.
        // jvmCmdRegistry.put("/bin/test", new JvmCmdTest());

        jvmCmdRegistry.put("/bin/echo", new JvmCommandEcho());
        jvmCmdRegistry.put("/bin/cat", new JvmCommandCat());
        jvmCmdRegistry.put("/usr/bin/echo", new JvmCommandEcho());
        jvmCmdRegistry.put("/usr/bin/cat", new JvmCommandCat());
        jvmCmdRegistry.put("/bin/head", new JvmCommandHead());

        CompressorStreamFactory csf = new CompressorStreamFactory();

        JvmCommand bzip2Cmd = JvmCommandTranscode.of(csf, CompressorStreamFactory.BZIP2);
        jvmCmdRegistry.put("/jvm/bzip2", bzip2Cmd);
        return jvmCmdRegistry;
    }

    /*
     * XXX Should we indirectly access the command parser via the jvm exec site?!
     *     Or should the jvm parser be linked to the virt command?
     */

    /** These are the candidate mappings. */
    public static CommandRegistry initCmdCandRegistry(CommandRegistry registry) {
        registry.put("/virt/lbzip2", ExecSites.docker("nestio/lbzip2"), "/usr/bin/lbzip2");
        // Note: There can be multiple candidates per exec site.
        registry.put("/virt/lbzip2", ExecSites.host(), "/usr/bin/lbzip2");
        registry.put("/virt/lbzip2", ExecSites.jvm(), "/jvm/bzip2");

        registry.put("/virt/bzip2", ExecSites.jvm(), "/jvm/bzip2");

        registry.put("/virt/echo", ExecSites.host(), "/usr/bin/echo");
        registry.put("/virt/echo", ExecSites.jvm(), "/bin/echo");
        registry.put("/usr/bin/cat", ExecSites.host(), "/usr/bin/cat");

        registry.put("/virt/cat", ExecSites.jvm(), "/bin/cat");
        registry.put("/virt/cat", ExecSites.host(), "/bin/cat");
        return registry;
    }
}
