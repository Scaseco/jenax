package org.aksw.jenax.arq.util.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;

public class TypeMapperUtils {
    /**
     * Find all RDFDatatypes in a TypeMapper whose backing Java type is a
     * sub class (reflexive) of 'cls'.
     *
     * The motivation for implementing this method was to detect that xsd:string (backed by String)
     * is a suitable type for wherever CharSequence is required. However, the TypeMapper (by default)
     * already also contains a mapping from xsd:normalizedString to String. Hence, for this use case
     * this method is not suitable because the result is not unique.
     */
    public static List<RDFDatatype> getTypesBySubClassOf(TypeMapper tm, Class<?> cls) {
        List<RDFDatatype> result;
        RDFDatatype cand = tm.getTypeByClass(cls);

        if (cand == null) {
            result = new ArrayList<>();
            Iterator<RDFDatatype> it = tm.listTypes();
            while (it.hasNext()) {
                RDFDatatype dtype = it.next();
                Class<?> dtypeCls = dtype.getJavaClass();
                if (dtypeCls != null) {
                    if (ClassUtils.isAssignable(dtypeCls, cls)) {
                        result.add(dtype);
                    }
                }
            }
        } else {
            result = Collections.singletonList(cand);
        }
        return result;
    }

//    public static void main(String[] args) {
//        System.out.println(getTypesBySubClassOf(TypeMapper.getInstance(), CharSequence.class));
//    }
}
