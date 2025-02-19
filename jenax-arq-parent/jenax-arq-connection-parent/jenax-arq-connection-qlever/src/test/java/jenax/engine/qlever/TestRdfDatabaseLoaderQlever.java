package jenax.engine.qlever;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;
import org.aksw.jenax.engine.qlever.RdfDatabaseBuilderQlever;
import org.aksw.jenax.engine.qlever.SystemUtils;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.exec.SysRuntimeWrapperBase;
import org.junit.Ignore;
import org.junit.Test;


public class TestRdfDatabaseLoaderQlever {

    @Test
    public void testRegistry() {

    }

    @Test
    @Ignore
    public void testSys() throws Exception {
        String str = SystemUtils.which("lbzip2");
        System.out.println(str);

        System.out.println(SystemUtils.getCommandOutput("/bin/bash", "-c", "lbzip2 -h"));
    }

    @Test
    @Ignore
    public void testSystemBzip2() throws Exception {
        Path outputPath = Path.of("/tmp/foobar");
        Files.createDirectories(outputPath);

         RDFDatabase database = new RdfDatabaseBuilderQlever()
            .setOutputFolder(outputPath)
            .setIndexName("test")
            .addPath("/home/raven/.m2/repository/org/aksw/data/text2sparql/2025/dbpedia/1.0.0/dbpedia-1.0.0-dbpedia_2015-10.nt")
            .addPath("/home/raven/.m2/repository/dcat/org/aksw/moin/moin/1.20220502.0-1/moin-1.20220502.0-1-dcat.ttl.bz2")
            .addPath("/home/raven/.m2/repository/org/aksw/data/text2sparql/2025/dbpedia/1.0.0/dbpedia-1.0.0-instance_types_transitive_es.ttl.bz2")
            .addPath("/home/raven/.m2/repository/org/aksw/data/text2sparql/2025/dbpedia/1.0.0/dbpedia-1.0.0-infobox_properties_es.ttl.bz2", "http://info.box/properties")

            // .addPath("/home/raven/.m2/repository/dcat/org/aksw/moin/moin/1.20220502.0-1/moin-1.20220502.0-1-dcat.ttl.bz2")
            //.addPath("/home/raven/Projects/Eclipse/jena-sparql-api-parent/jena-sparql-api-concepts/src/main/resources/dataset-fp7.ttl")
//            .addPath("/home/raven/Datasets/fp7_ict_project_partners_database_2007_2011.nt.bz2")
//            // .addPath("/home/raven/Datasets/fp7_ict_project_partners_database_2007_2011.nt.bz2")
//            .addPath("/home/raven/tmp/codec-test/fp7_ict_project_partners_database_2007_2011.nt.bz2.gz")
            .build();

        System.out.println(database);
    }

    @Test
    @Ignore
    public void testJavaBzip2() throws Exception {
        Path outputPath = Path.of("/tmp/foobar");
        Files.createDirectories(outputPath);

        SysRuntime core = SysRuntimeImpl.forCurrentOs();
        // Set up a system runtime that does not provide bzip2
        // XXX A builder with an .excludeCommand() method would be a lot nicer.
        SysRuntime runtime = new SysRuntimeWrapperBase<>(core) {
            @Override
            public String which(String cmdName) throws IOException, InterruptedException {
                boolean isExcluded = Set.of("lbzip2", "bzip2", "pbzip2").contains(cmdName);
                String result = isExcluded ? null : super.which(cmdName);
                return result;
            }
        };

        RDFDatabase database = new RdfDatabaseBuilderQlever()
            .setSysRuntime(runtime)
            .setOutputFolder(outputPath)
            .setIndexName("test")
            .addPath("/home/raven/.m2/repository/dcat/org/aksw/moin/moin/1.20220502.0-1/moin-1.20220502.0-1-dcat.ttl.bz2")
            .addPath("/home/raven/.m2/repository/org/aksw/data/text2sparql/2025/dbpedia/1.0.0/dbpedia-1.0.0-instance_types_transitive_es.ttl.bz2")
            .addPath("/home/raven/.m2/repository/org/aksw/data/text2sparql/2025/dbpedia/1.0.0/dbpedia-1.0.0-infobox_properties_es.ttl.bz2")
            //.addPath("/home/raven/Projects/Eclipse/jena-sparql-api-parent/jena-sparql-api-concepts/src/main/resources/dataset-fp7.ttl")
//            .addPath("/home/raven/Datasets/fp7_ict_project_partners_database_2007_2011.nt.bz2")
//            // .addPath("/home/raven/Datasets/fp7_ict_project_partners_database_2007_2011.nt.bz2")
//            .addPath("/home/raven/tmp/codec-test/fp7_ict_project_partners_database_2007_2011.nt.bz2.gz")
            .build();

        System.out.println(database);
    }

}
