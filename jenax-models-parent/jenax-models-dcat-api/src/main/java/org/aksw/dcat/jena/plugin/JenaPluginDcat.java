package org.aksw.dcat.jena.plugin;

import org.aksw.dcat.jena.conf.api.DcatRepoConfig;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.DcatDownloadUrl;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat.mgmt.api.DataProject;
import org.aksw.dcat.mgmt.api.FoafDepiction;
import org.aksw.dcat.mgmt.api.RdfsComment;
import org.aksw.dcat.mgmt.api.RdfsLabel;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;


public class JenaPluginDcat
    implements JenaSubsystemLifecycle {

    public void start() {
        JenaPluginDcat.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
                DcatDataset.class,
                DcatDistribution.class,
                DcatDownloadUrl.class);

        JenaPluginUtils.registerResourceClasses(
                RdfsLabel.class,
                RdfsComment.class,
                FoafDepiction.class,
                DataProject.class,
                DcatRepoConfig.class,
                MavenEntity.class);
    }
}
