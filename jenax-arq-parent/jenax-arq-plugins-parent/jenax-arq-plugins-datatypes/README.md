---
title: Datatype Extensions
parent: RDF/SPARQL Plugins
nav_order: 10
---

This module adds RDF datatype extensions for ARQ constructs, such as queries, expressions and bindings.
The main benefits are:

* Syntax validation of appropriately typed RDF literals when parsing RDF data
* Automatic mapping of RDF literals to the appropriate Java objects, e.g. `Query query = (Query)rdfNode.asLiteral().getValue();`

## Maven Dependency
```xml
<dependency>
  <groupId>org.aksw.jenax</groupId>
  <artifactId>jenax-arq-plugins-datatype</artifactId>
</dependency>
```

## Table

| Type | Implementation | RDF Datatype IRI |
|---|---|---|
| Query<br /><sub>`org.apache.jena.query.Query`</sub> | <sub>`org.aksw.jenax.arq.datatype.RDFDatatypeQuery`</sub> | `http://jsa.aksw.org/dt/sparql/query` |
| Binding<br /><sub>`org.apache.jena.sparql.engine.binding.Binding`</sub> | <sub>`org.aksw.jenax.arq.datatype.RDFDatatypeBinding`</sub> | `http://jsa.aksw.org/dt/sparql/binding` |
| Expr<br /><sub>`org.apache.jena.sparql.expr.Expr`</sub> | <sub>`org.aksw.jenax.arq.datatype.RDFDatatypeExpr`</sub> | `http://jsa.aksw.org/dt/sparql/expr` |
| ExprList<br /><sub>`org.apache.jena.sparql.expr.ExprList`</sub> | <sub>`org.aksw.jenax.arq.datatype.RDFDatatypeExprList`</sub> | Used internally for the `Binding` datatype. No public IRI yet. |
| NodeList<br /><sub>`org.aksw.jenax.arq.util.node.NodeList`</sub> | <sub>`org.aksw.jenax.arq.datatype.RDFDatatypeNodeList`</sub> | `http://jsa.aksw.org/dt/sparql/array` |
| Query<br /><sub>`org.aksw.jenax.arq.util.node.NodeSet`</sub> | <sub>`org.aksw.jenax.arq.datatype.RDFDatatypeNodeSet`</sub> | `http://jsa.aksw.org/dt/sparql/set` |

## Example

```turtle
@prefix eg:  <http://www.example.org/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix xdt: <http://jsa.aksw.org/dt/sparql/> .

<urn:example:s>  eg:hasArray  "<http://www.w3.org/2000/01/rdf-schema#Class> <http://www.w3.org/2002/07/owl#Class>"^^rdf:array ;
        eg:hasBinding  "coalesce(( ?s = rdf:type ))"^^xdt:binding ;
        eg:hasExpr     "concat(\"foo\", ?bar)"^^xdt:expr ;
        eg:hasQuery    "SELECT  *\nWHERE\n  { ?s  ?p  ?o }\n"^^xdt:query ;
        eg:hasSet      "<http://www.w3.org/2000/01/rdf-schema#comment> <http://www.w3.org/2000/01/rdf-schema#label>"^^rdf:set .

```

Note: Currently `array` and `set` are in the `rdf` namespace. However, eventually they will be moved to the `xdt` namespace.
