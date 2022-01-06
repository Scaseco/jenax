package org.aksw.dcat.jena.term;

/**
 * Constants for the W3C Data Catalog Vocabulary.
 *
 * @see <a href="https://www.w3.org/TR/vocab-dcat/">Data Catalog Vocabulary</a>
 */
public class DcatTerms {
    public static final String NS = org.apache.jena.vocabulary.DCAT.NS;

    // Classes
    public static final String Catalog = NS + "Catalog";
    public static final String CatalogRecord = NS + "CatalogRecord";
    public static final String Dataset = NS + "Dataset";
    public static final String Distribution = NS + "Distribution";

    // Properties
    public static final String accessURL = NS + "accessURL";
    public static final String byteSize = NS + "byteSize";
    public static final String contactPoint = NS + "contactPoint";
    public static final String dataset = NS + "dataset";
    public static final String distribution = NS + "distribution";
    public static final String downloadURL = NS + "downloadURL";
    public static final String keyword = NS + "keyword";
    public static final String landingPage = NS + "landingPage";
    public static final String mediaType = NS + "mediaType";
    public static final String record = NS + "record";
    public static final String theme = NS + "theme";
    public static final String themeTaxonomy = NS + "themeTaxonomy";
}
