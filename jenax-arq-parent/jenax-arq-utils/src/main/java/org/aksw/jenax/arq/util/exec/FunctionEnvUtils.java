package org.aksw.jenax.arq.util.exec;

import java.util.Optional;

import org.aksw.jenax.arq.util.prefix.PrefixMap2;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapAdapter;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.function.FunctionEnv;

public class FunctionEnvUtils {
    /** Returns a prefix map that is the union of those of the active statement and those of the dataset */
    public static PrefixMap getActivePrefixes(FunctionEnv env) {
        PrefixMap datasetPrefixes = Optional.ofNullable(env.getDataset().prefixes()).orElse(PrefixMapFactory.emptyPrefixMap());
        Prologue prologue = (Prologue)env.getContext().get(ARQConstants.sysCurrentQuery);
        PrefixMap stmtPrefixes = Optional.ofNullable(prologue).map(Prologue::getPrefixMapping).map(x -> (PrefixMap)new PrefixMapAdapter(x)).orElse(PrefixMapFactory.emptyPrefixMap());
        PrefixMap result = new PrefixMap2(datasetPrefixes, stmtPrefixes);
        return result;
    }
}
