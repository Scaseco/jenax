package org.aksw.jena_sparql_api.changeset;

import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Triple;

import com.google.common.base.Function;

public class FN_TripleToMd5
    implements Function<Triple, String>
{
    @Override
    public String apply(Triple triple) {
        String result = TripleUtils.md5sum(triple);
        return result;
    }

    public static final FN_TripleToMd5 fn = new FN_TripleToMd5();
}