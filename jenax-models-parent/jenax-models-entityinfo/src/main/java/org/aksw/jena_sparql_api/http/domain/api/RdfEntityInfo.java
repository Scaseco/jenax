package org.aksw.jena_sparql_api.http.domain.api;

import java.util.Collection;
import java.util.List;

import org.aksw.commons.util.entity.EntityInfo;
import org.aksw.dcat.ap.domain.api.Checksum;
import org.apache.jena.rdf.model.Resource;

public interface RdfEntityInfo
    extends Resource, EntityInfo
{
    RdfEntityInfo setContentEncodings(List<String> enocdings);
    RdfEntityInfo setContentType(String contentType);
    RdfEntityInfo setCharset(String charset);

    RdfEntityInfo setByteSize(Long size);

    // Only applicable for encoded entities
    RdfEntityInfo setUncompressedByteSize(Long size);

//	RdfEntityInfo setContentLength(Long length);

    Collection<Checksum> getHashes();

//	@ToString
//	default $toString() {
//
//	}

    default Checksum getHash(String algo) {
        Checksum result = getHashes().stream()
            .filter(x -> algo.equalsIgnoreCase(x.getAlgorithm()))
            .findAny()
            .orElse(null);
        return result;
    }


    public static void copy(RdfEntityInfo tgt, RdfEntityInfo src) {
        tgt
            .setCharset(src.getCharset())
            .setContentEncodings(src.getContentEncodings())
            .setContentType(src.getContentType())
            .setByteSize(src.getByteSize())
            .setUncompressedByteSize(src.getUncompressedByteSize())
            ;
    }

}
