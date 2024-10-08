package org.aksw.jenax.graphql.sparql.v2.ron;

import org.apache.jena.sparql.path.P_Path0;

/**
 * A data model for RDF tree structures akin to gson's JsonElement.
 */
public interface RdfElement {

    default boolean isArray() {
        return this instanceof RdfArray;
    }

    default RdfArray getAsArray() {
        return (RdfArray)this;
    }

    default boolean isObject() {
        return this instanceof RdfObject;
    }

    default RdfObject getAsObject() {
        return (RdfObject)this;
    }

    default boolean isLiteral() {
        return this instanceof RdfLiteral;
    }

    default RdfLiteral getAsLiteral() {
        return (RdfLiteral)this;
    }

    default boolean isNull() {
        return this instanceof RdfNull;
    }

    default RdfNull asNull() {
        return (RdfNull)this;
    }

    <T> T accept(RdfElementVisitor<T> visitor);

    ParentLink getParent();

    default void unlinkFromParent() {
        ParentLink link = getParent();

        if (link != null) {
            if (link.isObjectLink()) {
                ParentLinkObject objLink = link.asObjectLink();
                P_Path0 key = objLink.getKey();
                objLink.getParent().remove(key);
            } else if (link.isArrayLink()) {
                ParentLinkArray arrLink = link.asArrayLink();
                int index = arrLink.getIndex();
                // objLink.getParent().remove(index);
                arrLink.getParent().set(index, new RdfNull());
            } else {
                throw new RuntimeException("Unknown parent link type: " + link.getClass());
            }
        } else {
            // Ignore
            // throw new RuntimeException("Cannot unlink an element that does not have a parent");
        }
    }

    default RdfElement getRoot() {
        ParentLink link = getParent();
        RdfElement result = link == null ? this : link.getParent().getRoot();
        return result;
    }

//    default RdfElement resolve(PathPP path) {
//        RdfElement result;
//        if (path.isAbsolute()) {
//            PathPP relPath = path.relativize(PathPP.newAbsolutePath());
//            RdfElement root = getRoot();
//            result = root.resolve(relPath);
//        } else {
//            int n = path.getNameCount();
//            if (n == 0) {
//                result = this;
//            } else {
//                RdfElement next;
//                PathPP segment = path.getName(0);
//                P_Path0 p = segment.toSegment();
//                PathPP rest = path.subpath(1, n);
//                if (PathOpsPP.PARENT.equals(p)) {
//                    next = getParent().getParent();
//                } else if (PathOpsPP.SELF.equals(p)) {
//                    next = this;
//                } else {
//                    if (this.isObject()) {
//                        next = this.getAsObject().get(p);
//                    } else if (this.isArray()) {
//                        if (!p.isForward()) {
//                            throw new RuntimeException("Cannot resolve backward step on array");
//                        }
//                        int index = NodeFactoryExtra.nodeToInt(p.getNode());
//                        next = this.getAsArray().get(index);
//                    } else {
//                        throw new RuntimeException("Cannot resolve path (other than PARENT and SELF) on literal");
//                    }
//                }
//                result = next.resolve(rest);
//            }
//        }
//        return result;
//    }

//    public static RdfElement of(Node node) {
//        RdfElement result = node == null
//            ? RdfNull.get()
//            : RdfElement.newObject(node);
//        return result;
//    }

//    public static RdfElement newObject(Node node) {
//        return new RdfObjectImpl(node);
//    }
//
//    public static RdfArray newArray() {
//        return new RdfArrayImpl();
//    }
//
//    public static RdfElement newLiteral(Node node) {
//        return new RdfLiteralImpl(node);
//    }
//
//    public static RdfElement nullValue() {
//        return RdfNull.get();
//    }
}
