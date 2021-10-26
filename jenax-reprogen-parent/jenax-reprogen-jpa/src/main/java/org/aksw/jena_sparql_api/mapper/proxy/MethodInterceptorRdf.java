package org.aksw.jena_sparql_api.mapper.proxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyValue;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

public class MethodInterceptorRdf
    implements MethodInterceptor
{
    //private Map<String, Coordinate> propertyToCoordinate;
    //private Map<>

    protected Object proxied;

    protected RdfClass rdfClass;

    protected Node presetSubject; // The subject URI that corresponds to the proxied object
    protected DatasetGraph datasetGraph;

    public MethodInterceptorRdf(Object proxied, RdfClass rdfClass, Node subject) {
        this(proxied, rdfClass, subject, DatasetGraphFactory.createGeneral());
    }

    public MethodInterceptorRdf(Object proxied, RdfClass rdfClass, Node subject, DatasetGraph datasetGraph) {
        this.proxied = proxied;

        this.rdfClass = rdfClass;

        /**
         * Pre-set subject - may be null
         */
        this.presetSubject = subject;

        /**
         * Pre-set datasetGraph, may be empty (but must not be null)
         */
        this.datasetGraph = datasetGraph;
    }

    public RdfClass getRdfClass() {
        return rdfClass;
    }

    public Object getProxied() {
        return proxied;
    }

    public Node getPresetSubject() {
        return presetSubject;
    }

    public DatasetGraph getDatasetGraph() {
        return datasetGraph;
    }

    public void setDatasetGraph(DatasetGraph datasetGraph) {
        this.datasetGraph = datasetGraph;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {


        BeanWrapperImpl bean = new BeanWrapperImpl(proxied);

        PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);

        if(pd != null) {
            String propertyName = pd.getName();
            PropertyValue x;

            boolean writable = bean.isWritableProperty(propertyName);
            boolean readable = bean.isReadableProperty(propertyName);

            System.out.println(propertyName + " " + readable + " " + writable);
        }

        //return methodProxy.invokeSuper(object, args);
        return methodProxy.invoke(proxied, args);
    }

}