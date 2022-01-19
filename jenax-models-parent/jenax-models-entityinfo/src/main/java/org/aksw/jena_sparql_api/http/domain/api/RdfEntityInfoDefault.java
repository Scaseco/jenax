package org.aksw.jena_sparql_api.http.domain.api;

import java.util.List;
import java.util.Set;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView(RdfEntityInfo.class)
// @RdfType("eg:EntityInfo")
public interface RdfEntityInfoDefault
    extends RdfEntityInfo
{
    @IriNs("eg")
    @Override
    List<String> getContentEncodings();


    /**
     * Helper attribute to skolemize the rdf list of content encodings.
     * At present {@code @HashId} always generates ids for 'this'; we
     * cannot express generate an ID for the lists underlying target rdf resource.
     * we need some new annotation in the future such as {@code @ValueHashId}.
     */
    @Iri("eg:contentEncodings")
    RdfList getContentEncodingsRdfList();

    @IriNs("eg")
    @Override
    String getContentType();

//    @IriNs("eg")
//    @Override
//    Long getContentLength();

    /**
     * Charset, such as UTF-8 or ISO 8859-1
     *
     * @return
     */
    @IriNs("eg")
    @Override
    String getCharset();

    /**
     * The set of language tags for which the content is suitable.
     *
     * @return
     */
    @IriNs("eg")
    @Override
    Set<String> getLanguageTags();


    @IriNs("dct")
    @Override
    Set<String> getConformsTo();


    @IriNs("eg")
    @Override
    Set<Checksum> getHashes();


    @IriNs("eg")
    @Override
    Long getByteSize();

    @IriNs("eg")
    @Override
    Long getUncompressedByteSize();
}
