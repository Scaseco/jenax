package org.aksw.jenax.dataaccess.sparql.linksource;

import org.apache.jena.rdflink.RDFLink;

public interface RDFLinkSourceWrapper<X extends RDFLinkSource>
    extends RDFLinkSource
{
    X getDelegate();

    @Override
    default RDFLink newLink() {
        X tmp = getDelegate();
        return tmp.newLink();
    }
}
