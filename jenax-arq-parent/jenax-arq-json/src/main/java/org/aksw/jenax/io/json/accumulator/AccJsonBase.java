package org.aksw.jenax.io.json.accumulator;

import org.apache.jena.graph.Node;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;

abstract class AccJsonBase
    implements AccJson
{
    protected AccJson parent;
    protected JsonElement value = null;
    protected Node oldSourceNode; // Old value stored for debugging


    protected boolean hasBegun = false;
    protected Node currentSourceNode; // can be null


    protected boolean skipOutput = false;

//    public AccJsonBase(AccJson parent) {
//        super();
//        this.parent = parent;
//    }

    @Override
    public void setParent(AccJson parent) {
        Preconditions.checkArgument(this.parent == null, "Parent already set");
        this.parent = parent;
    }

    @Override
    public AccJson getParent() {
        return parent;
    }

    @Override
    public JsonElement getValue() {
        if (currentSourceNode != null) {
            throw new IllegalStateException("getValue() must only be called after end()");
        }
        return value;
    }

    @Override
    public void begin(Node sourceNode, AccContext cxt, boolean skipOutput) throws Exception {
        if (hasBegun) {
            throw new IllegalStateException("begin() has already been called() with " + currentSourceNode);
        }
        this.hasBegun = true;
        this.currentSourceNode = sourceNode;
        this.skipOutput = skipOutput;
    }

    @Override
    public void end(AccContext cxt) throws Exception {
        this.oldSourceNode = currentSourceNode;
        this.currentSourceNode = null;
        this.hasBegun = false;
    }

    @Override
    public boolean hasBegun() {
        return hasBegun;
    }
}
