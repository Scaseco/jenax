package org.aksw.jenax.arq.datatype.plugin;

import org.aksw.jenax.arq.datatype.RDFDatatypeExpr;
import org.aksw.jenax.arq.datatype.RDFDatatypeQuery;
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
        typeMapper.registerDatatype(RDFDatatypeExpr.INSTANCE);
        typeMapper.registerDatatype(RDFDatatypeQuery.INSTANCE);
    }
}
