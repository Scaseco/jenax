package org.aksw.jenax.dataaccess.sparql.link.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.query.TransformList;
import org.aksw.jenax.dataaccess.sparql.link.dataset.LinkDatasetGraphTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransformBuilder;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransformBuilder;

public class RDFLinkModularTransformBuilder {
    protected List<RDFLinkTransform> linkTransforms = new ArrayList<>();

    protected LinkSparqlQueryTransformBuilder qBuilder = new LinkSparqlQueryTransformBuilder();
    protected LinkSparqlUpdateTransformBuilder uBuilder = new LinkSparqlUpdateTransformBuilder();

    protected RDFLinkTransform lastLink() {
        RDFLinkTransform result = null;
        LinkSparqlQueryTransform q = qBuilder.build();
        LinkSparqlUpdateTransform u = uBuilder.build();
        LinkDatasetGraphTransform d = null; // dBuilder.build();
        if (u != null || q != null) {
            result = new RDFLinkTransformModular(q, u, d);
        }
        return result;
    }

    protected void finalizeSubBuilder() {
        RDFLinkTransform lastLink = lastLink();
        if (lastLink != null) {
            linkTransforms.add(lastLink);
            qBuilder.reset();
            uBuilder.reset();
        }
    }

    protected void addInternal(RDFLinkTransform transform) {
        if (transform instanceof RDFLinkTransformModular t) {
            LinkSparqlQueryTransform q = t.getQueryTransform();
            if (q != null) {
                qBuilder.add(q);
            }

            LinkSparqlUpdateTransform u = t.getUpdateTransform();
            if (u != null) {
                uBuilder.add(u);
            }

//            LinkDatasetTransform d = t.getDatasetTransform();
//            if (d != null) {
//                dBuilder.add(d);
//            }

        } else {
            finalizeSubBuilder();
            linkTransforms.add(transform);
        }
    }

    public RDFLinkModularTransformBuilder add(RDFLinkTransform transform) {
        TransformList.streamFlatten(true, transform).forEach(this::addInternal);
        return this;
    }

    public RDFLinkModularTransformBuilder add(LinkSparqlQueryTransform transform) {
        qBuilder.add(transform);
        return this;
    }

    public RDFLinkModularTransformBuilder add(QueryTransform transform) {
        qBuilder.add(transform);
        return this;
    }

    public RDFLinkModularTransformBuilder add(OpTransform transform) {
        qBuilder.add(transform);
        uBuilder.add(transform);
        return this;
    }

    public RDFLinkTransform build() {
        Stream<RDFLinkTransform> stream = linkTransforms.stream();

        // Add an op transform if it is pending
        RDFLinkTransform lastLink = lastLink();
        if (lastLink != null) {
            stream = Stream.concat(stream, Stream.of(lastLink));
        }
        return TransformList.flattenOrNull(true, RDFLinkTransformList::new, stream);
    }
}
