package org.aksw.jenax.model.shacl.template.domain;

public class ShaclTemplateTerms {
    // ISSUE template can be both used as a property (resource that has a template) and as a datatype (literal that is a template)
    // One option would be to use e.g. hasTemplate and TemplateLiteral (similar to WKTLiteral)

    // Another solution is that RDF predicates are outside of the scope of norse.
    // The original concept for norse is to use norse:DATATYPE for a datatype and then have
    // norse:DATATYPE.method for the various datatype related methods
    public static final String template = "http://www.example.org/template";

    /** Datatype for literals that are pebble template strings */
    public static final String pebble = "https://w3id.org/aksw/norse#template.pebble";
}
