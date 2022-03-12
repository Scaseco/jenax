package org.aksw.jenax.model.csvw.domain.api;

import org.aksw.commons.model.csvw.term.CsvwTerms;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

/** Resource view of the Dialect class according to
 *  https://www.w3.org/ns/csvw#class-definitions
 *
 *  All attributes mapped.
 */
@ResourceView
public interface Dialect
    extends Resource, org.aksw.commons.model.csvw.domain.api.Dialect
{
    @Iri(CsvwTerms.commentPrefix)
    String  getCommentPrefix();
    Dialect setCommentPrefix(String commentPrefix);

    @Iri(CsvwTerms.delimiter)
    String  getDelimiter();
    Dialect setDelimiter(String delimiter);

    @Iri(CsvwTerms.doubleQuote)
    Boolean isDoubleQuote();
    Dialect setDoubleQuote(Boolean doubleQuote);

    @Iri(CsvwTerms.encoding)
    String  getEncoding();
    Dialect setEncoding(String encoding);

    @Iri(CsvwTerms.header)
    Boolean getHeader();
    Dialect setHeader(String header);

    @Iri(CsvwTerms.headerRowCount)
    Long    getHeaderRowCount();
    Dialect setHeaderRowCount(String headerRowCount);

    @Iri(CsvwTerms.lineTerminators)
    String  getLineTerminators();
    Dialect setLineTerminators(String lineTerminators);

    @Iri(CsvwTerms.quoteChar)
    String  getQuoteChar();
    Dialect setQuotechar(String quoteChar);

    @Iri(CsvwTerms.skipBlankRows)
    Boolean getSkipBlankRows();
    Dialect setSkipBlankRows(Boolean skipBlankRows);

    @Iri(CsvwTerms.skipColumns)
    Long    getSkipColumns();
    Dialect setskipColumns(Long skipColumns);

    @Iri(CsvwTerms.skipInitialSpace)
    Boolean getSkipInitialSpace();
    Dialect setSkipInitialSpace(Boolean skipInitialSpace);

    @Iri(CsvwTerms.skipRows)
    Long    getSkipRows();
    Dialect setSkipRows(Long skipRows);

    @Iri(CsvwTerms.trim)
    String getTrim();
    Dialect setTrim(String trim);
}
