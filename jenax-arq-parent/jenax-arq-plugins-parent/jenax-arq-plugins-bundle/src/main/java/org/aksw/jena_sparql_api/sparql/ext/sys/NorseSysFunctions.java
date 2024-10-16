package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.norse.term.core.NorseTerms;

public class NorseSysFunctions {
    /**
     * Sleeps for the given amount of milli seconds.
     *
     * @param millis
     * @return True if sleeping succeeded.
     * @throws InterruptedException
     */
    @IriNs(NorseTerms.NS + "sys.")
    public static boolean sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
        return true;
    }
}
