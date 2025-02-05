package jenax.engine.qlever;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import jenax.engine.qlever.QleverLoader.QleverDbFileSet;


public class TestQleverLoader {

    @Test
    public void testRegistry() {

    }

    @Test
    public void testSys() throws Exception {
        String str = SystemUtils.which("lbzip2");
        System.out.println(str);

        System.out.println(SystemUtils.getCommandOutput("/bin/bash", "-c", "lbzip2 -h"));
    }

    @Test
    public void test() throws Exception {
        Path outputPath = Path.of("/tmp/foobar");
        Files.createDirectories(outputPath);

        QleverDbFileSet fileSet = new QleverLoader()
            .setOutputFolder(outputPath)
            .setIndexName("test")
            //.addPath("/home/raven/Projects/Eclipse/jena-sparql-api-parent/jena-sparql-api-concepts/src/main/resources/dataset-fp7.ttl")
            .addPath("/home/raven/Datasets/fp7_ict_project_partners_database_2007_2011.nt.bz2")
            // .addPath("/home/raven/Datasets/fp7_ict_project_partners_database_2007_2011.nt.bz2")
            .addPath("/home/raven/tmp/codec-test/fp7_ict_project_partners_database_2007_2011.nt.bz2.gz")
            .build();

        System.out.println(fileSet);
    }
}
