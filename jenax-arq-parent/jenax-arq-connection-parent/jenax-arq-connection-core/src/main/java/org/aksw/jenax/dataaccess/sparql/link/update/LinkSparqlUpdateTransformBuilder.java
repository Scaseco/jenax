package org.aksw.jenax.dataaccess.sparql.link.update;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.exec.update.UpdateExecTransform;
import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.query.TransformList;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransformBuilder;
import org.aksw.jenax.arq.util.update.UpdateTransform;

public class LinkSparqlUpdateTransformBuilder {
    protected List<LinkSparqlUpdateTransform> linkTransforms = new ArrayList<>();
    protected UpdateRequestTransformBuilder uTransformBuilder = new UpdateRequestTransformBuilder();
    // protected UpdateExecTransformBuilder uExecTransformBuilder = new UpdateExecTransformBuilder();

    protected LinkSparqlUpdateTransform lastLink() {
        LinkSparqlUpdateTransform result = null;
        UpdateRequestTransform uTransform = uTransformBuilder.build();
        UpdateExecTransform uExecTransform = uExecTransformBuilder.build();
        if (uTransform != null || uExecTransform != null) {
            result = new LinkSparqlUpdateTransformUpdateTransform(uTransform, uExecTransform);
        }
        return result;
    }

    protected void finalizeSubBuilder() {
        LinkSparqlUpdateTransform lastLink = lastLink();
        if (lastLink != null) {
            linkTransforms.add(lastLink);
            uTransformBuilder.reset();
            // uExecTransformBuilder.reset();
        }
    }

    protected void addInternal(LinkSparqlUpdateTransform transform) {
        if (transform instanceof LinkSparqlUpdateTransformUpdateTransform t) {
            UpdateTransform qt = t.getQueryTransform();
            if (qt != null) {
                uTransformBuilder.add(qt);
            }

            QueryExecTransform qet = t.getQueryExecTransform();
            if (qet != null) {
                uExecTransformBuilder.add(qet);
            }
        } else {
            finalizeSubBuilder();
            linkTransforms.add(transform);
        }
    }

    public LinkSparqlUpdateTransformBuilder add(LinkSparqlUpdateTransform transform) {
        TransformList.streamFlatten(true, transform).forEach(this::addInternal);
        return this;
    }

    public LinkSparqlUpdateTransformBuilder add(UpdateRequestTransform transform) {
        uTransformBuilder.add(transform);
        return this;
    }

    public LinkSparqlUpdateTransformBuilder add(OpTransform transform) {
        uTransformBuilder.add(transform);
        return this;
    }

    public LinkSparqlUpdateTransform build() {
        Stream<LinkSparqlUpdateTransform> stream = linkTransforms.stream();

        // Add an op transform if it is pending
        LinkSparqlUpdateTransform lastLink = lastLink();
        if (lastLink != null) {
            stream = Stream.concat(stream, Stream.of(lastLink));
        }
        return TransformList.flattenOrNull(true, LinkSparqlUpdateTransformList::new, stream);
    }

    public void reset() {
        linkTransforms.clear();
        uTransformBuilder.reset();
    }
}
