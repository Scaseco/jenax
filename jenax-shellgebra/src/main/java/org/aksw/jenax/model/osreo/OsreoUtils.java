package org.aksw.jenax.model.osreo;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;

public class OsreoUtils {
    public static List<Shell> listShells(Model model) {
        List<Shell> result = model.listResourcesWithProperty(RDF.type, OSREO.Shell)
            .mapWith(r -> r.as(Shell.class))
            .toList();
        return result;
    }

    public static List<LocatorCommand> listLocatorCommands(Model model) {
        List<LocatorCommand> result = model.listResourcesWithProperty(RDF.type, OSREO.LocatorComand)
            .mapWith(r -> r.as(LocatorCommand.class))
            .toList();
        return result;
    }

}
