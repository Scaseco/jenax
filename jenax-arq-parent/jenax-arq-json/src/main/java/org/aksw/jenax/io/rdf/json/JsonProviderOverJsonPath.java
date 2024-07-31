package org.aksw.jenax.io.rdf.json;

import java.math.BigDecimal;

import org.apache.jena.graph.Node;

public class JsonProviderOverJsonPath
    implements JsonProvider
{
    protected com.jayway.jsonpath.spi.json.JsonProvider delegate ;

    @Override
    public Object newObject() {
        return delegate.createMap();
    }

    @Override
    public boolean isObject(Object obj) {
        return delegate.isMap(obj);
    }

    @Override
    public Node getObjectId(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProperty(Object obj, String p, Object value) {
        delegate.setProperty(obj, p, value);
    }

    @Override
    public Object getProperty(Object obj, String p) {
        return delegate.getMapValue(obj, p);
    }

    @Override
    public void removeProperty(Object obj, String p) {
        delegate.removeProperty(obj, p);
    }

    @Override
    public void ensureValidJson(Object obj) {
        // delegate.isMap(obj) || delegate.isArray(obj) || delegate.nu
    }

    @Override
    public Object newArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isArray(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addElement(Object arr, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setElement(Object arr, int index, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeElement(Object arr, int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object newLiteral(String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object newLiteral(boolean value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object newLiteral(long value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object newLiteral(double value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object newLiteral(BigDecimal value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLiteral(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Node getLiteral(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object newNull() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isNull(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

}
