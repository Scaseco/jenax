package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

//public class RDFLinkSourceOverRDFEngine
//    implements RDFLinkSource
//{
//    protected RDFEngine engine;
//
//    protected RDFLinkSourceOverRDFEngine(RDFEngine engine) {
//        super();
//        this.engine = Objects.requireNonNull(engine);
//    }
//
//    public static RDFLinkSourceOverRDFEngine of(RDFEngine engine) {
//        return new RDFLinkSourceOverRDFEngine(engine);
//    }
//
//    @Override
//    public RDFLinkBuilder<?> newLinkBuilder() {
//        RDFLinkBuilder<?> linkBuilder = engine.newLinkBuilder();
//        DatasetGraph dataset = engine.getDataset();
//        if (dataset != null) {
//            RDFLinkTransform transform = RDFLinkTransforms.of(new LinkSparqlQueryTransformApp(dataset));
//            linkBuilder.linkTransform(transform);
//        }
//        return linkBuilder;
//    }
//}
