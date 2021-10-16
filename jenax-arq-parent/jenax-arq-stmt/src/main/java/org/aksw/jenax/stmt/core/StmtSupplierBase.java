package org.aksw.jenax.stmt.core;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;

public class StmtSupplierBase
{
    protected Prologue prologue;
    protected String baseURI;
    protected PrefixMapping sharedPrefixes;

    public StmtSupplierBase(Prologue prologue, String baseURI, PrefixMapping sharedPrefixes) {
        super();
        this.prologue = prologue;
        this.baseURI = baseURI;
        this.sharedPrefixes = sharedPrefixes;
    }
}
