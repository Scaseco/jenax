package org.aksw.jenax.sparql.relation.dataset;

import org.aksw.jenax.arq.util.binding.TableUtils;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

public class NodesInDatasetGraphImpl
    implements NodesInDatasetGraph
{
    protected DatasetGraph datasetGraph;
    protected Fragment2 graphAndNodeSelector;

    @Override
    public DatasetGraph getDatasetGraph() {
        return datasetGraph;
    }

    @Override
    public Fragment2 getGraphAndNodeSelector() {
        return graphAndNodeSelector;
    }

    @Override
    public Table listGraphAndNodes() {
        Table result;
        if (graphAndNodeSelector.holdsTable()) {
            result = graphAndNodeSelector.extractTable();
        } else {
            Query query = graphAndNodeSelector.toQuery();
            try (RDFLink link = RDFLinkFactory.connect(datasetGraph)) {
                try (QueryExec qe = link.query(query)) {
                    RowSet rs = qe.select();
                    result = TableUtils.createTable(rs);
                }
            }
        }
            return result;
    }
}
