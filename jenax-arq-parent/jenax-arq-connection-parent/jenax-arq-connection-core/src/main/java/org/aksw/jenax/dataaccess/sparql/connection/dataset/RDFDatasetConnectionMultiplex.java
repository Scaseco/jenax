package org.aksw.jenax.dataaccess.sparql.connection.dataset;

import java.util.Arrays;
import java.util.Collection;

import org.aksw.jenax.dataaccess.sparql.common.MultiplexUtils;
import org.aksw.jenax.dataaccess.sparql.common.TransactionalMultiplex;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFDatasetConnection;

public class RDFDatasetConnectionMultiplex
    extends TransactionalMultiplex<RDFDatasetConnection>
    implements RDFDatasetConnection
{
    public RDFDatasetConnectionMultiplex(RDFDatasetConnection ... delegates) {
         this(Arrays.asList(delegates));
    }

    public RDFDatasetConnectionMultiplex(Collection<? extends RDFDatasetConnection> delegates) {
        super(delegates);
    }

    @Override
    public Model fetch(String graphName) {
        return delegates.iterator().next().fetch(graphName);
    }

    @Override
    public Model fetch() {
        return delegates.iterator().next().fetch();
    }

    @Override
    public Dataset fetchDataset() {
        return delegates.iterator().next().fetchDataset();
    }

    @Override
    public void load(String graphName, String file) {
        MultiplexUtils.forEach(delegates, d -> d.load(graphName, file));
    }

    @Override
    public void load(String file) {
        MultiplexUtils.forEach(delegates, d -> d.load(file));
    }

    @Override
    public void load(String graphName, Model model) {
        MultiplexUtils.forEach(delegates, d -> d.load(graphName, model));
    }

    @Override
    public void load(Model model) {
        MultiplexUtils.forEach(delegates, d -> d.load(model));
    }

    @Override
    public void put(String graphName, String file) {
        MultiplexUtils.forEach(delegates, d -> d.put(graphName, file));
    }

    @Override
    public void put(String file) {
        MultiplexUtils.forEach(delegates, d -> d.put(file));
    }

    @Override
    public void put(String graphName, Model model) {
        MultiplexUtils.forEach(delegates, d -> d.put(graphName, model));
    }

    @Override
    public void put(Model model) {
        MultiplexUtils.forEach(delegates, d -> d.put(model));
    }

    @Override
    public void delete(String graphName) {
        MultiplexUtils.forEach(delegates, d -> d.delete(graphName));
    }

    @Override
    public void delete() {
        MultiplexUtils.forEach(delegates, RDFDatasetConnection::delete);
    }

    @Override
    public void loadDataset(String file) {
        MultiplexUtils.forEach(delegates, d -> d.loadDataset(file));
    }

    @Override
    public void loadDataset(Dataset dataset) {
        MultiplexUtils.forEach(delegates, d -> d.loadDataset(dataset));
    }

    @Override
    public void putDataset(String file) {
        MultiplexUtils.forEach(delegates, d -> d.putDataset(file));
    }

    @Override
    public void putDataset(Dataset dataset) {
        MultiplexUtils.forEach(delegates, d -> d.putDataset(dataset));
    }

    @Override
    public boolean isClosed() {
        boolean result = delegates.iterator().next().isClosed();
        return result;
    }

    @Override
    public void close() {
        MultiplexUtils.forEach(delegates, RDFDatasetConnection::close);
    }

}
