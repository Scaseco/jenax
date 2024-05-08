package org.aksw.jenax.model.polyfill.domain.api;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.model.rdfs.domain.api.HasRdfsComment;
import org.aksw.jenax.model.rdfs.domain.api.HasRdfsLabel;

@Namespace("https://w3id.org/aksw/norse#polyfill.")
@RdfType
@ResourceView
public interface PolyfillRewriteJava
    extends HasRdfsLabel, HasRdfsComment
{
    @IriNs
    String getJavaClass();
    PolyfillRewriteJava setJavaClass(String javaClassName);
}
