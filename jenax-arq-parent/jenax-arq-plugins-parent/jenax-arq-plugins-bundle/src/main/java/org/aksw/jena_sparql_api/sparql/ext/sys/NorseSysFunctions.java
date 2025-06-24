package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.norse.term.sys.NorseTermsSys;

public class NorseSysFunctions {
    /**
     * Sleeps for the given amount of milliseconds.
     *
     * @param millis
     * @return True if sleeping succeeded.
     * @throws InterruptedException
     */
    @IriNs(NorseTermsSys.NS)
    public static boolean sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
        return true;
    }

    @Iri(NorseTermsSys.threadName)
    public static String threadName() {
        return Thread.currentThread().getName();
    }

}
