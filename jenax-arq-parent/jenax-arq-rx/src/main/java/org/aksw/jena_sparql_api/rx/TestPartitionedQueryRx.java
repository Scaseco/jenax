package org.aksw.jena_sparql_api.rx;

//public class TestPartitionedQueryRx {
//    public static void main(String[] args) {
//        JenaSystem.init();
//
//        String queryStr =
//                "CONSTRUCT {\n" +
//                "  ?pub dct:creator ?auts .\n" +
//                "  ?ln rdf:first ?f ; rdf:rest ?r . # ln = list node\n" +
//                "} {\n" +
//                "  ?pub dct:creator ?auts .\n" +
//                "  ?auts rdf:rest* ?ln .\n" +
//                "  ?ln rdf:first ?f ; rdf:rest ?r .\n" +
//                "}";
//
//        Query standardQuery = SparqlQueryParserImpl
//                .create(DefaultPrefixes.prefixes)
//                .apply(queryStr);
//
//        EntityQueryImpl rootedQuery = new EntityQueryImpl();
//        rootedQuery.setPartitionSelectorQuery(standardQuery);
//
//        Var rootNode = Var.alloc("pub");
//        rootedQuery.getDirectGraphPartition().getEntityNodes().add(rootNode);
//        rootedQuery.getPartitionVars().add(rootNode);
//
//        Dataset ds = RDFDataMgr.loadDataset("https://raw.githubusercontent.com/Aklakan/aklakans-devblog/master/2020-10-20-rdflist/src/main/resources/publications.ttl");
//
//        try (RDFConnection conn = RDFConnectionFactory.connect(ds)) {
//            List<RDFNode> rdfNodes = EntityQueryRx.execConstructRooted(conn, rootedQuery).toList().blockingGet();
//
//            for (RDFNode rdfNode : rdfNodes) {
//                System.out.println("Got node: " + rdfNode + ": vvv");
//                RDFDataMgr.write(System.out, rdfNode.getModel(), RDFFormat.TURTLE_BLOCKS);
//            }
//        }
//
//    }
//}
