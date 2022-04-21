package org.aksw.difs.system.domain;

import java.util.Set;

import org.aksw.difs.sys.vocab.common.DIFSTerms;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * Interface capturing the configuration options of the
 * fs-rdf-store.
 *
 * @author raven
 *
 */
@ResourceView
public interface StoreDefinition
    extends Resource
{
    @HashId
    @Iri(DIFSTerms.storePath)
    String getStorePath();
    StoreDefinition setStorePath(String path);

    /** Whether to store all data in the file pointed to by store path; if false
     *  then graphs are mapped to different folders; default: false */
    @HashId
    @Iri(DIFSTerms.singleFile)
    Boolean isSingleFile();
    StoreDefinition setSingleFile(Boolean onOrOff);


    @HashId
    @Iri(DIFSTerms.indexPath)
    String getIndexPath();
    StoreDefinition setIndexPath(String path);


    @Iri(DIFSTerms.index)
    Set<IndexDefinition> getIndexDefinition();

    @Iri(DIFSTerms.allowEmptyGraphs)
    StoreDefinition setAllowEmptyGraphs(Boolean value);
    Boolean isAllowEmptyGraphs();

    /**
     * The heartbeat interval is a parameter common to all transactions
     * accessing the repository.
     *
     * @return
     */
    @Iri(DIFSTerms.heartbeatInterval)
    Long getHeartbeatInterval();
    StoreDefinition setHeartbeatInterval(Long heartbeatInterval);


    default StoreDefinition addIndex(String predicate, String folderName, Class<?> clazz) {
        return addIndex(NodeFactory.createURI(predicate), folderName, clazz);
    }

    default StoreDefinition addIndex(Node predicate, String folderName, Class<?> clazz) {
        IndexDefinition idx = getModel().createResource().as(IndexDefinition.class)
            .setPredicate(predicate)
            .setPath(folderName)
            .setMethod(clazz.getCanonicalName());

        getIndexDefinition().add(idx);

        return this;
    }
}
