package org.aksw.jena_sparql_api.sparql_path.api;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.sparql.path.SimplePath;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.path.P_Path0;

/**
 * Abstract base class for concept path finders that use
 * an data summary graph (typically in-memory) and a sparql connection to the actual data.
 *
 * @author Claus Stadler, Nov 11, 2018
 *
 */
public abstract class ConceptPathFinderFactorySummaryBase<T extends ConceptPathFinderFactorySummaryBase<T>>
    implements ConceptPathFinderFactory<T>
{
    protected Model dataSummary;
    // protected SparqlQueryConnection dataConnection;
    protected RdfDataSource dataSource;

    // These flags are so general, it probably makes sense to add them here
    // We can add traits on the concept path finder system level, whether implementations actually make use of these flags
    protected Boolean shortestPathsOnly;

    /**
     * Shortest paths are always simple paths - so if shortestPathsOnly is enabled, this attribute
     * has no effect
     */
    protected Boolean simplePathsOnly;


    protected Set<BiPredicate<? super SimplePath, ? super P_Path0>> pathValidators = new LinkedHashSet<>();

    @Override
    public T setDataSummary(Graph dataSummary) {
        this.dataSummary = ModelFactory.createModelForGraph(dataSummary);
        return self();
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T)this;
    }

    @Override
    public T setDataSummary(Model dataSummary) {
        this.dataSummary = dataSummary;
        return self();
    }

    @Override
    public T setDataConnection(SparqlQueryConnection dataConnection) {
        // this.dataConnection = dataConnection;
        // this.dataSource = new RdfDataSourceO
        throw new RuntimeException("This method should no longer be used.");
        // return self();
    }

    @Override
    public T setDataSource(RdfDataSource dataSource) {
        this.dataSource = dataSource;
        return self();
    }

    public T setDataConnection(RdfDataSource dataSource) {
        this.dataSource = dataSource;
        return self();
    }

    public T setShortestPathsOnly(Boolean onOrOff) {
        this.shortestPathsOnly = onOrOff;
        return self();
    }

    public T setSimplePathsOnly(Boolean onOrOff) {
        this.simplePathsOnly = onOrOff;
        return self();
    }

    @Override
    public T addPathValidator(BiPredicate<? super SimplePath, ? super P_Path0> pathValidator) {
        Objects.requireNonNull(pathValidator);
        pathValidators.add(pathValidator);
        return self();
    }


    @Override
    public Model getDataSummary() {
        return dataSummary;
    }

    @Override
    public SparqlQueryConnection getDataConnection() {
        throw new RuntimeException("This method should no longer be used");
        // return dataConnection;
    }

    public RdfDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public Boolean getShortestPathsOnly() {
        return shortestPathsOnly;
    }

    @Override
    public Boolean getSimplePathsOnly() {
        return simplePathsOnly;
    }
}
