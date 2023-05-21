package org.aksw.jenax.model.d2rq.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

/** Descriptions are taken from the specification at:
 * <a href="http://d2rq.org/d2rq-language">http://d2rq.org/d2rq-language</a>.
 */
@ResourceView
public interface D2rqDatabase
    extends Resource
{
    /** The JDBC database URL. This is a string of the form jdbc:subprotocol:subname.
     * For a MySQL database, this is something like jdbc:mysql://hostname:port/dbname. */
    @Iri(D2rqTerms.jdbcDSN)
    String getJdbcDSN();
    D2rqDatabase setJdbcDSN(String jdbcDSN);

    /** The JDBC driver class name for the database. Used together with d2rq:jdbcDSN. Example: com.mysql.jdbc.Driver for MySQL. */
    @Iri(D2rqTerms.jdbcDriver)
    String getJdbcDriver();
    D2rqDatabase setJdbcDriver(String jdbcDriver);

    /** A username if required by the database. */
    @Iri(D2rqTerms.username)
    String getUsername();
    D2rqDatabase setUsername(String username);

    /** A password if required by the database. */
    @Iri(D2rqTerms.password)
    @IriType
    String getPassword();
    D2rqDatabase setPassword(String password);

    /**
     * Returns true iff a resource viewed as this class is a d2rq database.
     * Effectively is only checks for the presence of of a jdbcDSN.
     *
     * @implNote This method relies on {@link #hasJdbcDSN()}.
     */
    public default boolean qualifiesAsD2rqDatabase() {
        boolean result = hasJdbcDSN();
        return result;
    }

    /** Convenience method to check whether a DSN is specified.
     * When checking for whether a resource represents a D2QR database use the
     * semantically accurate {@link #qualifiesAsD2rqDatabase()} method. */
    public default boolean hasJdbcDSN() {
        String dsn = getJdbcDSN();
        return dsn != null;
    }
}
