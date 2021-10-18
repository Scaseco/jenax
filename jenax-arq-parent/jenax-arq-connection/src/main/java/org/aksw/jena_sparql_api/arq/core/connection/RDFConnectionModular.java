package org.aksw.jena_sparql_api.arq.core.connection;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFDatasetConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.update.UpdateRequest;

/**
 * This class war removed in jena 4.3.0 - for now its added here but with the new additions to the connection
 * API it might be possible to migrate away from it ~ Claus
 *
 * Implementation of {@link RDFConnection} where the query, update and graph store
 * operations are given by specific implementations of the respective interfaces.
 */
@Deprecated
public class RDFConnectionModular implements RDFConnection {

    private final SparqlQueryConnection queryConnection;
    private final SparqlUpdateConnection updateConnection;
    private final RDFDatasetConnection datasetConnection;
    private final Transactional transactional;

    @Override public void begin()                       { transactional.begin(); }
    @Override public void begin(TxnType txnType)        { transactional.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { transactional.begin(mode); }
    @Override public boolean promote(Promote promote)   { return transactional.promote(promote); }
    @Override public void commit()                      { transactional.commit(); }
    @Override public void abort()                       { transactional.abort(); }
    @Override public boolean isInTransaction()          { return transactional.isInTransaction(); }
    @Override public void end()                         { transactional.end(); }
    @Override public ReadWrite transactionMode()        { return transactional.transactionMode(); }
    @Override public TxnType transactionType()          { return transactional.transactionType(); }

    public RDFConnectionModular(SparqlQueryConnection queryConnection ,
                                SparqlUpdateConnection updateConnection ,
                                RDFDatasetConnection datasetConnection ) {
        this.queryConnection = queryConnection;
        this.updateConnection = updateConnection;
        this.datasetConnection = datasetConnection;
        this.transactional =
            updateConnection  != null ? updateConnection :
            datasetConnection != null ? datasetConnection :
            queryConnection   != null ? queryConnection :
            null;
    }

    public RDFConnectionModular(RDFConnection connection) {
        this.queryConnection = connection;
        this.updateConnection = connection;
        this.datasetConnection = connection;
        this.transactional = connection;
    }

    private SparqlQueryConnection queryConnection() {
        if ( queryConnection == null )
            throw new UnsupportedOperationException("No SparqlQueryConnection");
        return queryConnection;
    }

    private SparqlUpdateConnection updateConnection() {
        if ( updateConnection == null )
            throw new UnsupportedOperationException("No SparqlUpdateConnection");
        return updateConnection;
    }

    private RDFDatasetConnection datasetConnection() {
        if ( datasetConnection == null )
            throw new UnsupportedOperationException("No RDFDatasetConnection");
        return datasetConnection;
    }

    @Override
    public QueryExecution query(Query query) { return queryConnection().query(query); }

    @Override
    public void update(UpdateRequest update) {
        updateConnection().update(update);
    }

    @Override
    public void load(String graphName, String file) {
        datasetConnection().load(graphName, file);
    }

    @Override
    public void load(String file) {
        datasetConnection().load(file);
    }

    @Override
    public void load(String graphName, Model model) {
        datasetConnection().load(graphName, model);
    }

    @Override
    public void load(Model model) {
        datasetConnection().load(model);
    }

    @Override
    public void put(String graphName, String file) {
        datasetConnection().put(graphName, file);
    }

    @Override
    public void put(String file) {
        datasetConnection().put(file);
    }

    @Override
    public void put(String graphName, Model model) {
        datasetConnection().put(graphName, model);
    }

    @Override
    public void put(Model model) {
        datasetConnection().put(model);
    }

    @Override
    public void delete(String graphName) {
        datasetConnection().delete(graphName);
    }

    @Override
    public void delete() {
        datasetConnection().delete();
    }

    @Override
    public void loadDataset(String file) {
        datasetConnection().loadDataset(file);
    }

    @Override
    public void loadDataset(Dataset dataset) {
        datasetConnection().loadDataset(dataset);
    }

    @Override
    public void putDataset(String file) {
        datasetConnection().putDataset(file);
    }

    @Override
    public void putDataset(Dataset dataset) {
        datasetConnection().putDataset(dataset);
    }

    @Override
    public Model fetch(String graphName) {
        return datasetConnection.fetch(graphName);
    }
    @Override
    public Model fetch() {
        return datasetConnection().fetch();
    }
    @Override
    public Dataset fetchDataset() {
        return datasetConnection().fetchDataset();
    }
    @Override
    public boolean isClosed() { return false; }

    /** Close this connection.  Use with try-resource. */
    @Override
    public void close() {
        if ( queryConnection != null )
            queryConnection.close();
        if ( updateConnection != null )
            updateConnection.close();
        if ( datasetConnection != null )
            datasetConnection.close();
    }
    @Override
    public QueryExecutionBuilder newQuery() {
        throw new UnsupportedOperationException();
    }
}

