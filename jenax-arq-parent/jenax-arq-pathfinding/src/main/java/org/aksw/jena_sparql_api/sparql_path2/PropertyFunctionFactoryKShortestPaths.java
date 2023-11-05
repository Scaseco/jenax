package org.aksw.jena_sparql_api.sparql_path2;

import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
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
        PropertyFunctionPathFinder result = PropertyFunctionPathFinder.DEFAULT_IRI.equals(uri)
                ? new PropertyFunctionPathFinder(dataSourceToPathFinder)
                : null
                ;

        return result;
    }
}
