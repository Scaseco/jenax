package org.aksw.jena_sparql_api.sparql_path2;

import java.util.function.Function;

import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

public class PropertyFunctionFactoryKShortestPaths
    implements PropertyFunctionFactory
{
    protected Function<RdfDataSource, SparqlKShortestPathFinder> dataSourceToPathFinder;

    public PropertyFunctionFactoryKShortestPaths(Function<RdfDataSource, SparqlKShortestPathFinder> serviceToPathFinder) {
        this.dataSourceToPathFinder = serviceToPathFinder;
    }

    @Override
    public PropertyFunction create(String uri) {
        PropertyFunctionKShortestPaths result = PropertyFunctionKShortestPaths.DEFAULT_IRI.equals(uri)
                ? new PropertyFunctionKShortestPaths(dataSourceToPathFinder)
                : null
                ;

        return result;
    }
}
