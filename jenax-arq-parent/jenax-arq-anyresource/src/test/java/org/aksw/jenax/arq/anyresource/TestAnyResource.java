package org.aksw.jenax.arq.anyresource;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceRequiredException;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

public class TestAnyResource {
    /** Test that demonstrates/ensures that usually literals cannot be cast as resources */
    @Test(expected = ResourceRequiredException.class)
    public void test01() {
        ModelFactory.createDefaultModel().createLiteral("test").as(Resource.class);
    }

    /** Test that demonstrates/ensures that literals can be cast to AnyResource. */
    @Test
    public void test02() {
        Resource s = ModelFactory.createDefaultModel().createLiteral("test").as(AnyResource.class);
        s.addProperty(RDFS.comment, "A literal wrapped as a resource");
        Assert.assertEquals(1, s.getModel().size());
        // s.getModel().getGraph().find().forEach(System.out::println);
    }
}
