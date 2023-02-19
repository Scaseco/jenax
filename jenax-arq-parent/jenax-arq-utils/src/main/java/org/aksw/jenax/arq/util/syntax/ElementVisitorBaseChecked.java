package org.aksw.jenax.arq.util.syntax;

import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementDataset;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitorBase;

public class ElementVisitorBaseChecked
    extends ElementVisitorBase
{
    public void onVisit(Element el) { }

    @Override
    public void visit(ElementTriplesBlock el)   { onVisit(el); }

    @Override
    public void visit(ElementFilter el)         { onVisit(el); }

    @Override
    public void visit(ElementAssign el)         { onVisit(el); }

    @Override
    public void visit(ElementBind el)           { onVisit(el); }

    @Override
    public void visit(ElementData el)           { onVisit(el); }

    @Override
    public void visit(ElementUnion el)          { onVisit(el); }

    @Override
    public void visit(ElementDataset el)        { onVisit(el); }

    @Override
    public void visit(ElementOptional el)       { onVisit(el); }

    @Override
    public void visit(ElementLateral el)        { onVisit(el); }

    @Override
    public void visit(ElementGroup el)          { onVisit(el); }

    @Override
    public void visit(ElementNamedGraph el)     { onVisit(el); }

    @Override
    public void visit(ElementExists el)         { onVisit(el); }

    @Override
    public void visit(ElementNotExists el)      { onVisit(el); }

    @Override
    public void visit(ElementMinus el)          { onVisit(el); }

    @Override
    public void visit(ElementService el)        { onVisit(el); }

    @Override
    public void visit(ElementSubQuery el)       { onVisit(el); }

    @Override
    public void visit(ElementPathBlock el)      { onVisit(el); }
}
