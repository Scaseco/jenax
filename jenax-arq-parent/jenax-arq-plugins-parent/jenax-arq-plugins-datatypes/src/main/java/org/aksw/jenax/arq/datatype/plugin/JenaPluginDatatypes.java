package org.aksw.jenax.arq.datatype.plugin;

import org.aksw.jenax.arq.datatype.RDFDatatypeBinding;
import org.aksw.jenax.arq.datatype.RDFDatatypeExpr;
import org.aksw.jenax.arq.datatype.RDFDatatypeNodeList;
import org.aksw.jenax.arq.datatype.RDFDatatypeNodeSet;
import org.aksw.jenax.arq.datatype.RDFDatatypeQuery;
import org.aksw.jenax.arq.datatype.lambda.RDFDatatypeLambda;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginDatatypes
    implements JenaSubsystemLifecycle {

    public void start() {
        init();
    }

    @Override
    public void stop() {
    }


    public static void init() {
        TypeMapper typeMapper = TypeMapper.getInstance();
        typeMapper.registerDatatype(RDFDatatypeExpr.get());
        // typeMapper.registerDatatype(RDFDatatypeExprList.INSTANCE);
        typeMapper.registerDatatype(RDFDatatypeQuery.get());
        typeMapper.registerDatatype(RDFDatatypeBinding.get());

        typeMapper.registerDatatype(RDFDatatypeNodeList.get());
        typeMapper.registerDatatype(RDFDatatypeNodeSet.get());

        typeMapper.registerDatatype(RDFDatatypeLambda.get());
    }

    // Initialize datatypes before sparql extensions
    @Override
    public int level() { return 9900; }
}
