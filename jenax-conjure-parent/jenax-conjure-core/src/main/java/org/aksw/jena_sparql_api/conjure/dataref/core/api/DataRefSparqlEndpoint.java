package org.aksw.jena_sparql_api.conjure.dataref.core.api;

import java.util.List;

import org.apache.jena.sparql.core.DatasetDescription;

public interface DataRefSparqlEndpoint
    extends DataRef
{
    String getServiceUrl();
    List<String> getDefaultGraphs();
    List<String> getNamedGraphs();

    Object getAuth();

    String getAcceptHeaderAskQuery();
    String getAcceptHeaderDataset();
    String getAcceptHeaderGraph();
    String getAcceptHeaderSelectQuery();

    String getSendModeQuery();
    String getSendModeUpdate();

    default DatasetDescription getDatsetDescription() {
        List<String> dgs = getDefaultGraphs();
        List<String> ngs = getNamedGraphs();

        DatasetDescription result = new DatasetDescription();
        result.addAllDefaultGraphURIs(dgs);
        result.addAllNamedGraphURIs(ngs);

        return result;
    }

    @Override
    default <T> T accept(DataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
