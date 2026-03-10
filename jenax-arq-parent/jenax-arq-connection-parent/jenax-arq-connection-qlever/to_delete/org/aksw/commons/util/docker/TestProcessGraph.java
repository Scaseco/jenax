package org.aksw.commons.util.docker;

//public class TestProcessGraph {
//    public void test01() {
//
//
//        // TODO: How to set up a stderr pipe with a logger attached?
//
//        ProcessGraph processGraph = new ProcessGraphImpl();
//
//        Pipe pipe = processGraph.newPipe();
//
//        Pipe file = processGraph.newPath(Path.of("/tmp/foo"));
//
//        ProcessNode p1 = processGraph.newProcessNode(() -> new ProcessBuilder("cat"));
//        ProcessNode p2 = processGraph.newProcessNode(() -> new ProcessBuilder("cat"));
//
//        p2.in().setSource(p1.out());
//        p1.out().setSink(p2.in());
//
//        processGraph.connect(p1.out(), p2.in());
//
//        ProcessNode p3 = processGraph.newProcessNode(() -> new ProcessBuilder("cat"));
//        p2.out().setSink(pipe.getWriteEnd());
//        // Alternative, pipe-centric api usage for the statement above:
//        pipe.getWriteEnd().setSource(p3.out());
//
//        p3.in().setSource(pipe.getReadEnd());
//
//        p1.in().setSource(file.getReadEnd());
//
//        // Q1: Can we use jgrapht internally - either directly or as a transformation step? mainly in order to reuse topological ordering.
//
//
//
////        GraphComponent
////            .of(graph)
////            .stdin(p1.stdin())
////            .stdout(p2.stdou())
////            .stderr(p3.getStderr());
////
////        graph.newInstance(component);
//
//
//        // Start all processes in the graph
//
//        // Issue: How to declare pipes on the graph?
//        // Execution is o
//
//        // processGraph.exec();
//    }
//
//    public void test02() {
//        // { cat pipe | tee pipe ; } < pipe
//
//        ProcessGraph graph = new ProcessGraphImpl();
//        Object catStmt = null;
//        // ProcessNode cat = graph.newProcessNode(() -> new ProcessBuilder("cat"));
//
//        GroupNode group = new GroupNodeImpl();
//        ProcessNode cat1 = group.add(catStmt);
//        ProcessNode cat2 = group.add(catStmt);
//
//        // Is there a need for an explicit pipeline?
//        // We can just wire up the output of one process with the input of another.
//        // But we need a declaration of which units to use for in out err.
//        // So pipeline becomes a mere helper for p1.out->p2.in, p2.out->p3.in...
//        //  and all p(i).err (except for the last) are wired to the group's stderr.
//
//
//        //ProcessBuilder x; x.redirectInput()
//        // group.redirectInput(Redirect.otherProcess(someProcess));
//
//
//        // executor.exec(group);
//
//        // ProcessGraph graph = new ProcessGraphImpl();
//
//        // Pipe pipe = graph.newPipe();
//
//        // graph.newProcessNode(ProcessBuilder.)
//
//        // Pipe file = processGraph.newPath(Path.of("/tmp/foo"));
//
//
//    }
//}
