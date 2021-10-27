package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.rdf.model.Model;

public class SinkModelWriter
    implements Sink<Model>
{
    private SparqlService sparqlService;

    public SinkModelWriter(SparqlService sparqlService) {
        super();
        this.sparqlService = sparqlService;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub

    }


    @Override
    public void send(Model model) {
        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
        UpdateExecutionUtils.executeInsert(uef, model);
    }

}
