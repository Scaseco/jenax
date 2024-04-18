package org.aksw.jenax.io.rowset.core;

import static org.apache.jena.riot.resultset.ResultSetLang.RS_CSV;
import static org.apache.jena.riot.resultset.ResultSetLang.RS_TSV;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.jenax.io.rowset.csv.RowSetStreamWriterCSV;
import org.aksw.jenax.io.rowset.csv.RowSetStreamWriterTSV;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.rowset.RowSetReaderFactory;

public class RowSetStreamWriterRegistry {

    private static Map<Lang, RowSetStreamWriterFactory> registry = new ConcurrentHashMap<>();

    static {
        init();
    }

    /** Lookup a {@link Lang} to get the registered {@link RowSetReaderFactory} (or null) */
    public static RowSetStreamWriterFactory getFactory(Lang lang) {
        Objects.requireNonNull(lang);
        return registry.get(lang);
    }

    public static boolean isRegistered(Lang lang) {
        Objects.requireNonNull(lang);
        return registry.containsKey(lang);
    }

    /** Register a {@link RowSetReaderFactory} for a {@link Lang} */
    public static void register(Lang lang, RowSetStreamWriterFactory factory) {
        Objects.requireNonNull(lang);
        Objects.requireNonNull(factory);
        registry.put(lang, factory);
    }

    private static boolean initialized = false;
    public static void init() {
        if (!initialized) {
            synchronized (RowSetStreamWriterRegistry.class) {
                if (!initialized) {
    //              register(RS_XML,        RowSetWriterXML.factory);
    //              register(RS_JSON,       RowSetWriterJSON.factory);

                  register(RS_CSV,        RowSetStreamWriterCSV.factory);
                  register(RS_TSV,        RowSetStreamWriterTSV.factory);

    //              register(RS_Thrift,     RowSetWriterThrift.factory);
    //              register(RS_Protobuf,   RowSetWriterProtobuf.factory);
          //
    //              register(RS_Text,       RowSetWriterText.factory);
    //              register(RS_None,       RowSetWriterNone.factory);

                    initialized = true;
                }
            }
        }
    }
}
