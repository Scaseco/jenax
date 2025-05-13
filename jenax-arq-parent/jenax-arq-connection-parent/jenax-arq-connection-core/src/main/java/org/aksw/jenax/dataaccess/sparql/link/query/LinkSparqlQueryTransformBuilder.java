package org.aksw.jenax.dataaccess.sparql.link.query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.query.QueryExecTransformBuilder;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.query.QueryTransformBuilder;
import org.aksw.jenax.arq.util.query.TransformList;
import org.apache.jena.sparql.expr.ExprTransform;

public class LinkSparqlQueryTransformBuilder {
    protected List<LinkSparqlQueryTransform> linkTransforms = new ArrayList<>();
    protected QueryTransformBuilder qTransformBuilder = new QueryTransformBuilder();
    protected QueryExecTransformBuilder qExecTransformBuilder = new QueryExecTransformBuilder();

    protected LinkSparqlQueryTransform lastLink() {
        LinkSparqlQueryTransform result = null;
        QueryTransform qTransform = qTransformBuilder.build();
        QueryExecTransform qExecTransform = qExecTransformBuilder.build();
        if (qTransform != null || qExecTransform != null) {
            result = new LinkSparqlQueryTransformQueryTransform(qTransform, qExecTransform);
        }
        return result;
    }

    protected void finalizeSubBuilder() {
        LinkSparqlQueryTransform lastLink = lastLink();
        if (lastLink != null) {
            linkTransforms.add(lastLink);
            qTransformBuilder.reset();
            qExecTransformBuilder.reset();
        }
    }

    protected void addInternal(LinkSparqlQueryTransform transform) {
        if (transform instanceof LinkSparqlQueryTransformQueryTransform t) {
            QueryTransform qt = t.getQueryTransform();
            if (qt != null) {
                qTransformBuilder.add(qt);
            }

            QueryExecTransform qet = t.getQueryExecTransform();
            if (qet != null) {
                qExecTransformBuilder.add(qet);
            }
        } else {
            finalizeSubBuilder();
            linkTransforms.add(transform);
        }
    }

    public LinkSparqlQueryTransformBuilder add(LinkSparqlQueryTransform transform) {
        TransformList.streamFlatten(true, transform).forEach(this::addInternal);
        return this;
    }

    public LinkSparqlQueryTransformBuilder add(QueryExecTransform transform) {
        // qTransformBuilder.add(transform);
        add(new LinkSparqlQueryTransformQueryTransform(null, transform));
        return this;
    }

    public LinkSparqlQueryTransformBuilder add(QueryTransform transform) {
        qTransformBuilder.add(transform);
        return this;
    }

    public LinkSparqlQueryTransformBuilder add(OpTransform transform) {
        qTransformBuilder.add(transform);
        return this;
    }

    public LinkSparqlQueryTransformBuilder add(ExprTransform transform) {
        qTransformBuilder.add(transform);
        return this;
    }

    public LinkSparqlQueryTransform build() {
        Stream<LinkSparqlQueryTransform> stream = linkTransforms.stream();

        // Add an op transform if it is pending
        LinkSparqlQueryTransform lastLink = lastLink();
        if (lastLink != null) {
            stream = Stream.concat(stream, Stream.of(lastLink));
        }
        return TransformList.flattenOrNull(true, LinkSparqlQueryTransformList::new, stream);
    }

    public void reset() {
        linkTransforms.clear();
        qTransformBuilder.reset();
        qExecTransformBuilder.reset();
    }

    @Override
    public String toString() {
        return "LinkSparqlQueryTransformBuilder [linkTransforms=" + linkTransforms + ", qTransformBuilder="
                + qTransformBuilder + ", qExecTransformBuilder=" + qExecTransformBuilder + "]";
    }
}
