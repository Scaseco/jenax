package org.aksw.jena_sparql_api.dataset.file;

import org.junit.Test;

public class DatasetGraphFromFileSystemTests {
	
	@Test
	public void test() {
//		Dataset ds1 = TDB2Factory.connectDataset(Location.create(Paths.get("/tmp/tdb2test")));
//		Dataset ds2 = TDB2Factory.connectDataset(Location.create(Paths.get("/tmp/tdb2test")));
//		
//		Txn.executeWrite(ds1, () -> ds1.asDatasetGraph().add(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property));
//		
//		System.out.println("yay");
//		
//		ds1.close();
//		ds2.close();
//		
		
//		ProcessFileLock plock = ProcessFileLock.create("/tmp/test");
//		plock.get
//		Scanner sc= new Scanner(System.in); //System.in is a standard input stream.
//		System.out.print("Enter a string: ");
//		String str= sc.nextLine(); //reads string.
	}
	
//    public static void main(String[] args) throws IOException {
//        Path path = Paths.get("/tmp/graphtest/store");
//        Files.createDirectories(path);
//        DatasetGraphFromFileSystem raw = DatasetGraphFromFileSystem.createDefault(path);
//
//        raw.addPreCommitHook(dgd -> {
//            System.out.println("Added:");
//            RDFDataMgr.write(System.out, DatasetFactory.wrap(dgd.getAdded()), RDFFormat.TRIG_PRETTY);
//            System.out.println("Removed:");
//            RDFDataMgr.write(System.out, DatasetFactory.wrap(dgd.getRemoved()), RDFFormat.TRIG_PRETTY);
//        });
//
//        raw.addIndexPlugin(new DatasetGraphIndexerFromFileSystem(
//                raw, DCTerms.identifier.asNode(),
//                path = Paths.get("/tmp/graphtest/index/by-id"),
//                DatasetGraphIndexerFromFileSystem::mavenStringToToPath
//                ));
//
//        raw.addIndexPlugin(new DatasetGraphIndexerFromFileSystem(
//                raw, DCAT.distribution.asNode(),
//                path = Paths.get("/tmp/graphtest/index/by-distribution"),
//                DatasetGraphIndexerFromFileSystem::uriNodeToPath
//                ));
//
//        raw.addIndexPlugin(new DatasetGraphIndexerFromFileSystem(
//                raw, DCAT.downloadURL.asNode(),
//                path = Paths.get("/tmp/graphtest/index/by-downloadurl"),
//                DatasetGraphIndexerFromFileSystem::uriNodeToPath
//                ));
//
//
//        DatasetGraph dg = raw;
//
////        Node lookupId = RDF.Nodes.type;
//        Node lookupId = NodeFactory.createLiteral("my.test:id:1.0.0");
//        System.out.println("Lookup results for id: ");
//        dg.findNG(null, null, DCTerms.identifier.asNode(), lookupId).forEachRemaining(System.out::println);
//        System.out.println("Done");
//
//
//        // DcatDataset dataset = DcatDatasetCreation.fromDownloadUrl("http://my.down.load/url");
//        Resource dataset = ModelFactory.createDefaultModel().createResource("http://my.down.load/url#dataset");
//        dg.addGraph(dataset.asNode(), dataset.getModel().getGraph());
//
//
////        DatasetGraph dg = new DatasetGraphMonitor(raw, new DatasetChanges() {
////            @Override
////            public void start() {
////                System.out.println("start");
////            }
////
////            @Override
////            public void reset() {
////                System.out.println("reset");
////            }
////
////            @Override
////            public void finish() {
////                System.out.println("finish");
////            }
////
////            @Override
////            public void change(QuadAction qaction, Node g, Node s, Node p, Node o) {
////                System.out.println(Arrays.asList(qaction, g, s, p, o).stream()
////                        .map(Objects::toString).collect(Collectors.joining(", ")));
////            }
////        }, true);
//
//        if (true) {
//            System.out.println("graphnodes:" + Streams.stream(dg.listGraphNodes()).collect(Collectors.toList()));
////            RDFDataMgr.write(System.out, dg, RDFFormat.TRIG_BLOCKS);
//
//    //        Txn.executeWrite(dg, () -> {
//                dg.add(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.type);
//    //        });
//
//            System.out.println("Adding another graph");
//    //        Txn.executeWrite(dg, () -> {
//                dg.add(RDFS.Nodes.label, RDFS.Nodes.label, RDFS.Nodes.label, RDFS.Nodes.label);
//                dg.add(RDFS.Nodes.label, RDFS.Nodes.label, DCTerms.identifier.asNode(), lookupId);
//    //        });
//        }
//
//        Model m = RDFDataMgr.loadModel("/mnt/LinuxData/home/raven/Projects/Eclipse/dcat-suite-parent/dcat-experimental/src/main/resources/dcat-ap-ckan-mapping.ttl");
//        Txn.executeWrite(dg, () -> {
//            DatasetFactory.wrap(dg).addNamedModel(OWL.Class.getURI(), m);
//        });
//
//        DatasetFactory.wrap(dg).getNamedModel(OWL.Class.getURI()).add(RDF.Bag, RDF.type, RDF.Bag);
//        dg.add(OWL.Class.asNode(), RDFS.Nodes.label, DCTerms.identifier.asNode(), lookupId);
//
//        System.out.println("done");
//    }

}
