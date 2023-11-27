package org.aksw.facete.v4.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.facete.v3.api.TreeDataMap;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.facete.treequery2.api.ScopedFacetPath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;

import com.google.common.collect.Iterables;

/** An element with a mapping of FacetPaths to ElementAccs and their variables */
public class MappedElement {
    /** Mapping of element paths (FacetPaths with the component set to the TUPLE constant) */
    // protected Map<FacetPath, ElementAcc> eltPathToAcc = new LinkedHashMap<>();
    // protected ElementAcc elementAcc = new ElementAcc(null, getElement(), null);

    protected Map<ScopedFacetPath, Var> pathToVar = new HashMap<>();
    protected TreeDataMap<ScopedFacetPath, ElementAcc> eltPathToAcc;
    protected Element element;

    public MappedElement() {
        this(new TreeDataMap<>(), new HashMap<>(), new ElementGroup());
    }

    public MappedElement(TreeDataMap<ScopedFacetPath, ElementAcc> eltPathToAcc, Map<ScopedFacetPath, Var> pathToVar, Element element) {
        super();
        this.eltPathToAcc = eltPathToAcc;
        this.pathToVar = pathToVar;
        this.element = element;
    }

    public MappedElement putAll(MappedElement src) {
        this.pathToVar.putAll(src.pathToVar);
        TreeDataMap<ScopedFacetPath, ElementAcc> srcTree = src.getEltPathToAcc();
        this.eltPathToAcc.putAll(srcTree);
        List<Element> a = ElementUtils.toElementList(element);
        List<Element> b = ElementUtils.toElementList(src.getElement());
        Element newElt = ElementUtils.groupIfNeeded(Iterables.concat(a, b));
        this.element = newElt;
        return this;
    }

    public TreeDataMap<ScopedFacetPath, ElementAcc> getEltPathToAcc() {
        return eltPathToAcc;
    }

    public Element getElement() {
        return element;
    }

    public Var getVar(ScopedFacetPath facetPath) {
        return pathToVar.get(facetPath);
    }
}
