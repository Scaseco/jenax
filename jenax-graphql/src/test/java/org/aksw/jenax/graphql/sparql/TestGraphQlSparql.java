package org.aksw.jenax.graphql.sparql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.graphql.api.GraphQlExecFactory;
import org.aksw.jenax.graphql.impl.common.GraphQlExecUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TestGraphQlSparql {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void testPokemon01() {
        String queryStr = getResourceAsString("graphql/test01-query.graphql");
        JsonElement expected = getResourceAsJson("graphql/test01-result.json", gson);

        Dataset ds = RDFDataMgr.loadDataset("pokedex.sample.ttl");
        RdfDataSource dataSource = RdfDataEngines.of(ds);
        GraphQlExecFactory gef = GraphQlExecFactoryOverSparql.autoConfEager(dataSource);
        JsonObject actual = GraphQlExecUtils.materialize(gef, queryStr);

        Assert.assertEquals(expected, actual);
    }

    private static JsonElement getResourceAsJson(String name, Gson gson) {
        JsonElement result = gson.fromJson(getResourceAsString(name), JsonElement.class);
        return result;
    }

    private static String getResourceAsString(String name) {
        String result;
        try {
            result = Resources.toString(
                    Resources.getResource(name), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
