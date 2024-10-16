package org.aksw.jenax.dataaccess.sparql.polyfill.detector;

public abstract class SparqlPolyfillDetectorBase
    implements SparqlPolyfillDetector
{
    protected String name;

    public SparqlPolyfillDetectorBase(String name) {
        super();
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
