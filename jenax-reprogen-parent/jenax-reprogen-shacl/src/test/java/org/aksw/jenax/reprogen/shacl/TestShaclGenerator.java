package org.aksw.jenax.reprogen.shacl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchemaFromPropertyShape;
import org.aksw.jena_sparql_api.schema.SHAnnotatedClass;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.util.SHACLSystemModel;
import org.topbraid.shacl.util.SHACLUtil;

public class TestShaclGenerator {

    // @ResourceView(TestResource.class)
    // @RdfType
    @ResourceView
    public static interface TestResourceDefault
        extends Resource
    {
        @Iri("rdfs:label")
        String getString();

        @IriType
        @Iri("rdfs:seeAlso")
        TestResourceDefault setIri(String str);

        @Iri("owl:maxCardinality")
        Integer getInteger();

        @Iri("eg:stringList")
        TestResourceDefault setList(List<String> strs);

        @Iri("eg:set")
        Set<String> getItems();

        @Iri("eg:set")
        String getRandomItem();

//        @Iri("eg:dynamicSet")
//        <T> Collection<T> getDynamicSet(Class<T> clazz);
        //TestResource setDynamicSet(Iterable<T> items);

//        @Iri("eg:simpleMap")
//        Map<String, Object> getSimpleMap();

        @Iri("eg:dateTime")
        XSDDateTime getDateTime();
        TestResourceDefault setDateTime();
    }


    @Test
    public void test1() {
        JenaSystem.init();
        SHFactory.ensureInited();
        JenaPluginUtils.registerResourceClasses(NodeSchemaFromNodeShape.class, PropertySchemaFromPropertyShape.class, SHAnnotatedClass.class);

        JenaPluginUtils.registerResourceClasses(TestResourceDefault.class);
        ModelFactory.createDefaultModel().createResource().as(TestResourceDefault.class);

        Resource r = ShaclGenerator.create(TestResourceDefault.class);
        RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_PRETTY);
    }
}
