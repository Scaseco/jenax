package org.aksw.jenax.web.filter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;

/**
 * Source: https://stackoverflow.com/questions/2811769/adding-an-http-header-to-the-request-in-a-servlet-filter
 *
 * Allow adding additional header entries to a request
 *
 */
public class HeaderMapRequestWrapper extends HttpServletRequestWrapper {

	protected boolean overrideValuesMode;
    protected ListMultimap<String, String> headerMap = ArrayListMultimap.create();

    /**
     * construct a wrapper for this request
     *
     * @param request
     * @param overrideMode If false then values are appended, overridden otherwise. Cannot be used to remove keys.
     */
    public HeaderMapRequestWrapper(HttpServletRequest request, boolean overrideMode) {
        super(request);
        this.overrideValuesMode = overrideMode;
    }

    /**
     * add a header with given name and value
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name) {
    	String headerValue = super.getHeader(name);

        if ((overrideValuesMode || headerValue == null) && headerMap.containsKey(name)) {
            headerValue = Iterables.getFirst(headerMap.get(name), null);
        }
        return headerValue;
    }

    /**
     * get the Header names
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
        names.addAll(headerMap.keySet());

        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
    	List<String> tmp = headerMap.get(name);
    	List<String> values;
        if (overrideValuesMode && headerMap.containsKey(name)) {
        	values = tmp;
        } else {
            values = Collections.list(super.getHeaders(name));
            values.addAll(tmp);
        }
        return Collections.enumeration(values);
    }

}