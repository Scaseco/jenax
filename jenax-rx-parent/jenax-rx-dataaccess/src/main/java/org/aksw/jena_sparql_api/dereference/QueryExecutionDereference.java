package org.aksw.jena_sparql_api.dereference;

import java.io.IOException;
import java.net.URISyntaxException;

import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/30/11
 *         Time: 4:24 PM
 */
public class QueryExecutionDereference
    extends QueryExecutionAdapter
{
    private Query query;
    private Dereferencer dereferencer;

    public QueryExecutionDereference(Query query, Dereferencer dereferencer) {
        this.query = query;
        this.dereferencer = dereferencer;
    }


    @Override
    public Model execDescribe() {
        try {
            return _execDescribe(ModelFactory.createDefaultModel());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Model execDescribe(Model model) {
        try {
            Model result = _execDescribe(model);

            if(model != result) {
                result.add(model);
            }

            return result;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Model _execDescribe(Model model)
            throws IOException, URISyntaxException {

        Node node = QueryExecutionBaseSelect.extractDescribeNode(query);

        return dereferencer.dereference(node.getURI());
    }


    @Override
    public void close() {
        synchronized (this) {
            if(dereferencer != null) {
                try {
                    dereferencer.close();
                    dereferencer = null;
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
