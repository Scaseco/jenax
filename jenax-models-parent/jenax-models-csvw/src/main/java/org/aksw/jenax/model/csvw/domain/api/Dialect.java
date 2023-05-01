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
    @Override
    String  getCommentPrefix();
    Dialect setCommentPrefix(String commentPrefix);

    @Iri(CsvwTerms.delimiter)
    @Override
    String  getDelimiter();
    Dialect setDelimiter(String delimiter);

    @Iri(CsvwTerms.doubleQuote)
    @Override
    Boolean isDoubleQuote();
    Dialect setDoubleQuote(Boolean doubleQuote);

    @Iri(CsvwTerms.encoding)
    @Override
    String  getEncoding();
    Dialect setEncoding(String encoding);

    @Iri(CsvwTerms.header)
    @Override
    Boolean getHeader();
    Dialect setHeader(Boolean header);

    @Iri(CsvwTerms.headerRowCount)
    @Override
    Long    getHeaderRowCount();
    Dialect setHeaderRowCount(Long headerRowCount);

    @Iri(CsvwTerms.lineTerminators)
    @Override
    String  getLineTerminators();
    Dialect setLineTerminators(String lineTerminators);

    @Iri(CsvwTerms.quoteChar)
    @Override
    String  getQuoteChar();
    Dialect setQuotechar(String quoteChar);

    @Iri(CsvwTerms.quoteEscapeChar)
    @Override
    String getQuoteEscapeChar();
    Dialect setQuoteEscapeChar(String quoteEscapeChar);

    @Iri(CsvwTerms.skipBlankRows)
    @Override
    Boolean getSkipBlankRows();
    Dialect setSkipBlankRows(Boolean skipBlankRows);

    @Iri(CsvwTerms.skipColumns)
    @Override
    Long    getSkipColumns();
    Dialect setskipColumns(Long skipColumns);

    @Iri(CsvwTerms.skipInitialSpace)
    @Override
    Boolean getSkipInitialSpace();
    Dialect setSkipInitialSpace(Boolean skipInitialSpace);

    @Iri(CsvwTerms.skipRows)
    @Override
    Long    getSkipRows();
    Dialect setSkipRows(Long skipRows);

    @Iri(CsvwTerms.trim)
    @Override
    String getTrim();
    Dialect setTrim(String trim);
}
