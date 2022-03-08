package org.aksw.jenax.io.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.curator.shaded.com.google.common.base.Objects;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.rw.RowSetReaderJSON;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;

import com.google.common.base.Stopwatch;

public class TestRowSetJson {

    public static void main(String[] args) throws MalformedURLException, IOException {
        // TODO Read test data from class path resource
        byte[] data;
        try (InputStream in = new URL("http://moin.aksw.org/sparql?query=SELECT%20*%20{%20?s%20?p%20?o%20}").openStream()) {
            data = IOUtils.toByteArray(in);
        }

        Context cxt = ARQ.getContext().copy();
        cxt.setTrue(ARQ.inputGraphBNodeLabels);

        System.out.println("Data retrieved");
        RowSet actuals = RowSetJson.createBuffered(new ByteArrayInputStream(data), cxt);
        RowSet expecteds = RowSetReaderJSON.factory.create(ResultSetLang.RS_JSON).read(new ByteArrayInputStream(data), cxt);

        boolean isOk = true;
        while (actuals.hasNext() && expecteds.hasNext()) {
            Binding a = actuals.next();
            Binding b = expecteds.next();

            if (!Objects.equal(a, b)) {
                System.out.println(String.format("Difference at %d/%d: %s != %s",
                        actuals.getRowNumber(), expecteds.getRowNumber(), a , b));

                isOk = false;
            }
        }

        System.out.println("Success is " + isOk);

        // boolean isIsomorphic = ResultSetCompare.isomorphic(actuals, expecteds);
        // System.out.println("Isomorphic: " + isIsomorphic);

        actuals.close();
        expecteds.close();
    }

    public static void mainOld(String[] args) throws MalformedURLException, IOException {
        RowSet rs = RowSetJson.createBuffered(
                new URL("http://moin.aksw.org/sparql?query=SELECT%20*%20{%20?s%20?p%20?o%20}").openStream(),
                null);

        System.out.println("ResultVars: " + rs.getResultVars());

        Stopwatch sw = Stopwatch.createStarted();
        for (int i = 0; i < 1000000 && rs.hasNext(); ++i) {
            rs.next();
            // System.out.println(rs.next());
        }
        System.out.println("Elapsed: " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f + "s - final row: " + rs.getRowNumber());
        rs.close();
    }
}
