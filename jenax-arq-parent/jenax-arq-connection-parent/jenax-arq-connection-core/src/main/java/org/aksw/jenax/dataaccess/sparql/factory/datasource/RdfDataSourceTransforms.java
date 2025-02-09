package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderTransform;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderWrapperBase;
import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderTransform;
import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderWrapperBase;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransform;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.update.UpdateExecWrapperBase;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithSimpleCache;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Transformation views for use with {@link RdfDataEngine#decorate}.
 * The transformations are mostly based on the methods of {@link RdfDataSources}.
 */
public class RdfDataSourceTransforms {

    public static RdfDataSourceTransform decorateWithBuilderTransform(QueryExecBuilderTransform queryBuilderTransform, UpdateExecBuilderTransform updateBuilderTransform) {
        return rdfDataSource -> new RdfDataSourceWrapperBase<>(rdfDataSource) {
            @Override
            public RDFConnection getConnection() {
                RDFConnection result = RDFConnectionUtils.wrapWithBuilderTransform(
                        super.getConnection(),
                        queryBuilderTransform,
                        updateBuilderTransform);
                return result;
            }
        };
    }

    /** Decorate a data source such that execution will fail. */
    public static RdfDataSourceTransform alwaysFail() {
        // XXX Perhaps also wrap the LinkDatasetGraph
        return base -> base.decorate(decorateWithBuilderTransform(
            qeb -> new QueryExecBuilderWrapperBase(qeb) {
                @Override
                public QueryExec build() {
                    return new QueryExecWrapperBase<>(super.build()) {
                        public void beforeExec() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            },
            ueb -> new UpdateExecBuilderWrapperBase(ueb) {
                @Override
                public UpdateExec build() {
                    return new UpdateExecWrapperBase<>(super.build()) {
                        public void beforeExec() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            }
        ));
    }

    /** Cache with unlimited size */
    public static RdfDataSourceTransform simpleCache() {
        Cache<Object, Object> cache = Caffeine.newBuilder().recordStats().build();
        return ds -> new RdfDataSourceWithSimpleCache(ds, cache);
    }

    public static RdfDataSourceTransform simpleCache(long maxSize) {
        Cache<Object, Object> cache = Caffeine.newBuilder().maximumSize(maxSize).recordStats().build();
        return ds -> new RdfDataSourceWithSimpleCache(ds, cache);
    }

    public static RdfDataSourceTransform macros(Map<String, UserDefinedFunctionDefinition> udfRegistry) {
        return dataSource -> RdfDataSources.wrapWithMacros(dataSource, udfRegistry);
    }
}
