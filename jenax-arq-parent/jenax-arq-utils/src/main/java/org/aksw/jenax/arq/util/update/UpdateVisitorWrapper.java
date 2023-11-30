package org.aksw.jenax.arq.util.update;

import org.apache.jena.sparql.modify.request.UpdateAdd;
import org.apache.jena.sparql.modify.request.UpdateClear;
import org.apache.jena.sparql.modify.request.UpdateCopy;
import org.apache.jena.sparql.modify.request.UpdateCreate;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.sparql.modify.request.UpdateLoad;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.modify.request.UpdateMove;
import org.apache.jena.sparql.modify.request.UpdateVisitor;

public interface UpdateVisitorWrapper<T extends UpdateVisitor>
    extends UpdateVisitor
{
    T getDelegate();

    @Override
    default void visit(UpdateDrop update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateClear update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateCreate update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateLoad update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateAdd update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateCopy update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateMove update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateDataInsert update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateDataDelete update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateDeleteWhere update) {
        getDelegate().visit(update);
    }

    @Override
    default void visit(UpdateModify update) {
        getDelegate().visit(update);
    }
}
