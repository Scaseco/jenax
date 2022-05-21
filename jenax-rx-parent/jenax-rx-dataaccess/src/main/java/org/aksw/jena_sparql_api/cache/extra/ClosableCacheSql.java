package org.aksw.jena_sparql_api.cache.extra;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:54 PM
 */
public class ClosableCacheSql
    implements Closeable
{
    private static final Logger logger = LoggerFactory.getLogger(ClosableCacheSql.class);


    private CacheResource resource;
    private InputStream in;

    public ClosableCacheSql(CacheResource resource, InputStream in) {
        this.resource = resource;
        this.in = in;
    }


    @Override
    public void close() throws IOException {
        //SqlUtils.close(rs);
        resource.close();
        if(in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.warn("Cannot close resource",e);
            }
        }
    }
}
