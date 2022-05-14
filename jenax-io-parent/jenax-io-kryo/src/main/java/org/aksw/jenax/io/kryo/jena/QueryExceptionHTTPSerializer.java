package org.aksw.jenax.io.kryo.jena;

import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class QueryExceptionHTTPSerializer
	extends Serializer<QueryExceptionHTTP>
{
    @Override
    public void write(Kryo kryo, Output output, QueryExceptionHTTP obj) {
        kryo.writeObject(output, obj.getStatusCode());
        kryo.writeObject(output, obj.getMessage());
        kryo.writeClassAndObject(output, obj.getCause());
    }

    @Override
    public QueryExceptionHTTP read(Kryo kryo, Input input, Class<QueryExceptionHTTP> objClass) {
        Integer statusCode = kryo.readObject(input, Integer.class);
        String message = kryo.readObject(input, String.class);
        Throwable cause = (Throwable)kryo.readClassAndObject(input);

        return new QueryExceptionHTTP(statusCode, message, cause);
    }
}