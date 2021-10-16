package org.aksw.jenax.stmt.parser.update;

import java.util.function.Supplier;

import org.aksw.jenax.arq.util.prefix.PrefixMappingTrie;
import org.aksw.jenax.arq.util.prologue.PrologueUtils;
import org.aksw.jenax.stmt.core.StmtSupplierBase;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.apache.jena.update.UpdateRequest;

public class UpdateSupplierImpl
    extends StmtSupplierBase
    implements Supplier<UpdateRequest>
{
    public UpdateSupplierImpl() {
        this(null);
    }

    public UpdateSupplierImpl(Prologue prologue) {
        this(prologue, null);
    }

    public UpdateSupplierImpl(Prologue prologue, String baseURI) {
        this(prologue, baseURI, null);
    }

    public UpdateSupplierImpl(Prologue prologue, String baseURI, PrefixMapping sharedPrefixes) {
        super(prologue, baseURI, sharedPrefixes);
    }


    @Override
    public UpdateRequest get() {
        PrefixMapping prefixMapping = new PrefixMappingTrie();
        if (sharedPrefixes != null) {
            prefixMapping = new PrefixMapping2(sharedPrefixes, prefixMapping);
        }

        UpdateRequest result = new UpdateRequest();
        result.setPrefixMapping(prefixMapping);

        if (prologue != null) {
            PrologueUtils.configure(result, prologue, baseURI);
        }

        return result;
    }


}
