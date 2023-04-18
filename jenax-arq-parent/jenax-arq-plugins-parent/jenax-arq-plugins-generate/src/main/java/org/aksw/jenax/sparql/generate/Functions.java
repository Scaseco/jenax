package org.aksw.jenax.sparql.generate;

import fr.mines_stetienne.ci.sparql_generate.function.library.*;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sys.JenaSystem;

class Functions {
    static {
        JenaSystem.init();
    }
    public static void register() {
        FunctionRegistry functionRegistry = FunctionRegistry.get();
        functionRegistry.put(FUN_SplitAtPostion.URI, FUN_SplitAtPostion.class);
        functionRegistry.put(FUN_regex.URI, FUN_regex.class);
        functionRegistry.put(FUN_dateTime.URI, FUN_dateTime.class);
        functionRegistry.put(FUN_Property.URI, FUN_Property.class);
        functionRegistry.put(FUN_CamelCase.URI, FUN_CamelCase.class);
        functionRegistry.put(FUN_MixedCase.URI, FUN_MixedCase.class);
        functionRegistry.put(FUN_TitleCase.URI, FUN_TitleCase.class);
        functionRegistry.put(FUN_PrefixedIRI.URI, FUN_PrefixedIRI.class);
        functionRegistry.put(FUN_Select_Call_Template.URI, FUN_Select_Call_Template.class);
        functionRegistry.put(FUN_Log.URI, FUN_Log.class);

        //new fr.mines_stetienne.ci.sparql_generate.binary.FunctionLoader().load();

//        final ServiceLoader<FunctionLoader> functionLoaders = ServiceLoader.load(FunctionLoader.class);
//        functionLoaders.forEach((loader) -> {
//            loader.load(functionRegistry);
//        });

        functionRegistry.put(ST_Call_Template.URI, ST_Call_Template.class);
        functionRegistry.put(ST_Decr.URI, ST_Decr.class);
        functionRegistry.put(ST_Incr.URI, ST_Incr.class);
        functionRegistry.put(ST_Concat.URI, ST_Concat.class);
        functionRegistry.put(ST_Format.URI, ST_Format.class);
    }
}