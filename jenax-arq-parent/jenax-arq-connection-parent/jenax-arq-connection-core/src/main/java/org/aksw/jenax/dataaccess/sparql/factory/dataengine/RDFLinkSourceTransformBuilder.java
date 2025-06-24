package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.query.TransformList;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkModularTransformBuilder;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransformFromLinkTransform;
import org.apache.jena.sparql.expr.ExprTransform;


public class RDFLinkSourceTransformBuilder {
    // protected List<RDFLinkBuilderTransform> linkBuilderTransforms = new ArrayList<>();
    protected List<RDFLinkSourceTransform> sourceTransforms = new ArrayList<>();
    protected RDFLinkModularTransformBuilder lBuilder = new RDFLinkModularTransformBuilder();

    protected RDFLinkSourceTransform lastLink() {
        RDFLinkSourceTransform result = null;
        RDFLinkTransform linkTransform = lBuilder.build();
        if (linkTransform != null) {
            result = new RDFLinkSourceTransformFromLinkTransform(linkTransform);
        }
        return result;
    }

    protected void finalizeSubBuilder() {
        RDFLinkSourceTransform lastLink = lastLink();
        if (lastLink != null) {
            sourceTransforms.add(lastLink);
            lBuilder.reset();
        }
    }

    protected void addInternal(RDFLinkSourceTransform transform) {
        if (transform instanceof RDFLinkSourceTransformFromLinkTransform t) {
            RDFLinkTransform x = t.getLinkTransform();
            if (x != null) {
                lBuilder.add(x);
            }
        } else {
            finalizeSubBuilder();
            sourceTransforms.add(transform);
        }
    }

    public RDFLinkSourceTransformBuilder add(RDFLinkSourceTransform transform) {
        TransformList.streamFlatten(true, transform).forEach(this::addInternal);
        return this;
    }

    public RDFLinkSourceTransformBuilder add(RDFLinkTransform transform) {
        lBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceTransformBuilder add(LinkSparqlQueryTransform transform) {
        lBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceTransformBuilder add(QueryTransform transform) {
        lBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceTransformBuilder add(QueryExecTransform transform) {
        lBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceTransformBuilder add(LinkSparqlUpdateTransform transform) {
        lBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceTransformBuilder add(UpdateRequestTransform transform) {
        lBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceTransformBuilder add(OpTransform transform) {
        lBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceTransformBuilder add(ExprTransform transform) {
        lBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceTransform build() {
        Stream<RDFLinkSourceTransform> stream = sourceTransforms.stream();

        // Add an op transform if it is pending
        RDFLinkSourceTransform lastLink = lastLink();
        if (lastLink != null) {
            stream = Stream.concat(stream, Stream.of(lastLink));
        }
        RDFLinkSourceTransform result = TransformList.flattenOrNull(true, RDFLinkSourceTransformList::new, stream);
        return result;
    }
}
//public class RDFLinkSourceTransformBuilder
//    implements RDFLinkDecoratorBuilder<RDFLinkSourceTransformBuilder>
//{
//    protected List<RDFLinkSourceTransform> mods = new ArrayList<>();
//    protected RDFLinkTransformBuilder openBuilder = null;
//
//    protected void ensureOpenBuilder() {
//        if (openBuilder == null) {
//            openBuilder = new RDFLinkTransformBuilder();
//        }
//    }
//
//    protected void closeLinkBuilder() {
//        if (openBuilder != null) {
//            if (!openBuilder.isEmpty()) {
//                RDFLinkTransform linkTransform = openBuilder.build();
//                RDFLinkSourceTransform tmp = new RDFLinkSourceTransformFromLinkTransform(linkTransform);
//                mods.add(tmp);
//            }
//            openBuilder = null;
//        }
//    }
//
//    public RDFLinkSourceTransformBuilder decorate(RDFLinkSourceTransform transform) {
//        closeLinkBuilder();
//        mods.add(transform);
//        return this;
//    }
//
//    @Override
//    public RDFLinkSourceTransformBuilder decorate(RDFLinkTransform transform) {
//        RDFLinkSourceTransform tmp = new RDFLinkSourceTransformFromLinkTransform(transform);
//        return decorate(tmp);
//    }
//
//    @Override
//    public RDFLinkSourceTransformBuilder decorate(QueryTransform transform) {
//        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
//        return decorate(tmp);
//    }
//
//    @Override
//    public RDFLinkSourceTransformBuilder decorate(QueryExecTransform transform) {
//        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
//        return decorate(tmp);
//    }
//
//    @Override
//    public RDFLinkSourceTransformBuilder decorate(UpdateRequestTransform transform) {
//        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
//        return decorate(tmp);
//    }
//
//    @Override
//    public RDFLinkSourceTransformBuilder decorate(LinkSparqlQueryTransform transform) {
//        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
//        return decorate(tmp);
//    }
//
//    @Override
//    public RDFLinkSourceTransformBuilder decorate(SparqlStmtTransform transform) {
//        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
//        return decorate(tmp);
//    }
//
//    // OpTransform
//    // XXX Currently applied to query and update; perhaps add extra methods for either aspect.
//    @Override
//    public RDFLinkSourceTransformBuilder decorate(Rewrite transform) {
//        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
//        return decorate(tmp);
//    }
//
//    @Override
//    public RDFLinkSourceTransformBuilder decorate(ExprTransform transform) {
//        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
//        return decorate(tmp);
//    }
//
//    public RDFLinkSourceTransform build() {
//        return new RDFLinkSourceTransformList(mods);
//    }
//}
