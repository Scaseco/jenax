package org.aksw.jenax.io.rdf.json;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jenax.io.rdf.jsonld.JsonLdTerms;
import org.aksw.jenax.path.core.PathPP;
import org.aksw.jenax.ron.ParentLink;
import org.aksw.jenax.ron.ParentLinkArray;
import org.aksw.jenax.ron.ParentLinkObject;
import org.aksw.jenax.ron.RdfElement;
import org.aksw.jenax.ron.RdfObject;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.util.NodeFactoryExtra;

record Match<T>(RdfElement elt, PathPP path, T value) {}

class UpSearch<T> {

    interface Matcher<T> {
        T match(RdfElement current, PathPP pathToOrigin);
    }

    public static <T> Match<T> search(RdfElement start, Matcher<T> matcher) {
        PathPP path = PathPP.newRelativePath();
        Match<T> result = search(start, path, matcher);
        return result;
    }

    public static <T> Match<T> search(RdfElement current, PathPP pathToOrigin, Matcher<T> matcher) {
        Match<T> result;
        T value = matcher.match(current, pathToOrigin);
        if (value != null) {
            result = new Match<>(current, pathToOrigin, value);
        } else {
            Entry<RdfObject, PathPP> nextSearch = getParentObject(current, pathToOrigin);
            if (nextSearch == null) {
                result = null;
            } else {
                RdfObject nextObj = nextSearch.getKey();
                PathPP nextPath = nextSearch.getValue();
                result = search(nextObj, nextPath, matcher);
            }
        }
        return result;
    }

    public static Entry<RdfObject, PathPP> getParentObject(RdfElement elt) {
        return getParentObject(elt, PathPP.newRelativePath());
    }

    /** Find the closest parent that is an object. Return it and the path to the given element. */
    public static Entry<RdfObject, PathPP> getParentObject(RdfElement elt, PathPP relPath) {
        Entry<RdfObject, PathPP> result;
        ParentLink link = elt.getParent();
        if (link == null) {
            result = null;
        } else if (link.isObjectLink()) {
            ParentLinkObject objLink = link.asObjectLink();
            PathPP nextPath = PathPP.newRelativePath(objLink.getKey()).resolve(relPath);
            result = Map.entry(objLink.getParent(), nextPath);
        } else if (link.isArrayLink()) {
            ParentLinkArray arrLink = link.asArrayLink();
            PathPP segment = PathPP.newRelativePath(new P_Link(NodeFactoryExtra.intToNode(arrLink.getIndex())));
            PathPP nextPath = segment.resolve(relPath);
            result = getParentObject(link.getParent(), nextPath);
        } else {
            throw new RuntimeException("Unknown link type: " + link.getClass());
        }
        return result;
    }
}

public class JsonLdContext {

    // Issue: Whenever we move up to a parent, we need to keep track of the relative path to our starting node
    //   Reason: A parent may use nested contexts to provide information for a certain field
    //   The first encountered field annotation wins

    public static String getNamespaceIri(RdfObject obj, String prefix) {
        RdfObject cxt = obj.getObject(JsonLdTerms.context);

        String result = null;
        if (cxt != null) {
            RdfElement elt = cxt.get(prefix);
            if (elt != null) {
                if (elt.isLiteral()) {
                    result = elt.getAsLiteral().getInternalId().getLiteralLexicalForm();
                } else {
                    throw new RuntimeException("not a literal");
                }
            } else {
                RdfObject parentObj = getParentObject(obj);
                result = getNamespaceIri(parentObj, prefix);
            }
        }

        return result;
    }

    public static RdfObject getParentObject(RdfElement elt) {
        RdfObject result;
        ParentLink link = elt.getParent();
        if (link == null) {
            result = null;
        } else if (link.isObjectLink()) {
            result = link.asObjectLink().getParent();
        } else if (link.isArrayLink()) {
            result = getParentObject(link.getParent());
        } else {
            throw new RuntimeException("Unknown link type: " + link.getClass());
        }
        return result;
    }
}
