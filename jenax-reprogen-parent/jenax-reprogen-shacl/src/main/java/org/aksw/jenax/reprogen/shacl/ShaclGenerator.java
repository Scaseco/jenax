package org.aksw.jenax.reprogen.shacl;

import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.beans.datatype.DataType;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchemaFromPropertyShape;
import org.aksw.jenax.reprogen.hashid.ClassDescriptor;
import org.aksw.jenax.reprogen.hashid.Metamodel;
import org.aksw.jenax.reprogen.hashid.PropertyDescriptor;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Path0;

public class ShaclGenerator {

    public static String classToIri(Class<?> cls) {
        return "java:" + cls.getName();
    }

    public static NodeSchemaFromNodeShape create(Class<?> clz) {
        return create(clz, ModelFactory.createDefaultModel());
    }

    public static NodeSchemaFromNodeShape create(Class<?> clz, Model model) {
        return create(clz, model, new HashMap<>());
    }

    public static NodeSchemaFromNodeShape create(
            Class<?> clz,
            Model model,
            Map<Class<?>, NodeSchemaFromNodeShape> map) {

        NodeSchemaFromNodeShape result = map.get(clz);
        if (result == null && Resource.class.isAssignableFrom(clz)) {
            String classIri = classToIri(clz);

            result = model.createResource(classIri + "#shape").as(NodeSchemaFromNodeShape.class);
            result.setTargetClass(model.createResource(classIri));
            map.put(clz, result);

            Metamodel metamodel = Metamodel.get();
            ClassDescriptor cd = metamodel.get(clz);

            if (cd == null) {
                throw new IllegalArgumentException("Class " + clz + " is not registered in the meta model");
            }

            // NodeSchemaFromNodeShape ns = targetNodeShape.as(NodeSchemaFromNodeShape.class);

            for (PropertyDescriptor pd : cd.getPropertyDescriptors()) {
                P_Path0 path = pd.getPath();

                PropertySchemaFromPropertyShape ps = result.createPropertySchema(path.getNode(), path.isForward());

                DataType type = pd.getTargetType();

                // NodeSchemaFromNodeShape targetSchema;

                Class<?> targetCls;

                Long minCount = null;
                Long maxCount = null;

                if (type.isScalarType()) {
                    targetCls = type.asScalarType().getJavaClass();

                    // TODO Add an isNullable() flag; if false then minCount is 1.
                    maxCount = 1l;


                } else if (type.isSetType()) {
                    // TODO Nested collections not supported yet
                    targetCls = type.asSetType().getItemType().asScalarType().getJavaClass();
                } else {
                    throw new RuntimeException("Only scalar and set types supported yet");
                }


                if (Resource.class.isAssignableFrom(targetCls)) {
                    ps.setSHClass(model.createResource(classToIri(targetCls)));
                    // tar create(javaClass, model, map);
                } else {
                    RDFDatatype dtype = TypeMapper.getInstance().getTypeByClass(targetCls);
                    if (dtype == null) {
                        throw new RuntimeException("No RDF datatype found for java class " + targetCls);
                    }
                    String dtypeIri = dtype.getURI();
                    ps.setDataTypeIri(dtypeIri);
                }

                ps
                    .setMinCount(minCount)
                    .setMaxCount(maxCount);
            }
        }

        return result;
    }
}
