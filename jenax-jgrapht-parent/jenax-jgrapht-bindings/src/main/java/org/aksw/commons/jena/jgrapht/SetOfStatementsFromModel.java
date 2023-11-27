package org.aksw.commons.jena.jgrapht;

import java.util.AbstractSet;
import java.util.Iterator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

import com.google.common.collect.Iterators;

public class SetOfStatementsFromModel
    extends AbstractSet<Statement>
{
    protected Model model;
    protected Property confinementProperty;

    public SetOfStatementsFromModel(Model model, Property confinementProperty) {
        super();
        this.model = model;
        this.confinementProperty = confinementProperty;
    }

    @Override
    public boolean add(Statement e) {
        boolean tmp = model.contains(e);
        if(!tmp) {
            model.add(e);
        }

        boolean result = !tmp;
        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = false;
        if(o instanceof Statement) {
            Statement stmt = (Statement)o;
            result = model.contains(stmt);
            if(result) {
                model.remove(stmt);
            }
        }
        return result;
    }

    @Override
    public boolean contains(Object o) {
        boolean result = o instanceof Statement ? model.contains((Statement)o) : false;
        return result;
    }

    @Override
    public Iterator<Statement> iterator() {
        return model.listStatements(null, confinementProperty, (RDFNode)null);
    }

    @Override
    public int size() {
        int result = Iterators.size(model.listStatements(null, confinementProperty, (RDFNode)null));
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((confinementProperty == null) ? 0 : confinementProperty.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SetOfStatementsFromModel other = (SetOfStatementsFromModel) obj;
        if (confinementProperty == null) {
            if (other.confinementProperty != null)
                return false;
        } else if (!confinementProperty.equals(other.confinementProperty))
            return false;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        return true;
    }
}
