package org.aksw.jena_sparql_api.sparql.ext.util;

import org.aksw.jena_sparql_api.sparql.ext.binding.JenaExtensionBinding;
import org.aksw.jena_sparql_api.sparql.ext.collection.array.JenaExtensionArray;
import org.aksw.jena_sparql_api.sparql.ext.collection.base.JenaExtensionCollection;
import org.aksw.jena_sparql_api.sparql.ext.collection.set.JenaExtensionSet;
import org.aksw.jena_sparql_api.sparql.ext.csv.JenaExtensionCsv;
import org.aksw.jena_sparql_api.sparql.ext.datatypes.JenaExtensionDuration;
import org.aksw.jena_sparql_api.sparql.ext.fs.JenaExtensionFs;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaExtensionJson;
import org.aksw.jena_sparql_api.sparql.ext.number.JenaExtensionNumber;
import org.aksw.jena_sparql_api.sparql.ext.prefix.JenaExtensionPrefix;
import org.aksw.jena_sparql_api.sparql.ext.str.JenaExtensionString;
import org.aksw.jena_sparql_api.sparql.ext.sys.JenaExtensionSys;
import org.aksw.jena_sparql_api.sparql.ext.url.JenaExtensionUrl;
import org.aksw.jena_sparql_api.sparql.ext.xml.JenaExtensionXml;
import org.apache.jena.shared.PrefixMapping;

public class JenaExtensionUtil {
//    public static void registerAll() {
//        throw new RuntimeException("Not yet implemented, because we need to support configuration options");
//    }


    public static void addPrefixes(PrefixMapping pm) {
        JenaExtensionJson.addPrefixes(pm);
        JenaExtensionCsv.addPrefixes(pm);
        JenaExtensionXml.addPrefixes(pm);
        JenaExtensionUrl.addPrefixes(pm);
        JenaExtensionFs.addPrefixes(pm);
        JenaExtensionSys.addPrefixes(pm);
        JenaExtensionCollection.addPrefixes(pm);
        JenaExtensionArray.addPrefixes(pm);
        JenaExtensionSet.addPrefixes(pm);
        JenaExtensionJson.addPrefixes(pm);

        JenaExtensionBinding.addPrefixes(pm);

        JenaExtensionDuration.addPrefixes(pm);
        // JenaExtensionOsrm.
        JenaExtensionArray.addPrefixes(pm);
        JenaExtensionSet.addPrefixes(pm);
        JenaExtensionCollection.addPrefixes(pm);
        JenaExtensionPrefix.addPrefixes(pm);
        JenaExtensionString.addPrefixes(pm);
        JenaExtensionNumber.addPrefixes(pm);

        // JenaExtensionsMvn
    }
}
