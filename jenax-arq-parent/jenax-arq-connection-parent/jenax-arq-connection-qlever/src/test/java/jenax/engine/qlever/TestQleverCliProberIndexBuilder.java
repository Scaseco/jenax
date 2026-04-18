package jenax.engine.qlever;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import org.aksw.jenax.engine.qlever.QleverCliProberIndexBuilder;
import org.aksw.jenax.engine.qlever.QleverCliProberIndexBuilder.CliType;

public class TestQleverCliProberIndexBuilder {
    @Test
    public void testIndexBuilderMain() {
        String imageName = "adfreiburg/qlever:commit-a307781";
        Optional<CliType> probeResult = QleverCliProberIndexBuilder.probe(imageName);
        assertEquals(CliType.IndexBuilderMain, probeResult.get());
    }

    @Test
    public void testQleverIndex() {
        String imageName = "adfreiburg/qlever:commit-b7486a3";
        Optional<CliType> probeResult = QleverCliProberIndexBuilder.probe(imageName);
        assertEquals(CliType.QleverIndex, probeResult.get());
    }
}
