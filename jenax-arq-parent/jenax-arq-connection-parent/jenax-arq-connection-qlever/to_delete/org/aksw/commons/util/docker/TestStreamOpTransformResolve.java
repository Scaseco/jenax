package org.aksw.commons.util.docker;

//public class TestStreamOpTransformResolve {
//    @Test
//    public void test() {
//        StreamOpTransformResolve resolver = new StreamOpTransformResolve();
//
//        // Targets for compilation:
//        // - a ByteSource (would require intermediate files to be scoped - but input files would remain static)
//        // - a command that when run yields the desired response on stdout.
//
//        StreamOp op =
//            new StreamOpTranscode(TranscodeMode.DECODE, "gz",
//                new StreamOpContentConvert("rdfxml", "nt", null,
//                    new StreamOpTranscode(TranscodeMode.ENCODE, "bzip2", new StreamOpFile("/tmp/foo.rdf.bz2"))));
//
//        // And what if we change to model for unary operators to explicit pipes?
//
//        // pipe(pipe(cat(foo.rdf.bz2), convert(rdfxml, nt, null)), encode(gz))
//        // pipe(convert(rdfxml, nt, null), encode(gz))
//
//
//        // Transformers.of(opSpec1, opSpec2);
//
//
//
//        Resolution1 r1 = resolver.processOpSpec(OpSpecTranscoding.encode("bzip2"));
//        System.out.println(r1);
//
//        Resolution1 r2 = resolver.processOpSpec(OpSpecTranscoding.decode("xz"));
//        System.out.println(r2);
//
//
//        // Cmd + Cmd
//        // Cmd + Java
//        // Java + Cmd
//        // Java + Java
//
//        // Cmd could be executed via: ProcessBuilder or via DockerAPI
//
//
//        if (true) {
//            return;
//        }
//
//        // Reuse range cache. Pipeline.setExpression(expr).setExecutor(executor).checkpointCache(cp).execRange(offset=100000, limit=10);
//
//
//
//        // Pipeline.setInput(someCommand).execInputStream()
//
//        CmdOp cmdOp;
//
//
//
//        StreamOp afterOp = StreamOpTransformer.transform(op, resolver);
//        System.out.println("Resolve transform outcome: " + afterOp);
//
//        // For each node, determine which tools and images could be used.
//        ToolInfoProviderImpl tools = ToolUsage.analyzeUsage(afterOp);
//
//
//        StreamOpVisitorToGraph graphConverter = new StreamOpVisitorToGraph();
//        OpSpecNode rootNode = op.accept(graphConverter);
//        DirectedAcyclicGraph<OpSpecNode, OpSpecEdge> dag = graphConverter.getDag();
//
//        Map<OpSpec, Resolution1> candidateMap = null;
//        doPlacement(dag, rootNode, candidateMap);
//
//
//        // So how to extract all n-ary oppecs from the graph or the expression?
//        // Set<OpSpec> opSpecNodedag.vertexSet().stream().map
//        // StreamOp afterOp = StreamOpTransformer.transform(op, resolveTransform);
//
//        // For resolved commands we probably need to track both the tool name and perhaps even the tool id
//        // For optimization we want to be able to parse the tool command line in order to transform it.
//
//
//        // So the central task is to resolve op specs to the containers they can run on.
//        // Usually this does not depend on the command - but what if there were changes in the tool interface that e.g.
//        // multiple arguments are now supported? But then again
//
//        // dag.outgoingEdgesOf(rootNode).iterator().next().getIndex();
//
//        System.out.println(dag);
//
//        if (true) { return; }
//
//
//
//        // Check if the tools also exist in other images.
//        ToolUsage.enrich(tools, "adfreiburg/qlever:commit-f59763c");
//
//        // Note: Image vs Container: Do we need to explicitly model when to start a new container and when to exec
//        // inside an existing one? I think here we only schedule new containers to start - so image names are sufficient.
//
//
//
//        // Next step: Execution Placement - place operations on execution sites.
//        // Top-down traverse the op and try to map as many tools as possible into a specified target container.
//
//        // The arg builder approach right now only works for stdin/stdout based streams.
//        // Instead of Strings, the arg builder could emit "Arg" objects - there could be an INPUT and OUTPUT token.
//        // This could be substituted later - so its like a variable substitution then.
//        // Under this perspective, ArgBuilder should probably just emit a List<CmdArg>.
//        // CmdArg: String, Variable,  - perhaps even support CmdArgOverCmdOp.
//
////        Tool x = null;
////        x.argsBuilder();
//
//        System.out.println(tools);
//    }
//
//    // So what about concat? If the arguments are in different containers,
//    // then we could still mount them all into the same container.
//    // If one argument is in a different container, then we could still just execute this argument separately
//    // and mount it into the container.
//    // So then we even have operations scheduled to the same image in different execution stages... argh!
//
//    // But still: when processing a concat op:
//    // - if there is already an image with bash involved then all resources could be mounted into that image
//    //   to form a bash group
//    // Now I am confused - could be still put wiring into a separate phase then?
//    // Well, we could map the concat op to the same image as another argument
//    // Or we handle concat completely separate and forget about the mapping to an image and handle this
//    // in wiring exclusively.
//
//    // - alternatively, a new image could be run
//
//    // Map each nodes to the candidate sites
//    // host site / imageSite / javaSite
//    // I think the site already includes the command target (no longer the abstract tool name) - so site is where something
//    // concrete can run.
//
//    // Or we really leave placeholders in commands - which get filled out with the concrete filename eventually.
//    // A placeholder could also be in the stdin slot of a command.
//    // So stdin source could be another OpCmd.
//
//    // So what if we really model tools with connectors?
//
//    public static void doPlacement(DirectedAcyclicGraph<OpSpecNode, OpSpecEdge> dag, OpSpecNode start, Map<OpSpec, Resolution1> resolutionMap) {
//        // The concrete site mapping.
//        Map<OpSpecNode, ExecSite> nodeToSite = new HashMap<>();
//
//        //
//        OpSpec opSpec = start.getOpSpec();
//        List<OpSpecEdge> subNodes = dag.incomingEdgesOf(start).stream().sorted(Comparator.comparingInt(OpSpecEdge::getIndex)).toList();
//
//        resolutionMap.get(opSpec);
//    }
//
//    // Fragment: Connected nodes with the same execution site.
//    // The nodes having
//    public static void createFragements() {
//
//    }
//
//
//
//}
