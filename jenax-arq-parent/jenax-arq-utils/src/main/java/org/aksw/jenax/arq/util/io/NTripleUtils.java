package org.aksw.jenax.arq.util.io;

import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.lang.LangNQuads;
import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.ParserProfileStd;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.Quad;

/** TODO This class should be separated into a ParserProfiles class and ParserUtils one */
public class NTripleUtils {

    // Adjustment from RiotLib to use IRIResolver.createNoResolve()
    public static ParserProfile permissiveProfile() {
        return new ParserProfileStd(RiotLib.factoryRDF(),
                                    ErrorHandlerFactory.errorHandlerWarn,
                                    IRIxResolver.create().resolve(false).allowRelative(true).build(),
                                    PrefixMapFactory.create(),
                                    RIOT.getContext().copy(),
                                    false, false);
    }

    public static ParserProfile profile = permissiveProfile();

    /**
     * Parse the first triple from a given string.
     * It is recommended for the string to not have any trailing data.
     *
     * @param str
     * @return
     */
    public static Triple parseNTriplesString(String str)  {
        Tokenizer tokenizer = TokenizerText.fromString(str);
        LangNTriples parser = new LangNTriples(tokenizer, profile, null);
        Triple result = parser.next();

        return result;
    }

    /**
     * Parse the first quad from a given string.
     * It is recommended for the string to not have any trailing data.
     *
     * @param str
     * @return
     */
    public static Quad parseNQuadsString(String str)  {
        Tokenizer tokenizer = TokenizerText.fromString(str);
        LangNQuads parser = new LangNQuads(tokenizer, profile, null);
        Quad result = parser.next();

        return result;
    }
}
