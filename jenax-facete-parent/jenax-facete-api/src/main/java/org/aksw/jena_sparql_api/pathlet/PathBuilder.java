package org.aksw.jena_sparql_api.pathlet;

import org.aksw.facete.v3.api.path.StepImpl;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.aksw.jenax.sparql.path.PathUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.PathFactory;

public abstract class PathBuilder {
    public Path optional() {
        return optional("", null);
    }

    public Path optional(Object key) {
        return optional(key, null);
    }

    public Path optional(Object key, String alias) {
        return appendStep(new StepImpl("optional", key, null));
    }


    public Path fwd(String str) {
        return step(true, str, null);
    }

    public Path fwd(String str, String alias) {
        return step(true, str, alias);
    }

    public Path fwd(Resource p) {
        return fwd(p, null);
    }

    public Path fwd(Node node) {
        return fwd(node, null);
    }

    public Path fwd(Node node, String alias) {
        return step(PathUtils.createStep(node, true), alias);
    }


    public Path fwd(org.apache.jena.sparql.path.Path path) {
        return fwd(path, null);
    }

    public Path fwd(org.apache.jena.sparql.path.Path path, String alias) {
        return step(Fragment2Impl.create(path), alias);
    }


    /**
     * Wrap a path in a Path_0 object ;
     * as this is not a valid sparql construct this might be considered a hack
     *
     * @param path
     * @param isFwd
     * @return
     */
//    public static Path_0 createStep(Path path, boolean isFwd) {
//        Path result = isFwd ? path : PathFactory.pathInverse(path);
//        return result;
//    }


    public Path bwd(Node node) {
        return bwd(node, null);
    }

    public Path bwd(Node node, String alias) {
        return step(PathUtils.createStep(node, false), alias);
    }


    public Path bwd(org.apache.jena.sparql.path.Path path) {
        return bwd(path, null);
    }

    public Path bwd(org.apache.jena.sparql.path.Path path, String alias) {
        return step(Fragment2Impl.create(PathFactory.pathInverse(path)), alias);
    }



    public Path step(boolean isFwd, String pStr, String alias) {
        Node p = NodeFactory.createURI(pStr);
        P_Path0 path = PathUtils.createStep(p, isFwd);

        return step(path, alias);
    }

    public Path step(P_Path0 p, String alias) {
        return appendStep(new StepImpl("br", p, alias));
    }

    public Path step(Fragment2 br, String alias) {
        return appendStep(new StepImpl("br", br, alias));
    }

    public Path fwd(Resource p, String alias) {
//		BinaryRelation br = RelationUtils.createRelation(p.asNode(), false);
        return step(PathUtils.createStep(p.asNode(), true), alias);
    }

    public abstract Path appendStep(StepImpl step);
}