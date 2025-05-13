package org.aksw.jenax.dataaccess.sparql.linksource;

/** Usually you want {@link RDFLinkSourceWrapperOverNewLinkBase}. */
public class RDFLinkSourceWrapperBase<X extends RDFLinkSource>
    implements RDFLinkSourceWrapper<X>
{
    protected X delegate;

    public RDFLinkSourceWrapperBase(X delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public X getDelegate() {
        return delegate;
    }
}
