package org.aksw.jenax.reprogen.hashid;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.commons.beans.datatype.DataType;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.path.P_Path0;

public class PropertyDescriptor {
    protected ClassDescriptor classDescriptor;
    protected P_Path0 path;


    // TODO Also track target type for generics?
    protected DataType targetType;

    protected Function<? super Resource, ? extends Collection<? extends RDFNode>> rawProcessor;


    /** A predicate that can decide for a given RDFNode whether to descend into that node.
     * If the predicate evaluates to false for an IRIs then it is not descended into */
    protected Predicate<Statement> allowDescendPredicate = null;

    protected boolean includedInHashId;

    // If true the values are represented as IRIs but for the sake of identity are treated as string literals
    // Hence, ID computation does not descend into those RDF resources
    // Effectively an attribute that tells that preventDescend is always true
    protected boolean isIriType;


    /**
     * If ownsValue is true then the properties value is owned by the source entity.
     * The referred to entity does not have a hashId of its own, instead, the hashId
     * is defined by the owning class and the referring property IRI.
     */
    // protected boolean ownsValue;

    /**
     * Only valid if includeInHashId is true:
     * Usually the rdf property (path) is combined with the value to form the id. This excludes the rdf property
     * and thus only uses the value.
     *
     */
    protected boolean rdfPropertyExcludedFromHashId;


    /**
     * Whether the target of this property is owned by the source entity.
     * Ownerships trigger a second hash-id assignment pass that alters the hash
     * ids of owned entities with the hash id of the owner.
     */
    protected boolean isTargetOwned;

    public PropertyDescriptor(ClassDescriptor classDescriptor, P_Path0 path) {
        super();
        this.classDescriptor = classDescriptor;
        this.path = path;
    }

    public P_Path0 getPath() {
        return path;
    }

    public PropertyDescriptor setRdfPropertyExcludedFromHashId(boolean onOrOff) {
        this.rdfPropertyExcludedFromHashId = onOrOff;
        return this;
    }

    public boolean isExcludeRdfPropertyFromHashId() {
        return rdfPropertyExcludedFromHashId;
    }

    public PropertyDescriptor setIncludedInHashId(boolean onOrOff) {
        this.includedInHashId = onOrOff;
        return this;
    }

    public boolean isIncludedInHashId() {
        return includedInHashId;
    }

    public PropertyDescriptor setIriType(boolean isIriType) {
        this.isIriType = isIriType;
        return this;
    }

    public boolean isIriType() {
        return isIriType;
    }

    public PropertyDescriptor setAllowDescendPredicate(Predicate<Statement> allowDescendPredicate) {
        this.allowDescendPredicate = allowDescendPredicate;
        return this;
    }

    public Predicate<Statement> getAllowDescendPredicate() {
        return allowDescendPredicate;
    }

    public PropertyDescriptor setRawProcessor(Function<? super Resource, ? extends Collection<? extends RDFNode>> rawProcessor) {
        this.rawProcessor = rawProcessor;
        return this;
    }

    public Function<? super Resource, ? extends Collection<? extends RDFNode>> getRawProcessor() {
        return rawProcessor;
    }

    public boolean isTargetOwned() {
        return isTargetOwned;
    }

    public PropertyDescriptor setTargetOwned(boolean isTargetOwned) {
        this.isTargetOwned = isTargetOwned;
        return this;
    }

    public DataType getTargetType() {
        return targetType;
    }

    public PropertyDescriptor setTargetType(DataType targetType) {
        this.targetType = targetType;
        return this;
    }
}
