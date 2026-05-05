package org.aksw.jenax.arq.anyresource;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceRequiredException;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAnyResource {
    /** Test that demonstrates/ensures that usually literals cannot be cast as resources */
    @Test
    public void test01() {
        Assertions.assertThrows(ResourceRequiredException.class, () -> {
            ModelFactory.createDefaultModel().createLiteral("test").as(Resource.class);
        });
    }

    /** Test that demonstrates/ensures that literals can be cast to AnyResource. */
    @Test
    public void test02() {
        Resource s = ModelFactory.createDefaultModel().createLiteral("test").as(AnyResource.class);
        s.addProperty(RDFS.comment, "A literal wrapped as a resource");
        Assertions.assertEquals(1, s.getModel().size());
        // s.getModel().getGraph().find().forEach(System.out::println);
    }
}
