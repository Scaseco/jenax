package org.aksw.dcat.jena.domain.api;

import java.util.Collections;
import java.util.Set;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;

import com.google.common.collect.Iterables;

public class DcatUtils {
    public static String getFirstDownloadUrlFromDistribution(Resource dcatDistribution) {
        String result = ResourceUtils.listPropertyValues(dcatDistribution, DCAT.downloadURL).toList().stream()
                .filter(RDFNode::isURIResource)
                .map(RDFNode::asResource)
                .map(Resource::getURI)
                .sorted()
                .findFirst()
                .orElse(null);

        return result;
    }

    public static String getFirstDownloadUrl(Resource dcatDataset) {
        String result = ResourceUtils.listPropertyValues(dcatDataset, DCAT.distribution, Resource.class).toList().stream()
            .flatMap(d -> ResourceUtils.listPropertyValues(d, DCAT.downloadURL).toList().stream())
            .filter(RDFNode::isURIResource)
            .map(RDFNode::asResource)
            .map(Resource::getURI)
            .sorted()
            .findFirst()
            .orElse(null);

        return result;
     }



    /**
     * Attempt to resolve the given resource to the set of related distributions.
     * The result of the first matching rule is returned:
     * - The singleton set of the given resource itself if it appears as a distribution of a dataset
     * - The set of distributions having the given resource as a download URL.
     * - The set of distributions of the resource interpreted as a dcat dataset
     *
     * @param res
     * @return
     */
    public static Set<? extends DcatDistribution> resolveDistributions(Resource res) {
        Set<? extends DcatDistribution> result;

        DcatDistribution dist = res.as(DcatDistribution.class);
        if (dist.getDcatDatasets(DcatDataset.class).isEmpty()) {
            DcatDownloadUrl durl = res.as(DcatDownloadUrl.class);
            result = durl.getDistributions();
            if (result.isEmpty()) {
                DcatDataset ds = res.as(DcatDataset.class);
                result = ds.getDistributions();
            }
        } else {
            result = Collections.singleton(dist);
        }

        return result;
    }

    public static DcatDistribution resolveDistribution(Resource res) {
        Set<? extends DcatDistribution> candidates = resolveDistributions(res);
        DcatDistribution result = Iterables.getOnlyElement(candidates);
        return result;
    }


    public static Resource getRelatedId(DcatDistribution distribution, DcatIdType idType) {
        Set<? extends Resource> candidates = getRelatedIds(distribution, idType);
        Resource result = Iterables.getOnlyElement(candidates);
        return result;
    }

    public static Set<? extends Resource> getRelatedIds(DcatDistribution distribution, DcatIdType idType) {
        Set<? extends Resource> result;
        switch (idType) {
        case DATASET:
            result = distribution.getDcatDatasets(DcatDataset.class);
            break;
        case DISTRIBUTION:
            result = Collections.singleton(distribution);
            break;
        case FILE:
            result = distribution.getDownloadUrlResources();
            break;
        default:
            throw new IllegalStateException("Unknown id type: " + idType);
        }
        return result;
    }
}
