package org.aksw.dcat.jena.domain.api;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Iterables;

public interface DcatDistributionCore
    extends DcatEntityCore
{
    String getFormat();
    DcatDistributionCore setFormat(String format);

    /** Set-based access because of having seen dirty data in the wild; actually the arity is 0..1 */
    Set<String> getAccessUrls();
    Set<String> getDownloadUrls();

//	default void setAccessUrls(Collection<String> urls) {
//		replace(getAccessUrls(), urls);
//	}
//
//	default void setDownloadUrls(Collection<String> urls) {
//		replace(getAccessUrls(), urls);
//	}

    default String getAccessUrl() {
        Set<String> c = getAccessUrls();
        String result = Iterables.getFirst(c, null);
        return result;
    }

    default void setAccessUrl(String url) {
        Set<String> c = getAccessUrls();
        replace(c, url);
    }

    default String getDownloadUrl() {
        Set<String> c = getDownloadUrls();
        String result = Iterables.getFirst(c, null);
        return result;
    }

    default void setDownloadUrl(String url) {
        Set<String> c = getDownloadUrls();
        replace(c, url);
    }

    public static <T, C extends Collection<T>> C replace(C c, T item) {
        c.clear();
        c.add(item);
        return c;
    }

    public static <T, C extends Collection<T>> C replace(C c, Collection<T> items) {
        c.clear();
        c.addAll(items);
        return c;
    }
}
