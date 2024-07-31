package org.aksw.jenax.graphql.sparql.v2.api2;

//public interface HasElementNodeBuilder
//    extends SparqlPathTraversable<ElementNodeBuilder>
//{
//    ElementNodeBuilder newElementNodeBuilder();
//
//    @Override
//    /** Convenience function to create a sub field by traversal along a given property. */
//    default ElementNodeBuilder step(Path path) {
//        Objects.requireNonNull(path);
//
//        String baseName = null;
//        if (path instanceof P_Path0 p0) {
//            Node node = p0.getNode();
//            if (node.isURI()) {
//                baseName = node.getLocalName();
//            }
//        }
//
//        ElementNodeBuilder result = newElementNodeBuilder()
//            .baseName(baseName)
//            .newConnectiveBuilder()
//                .step(path)
//                .set()
//            .build();
//        return result;
//    }
//}
