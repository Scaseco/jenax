package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineDecorator;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFLinkSourceTransformBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.expr.ExprTransform;

/**
 * A wrapper for an {@link RDFEngine} that supports transformations on several levels.
 * The levels are: data source, link source, link, stmt, algebra and expr.
 * All transforms are uniformly treated as transformations on the data source.
 * Each transform returns a new {@link RDFEngineDecorator} instance with the
 * data source transformation applied.
 *
 * Note, that transformations may be grouped. For example, adding multiple
 * Rewrite transformations on the algebra level will group than as to avoid
 * query-algebra-query roundtrips.
 */
public class RDFLinkSourceDecorator
{
    protected RDFLinkSource baseLinkSource;
    protected RDFLinkSourceTransformBuilder linkSourceTransformBuilder = new RDFLinkSourceTransformBuilder();

    // NOTE: RDFLinkMods need to be added as RDFLinkTransforms!
    // (FIXME These builders are applied after the link source transform!)
    // protected List<RDFLinkBuilderTransform> linkBuilderMods = new ArrayList<>();

    public RDFLinkSourceDecorator(RDFLinkSource baseLinkSource) {
        this.baseLinkSource = Objects.requireNonNull(baseLinkSource);
    }

    public static RDFLinkSourceDecorator of(RDFLinkSource baseLinkSource) {
        return new RDFLinkSourceDecorator(baseLinkSource);
    }

    // TODO Add a variant that unwraps a decorated link source and adds all transforms
    // to a builder.
//    public static RDFLinkSourceDecorator ofUnwrapped(RDFLinkSource baseLinkSource) {
//    	RDFLinkSourceDecorator = null;
//    	if (RDFLinkSourceDecorator instanceof DecoratedRDFLinkSource<?> d) {
//    		d.getDelegate();
//    		d
//    	} else {
//
//    	}
//    }

    public RDFLinkSource snapshotLinkSource() {
        RDFLinkSource result = baseLinkSource;
        RDFLinkSourceTransform linkSourceTransform = linkSourceTransformBuilder.build();
        if (linkSourceTransform != null) {
            result = linkSourceTransform.apply(result);
        }
        return result;
    }

    public RDFLinkBuilder<?> snapshotLinkBuilder() {
        RDFLinkSource linkSource = snapshotLinkSource();
        RDFLinkBuilder<?> result = linkSource.newLinkBuilder();
//        for (RDFLinkBuilderTransform mod : linkBuilderMods) {
//            RDFLinkBuilder<?> next = mod.apply(result);
//            result = next;
//        }
        return result;
    }

    public RDFLink snapshotLink() {
        RDFLinkBuilder<?> tmp = snapshotLinkBuilder();
        return tmp.build();
    }

    public RDFLinkSourceDecorator addLinkBuilderMod(RDFLinkBuilderTransform linkBuilderMod) {
        RDFLinkSourceTransform rs = new  RDFLinkSourceTransformLinkBuilder(linkBuilderMod);
        linkSourceTransformBuilder.add(rs);
        return this;
    }

    public RDFLinkSourceDecorator decorate(RDFLinkSourceTransform linkSourceTransform) {
        RDFLinkSource snapshot = snapshotLinkSource();
        RDFLinkSource linkSource = linkSourceTransform.apply(snapshot);
        RDFLinkBuilder<?> linkBuilder = linkSource.newLinkBuilder();
        RDFLink link = linkBuilder.build();

        // Coming here means success - add the transform
        linkSourceTransformBuilder.add(linkSourceTransform);
        return this;
    }

    public DecoratedRDFLinkSource build() {
        RDFLinkSource effectiveLinkSource = snapshotLinkSource();
        return new DecoratedRDFLinkSource<>(baseLinkSource, effectiveLinkSource);
    }

    public RDFLinkSourceDecorator decorate(RDFLinkTransform transform) {
        linkSourceTransformBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceDecorator decorate(LinkSparqlQueryTransform transform) {
        linkSourceTransformBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceDecorator decorate(QueryTransform transform) {
        linkSourceTransformBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceDecorator decorate(QueryExecTransform transform) {
        linkSourceTransformBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceDecorator decorate(LinkSparqlUpdateTransform transform) {
        linkSourceTransformBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceDecorator decorate(UpdateRequestTransform transform) {
        linkSourceTransformBuilder.add(transform);
        return this;
    }

    // OpTransform
    // XXX Currently applied to query and update; perhaps add extra methods for either aspect.
    public RDFLinkSourceDecorator decorate(OpTransform transform) {
        linkSourceTransformBuilder.add(transform);
        return this;
    }

    public RDFLinkSourceDecorator decorate(ExprTransform transform) {
        linkSourceTransformBuilder.add(transform);
        return this;
    }
}

//  public RDFLinkSourceDecorator decorate(SparqlStmtTransform transform) {
//  linkSourceTransformBuilder.add(transform);
//  return this;
//}

//  public RDFLinkSourceDecorator decorate(QueryExecTransform transform) {
//  linkSourceTransformBuilder.add(transform);
//  return this;
//}

//public RDFEngineDecorator<WrappedRDFEngine<DecoratedRDFEngine<X>>> decorate(RdfDataSourceTransform transform) {
//RDFLinkSourceTransform linkSourceTransform = RDFLinkSourceTransforms.of(transform);
//return decorate(linkSourceTransform);
//}
//
//public RDFEngineDecorator<WrappedRDFEngine<DecoratedRDFEngine<X>>> decorate(RDFLinkSourceTransform transform) {
//DecoratedRDFEngine<X> newBase = this.build();
//RDFLinkSource baseLinkSource = new RDFLinkSourceOverRDFEngine(newBase);
//RDFLinkSource wrappedLinkSource = transform.apply(baseLinkSource);
//WrappedRDFEngine<DecoratedRDFEngine<X>> newEngine = new WrappedRDFEngine<>(newBase, wrappedLinkSource);
//return RDFEngineDecorator.of(newEngine);
//}
